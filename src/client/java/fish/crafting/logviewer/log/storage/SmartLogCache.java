package fish.crafting.logviewer.log.storage;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.file.LogFileManager;
import fish.crafting.logviewer.util.LogUtil;
import fish.crafting.logviewer.util.Logs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class SmartLogCache extends LogCache<SmartLogCache.PendingLogChunk> {

    public SmartLogCache(){
        super(500, 4);
    }

    protected void chunkWorker(){
        try (FileChannel channel = FileChannel.open(LogFileManager.get().getActiveLogFile().toPath(), StandardOpenOption.READ)) {
            while (running) {
                PendingLogChunk chunk = loadingQueue.take();
                if(!chunk.shouldLoad()) continue;

                processChunk(channel, chunk);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            Logs.logError(e);
        }
    }

    protected void processChunk(FileChannel channel, PendingLogChunk pending) throws IOException {
        List<Long> offsets = LogFileManager.get().getLineOffsets();
        long startLine = (long) pending.id * chunkSize;
        long endLine = Math.min(startLine + chunkSize, offsets.size());

        if (startLine >= endLine) return;

        long startOffset = offsets.get((int) startLine);
        long endOffset = (int) endLine < offsets.size() ? offsets.get((int) endLine) : channel.size();

        int size = (int) (endOffset - startOffset);
        ByteBuffer buffer = ByteBuffer.allocate(size);

        // ensure full buffer read
        int bytesRead = 0;
        while (bytesRead < size) {
            int r = channel.read(buffer, startOffset + bytesRead);
            if (r <= 0) break;
            bytesRead += r;
        }

        buffer.flip();
        String chunkData = new String(buffer.array());

        String[] lines = chunkData.split("\n");
        int i = 0;
        boolean first = pending.id != 0; //If the chunk ID isn't 0, there is a \n at the beginning of the line.
        for (String line : lines) {
            if(first){
                first = false;
                continue;
            }

            pending.loadedLines.add(LogUtil.readIndexedLine((int) (startLine + i), line));
            i++;
        }

        int lineIndex = pending.id * chunkSize;
        for (LogLine line : pending.loadedLines) {
            line.lineId(lineIndex++);
        }

        LogChunk logChunk = new LogChunk(chunkSize);
        logChunk.insert(pending.loadedLines.toArray(new LogLine[0]));
        chunks.put(pending.id, logChunk);

        ConnectionManager.get().use(logInstance -> {
            logInstance.getViewerComponents().updateIndex(pending.id);
        });
    }

    @Override
    protected PendingLogChunk createLogChunk(int id) {
        return new PendingLogChunk(id);
    }

    @Override
    public int getLineAmount() {
        return LogFileManager.get().getLineAmount();
    }

    protected class PendingLogChunk extends PendingChunk {
        protected final List<LogLine> loadedLines = new ArrayList<>();

        private PendingLogChunk(int id) {
            super(id);
        }

        public boolean shouldLoad(){
            return minRange.get() <= id && id <= maxRange.get();
        }
    }

}
