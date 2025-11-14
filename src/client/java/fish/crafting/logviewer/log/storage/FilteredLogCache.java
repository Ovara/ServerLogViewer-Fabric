package fish.crafting.logviewer.log.storage;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.file.LogFileManager;
import fish.crafting.logviewer.util.LogUtil;
import fish.crafting.logviewer.util.Logs;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FilteredLogCache extends LogCache<FilteredLogCache.PendingLogChunk> {

    private IntArrayList filteredLines;
    private int revision = 0;

    public FilteredLogCache(IntArrayList filteredLines) {
        super(500, 4);
        this.filteredLines = filteredLines;
    }

    public void updateLines(IntArrayList lines){
        filteredLines = lines;
        loadingQueue.clear();
        chunks.clear();
        viewIndex0 = viewIndex1 = -1;
        revision++;
    }

    @Override
    protected void chunkWorker() {
        try{
            while (running) {
                PendingLogChunk chunk = loadingQueue.take();
                if(!chunk.shouldLoad()) continue;

                processChunk(chunk);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processChunk(PendingLogChunk chunk){
        int lineStart = chunk.id * chunkSize;
        int lineEnd = lineStart + chunkSize;
        int rev = revision;

        List<Long> lineOffsets = LogFileManager.get().getLineOffsets();
        lineEnd = Math.min(filteredLines.size(), lineEnd);

        List<LogLine> loadedLines = new ArrayList<>(lineEnd - lineStart);

        try (FileChannel channel = FileChannel.open(LogFileManager.get().getActiveLogFile().toPath(), StandardOpenOption.READ)) {
            for (int i = lineStart; i < lineEnd; i++) {
                int lineID = filteredLines.getInt(i);
                if (lineID >= lineOffsets.size()) continue;

                long startOffset = lineOffsets.get(lineID);
                long endOffset = (lineID + 1 < lineOffsets.size()) ? lineOffsets.get(lineID + 1) : channel.size();
                int size = (int) (endOffset - startOffset);

                ByteBuffer buffer = ByteBuffer.allocate(size);

                int bytesRead = 0;
                while (bytesRead < size) {
                    int r = channel.read(buffer, startOffset + bytesRead);
                    if (r <= 0) break;
                    bytesRead += r;
                }
                buffer.flip();

                byte[] lineBytes = new byte[buffer.remaining()];
                buffer.get(lineBytes);
                String lineStr = new String(lineBytes);

                if(lineStr.startsWith("\n")) lineStr = lineStr.substring(1);

                LogLine logLine = LogUtil.readIndexedLine(lineID, lineStr);
                logLine.lineId(lineID);

                loadedLines.add(logLine);
            }
        } catch (IOException e) {
            Logs.logError(e);
            return;
        }

        LogChunk logChunk = new LogChunk(chunkSize);
        logChunk.insert(loadedLines.toArray(new LogLine[0]));

        if(rev != revision) return;

        synchronized (chunks) {
            chunks.put(chunk.id, logChunk);
        }

        ConnectionManager.get().use(logInstance -> {
            logInstance.getViewerComponents().updateIndex(chunk.id);
        });
    }

    @Override
    public void addLines(List<LogLine> lines) {
        super.addLines(lines);
        filteredLines.addAll(lines.stream().map(LogLine::lineId).toList());
    }

    @Override
    protected PendingLogChunk createLogChunk(int id) {
        return new PendingLogChunk(id);
    }

    @Override
    public int getLineAmount() {
        return filteredLines.size();
    }

    protected class PendingLogChunk extends PendingChunk {

        public PendingLogChunk(int id) {
            super(id);
        }

        public boolean shouldLoad() {
            return minRange.get() <= id && id <= maxRange.get();
        }
    }
}
