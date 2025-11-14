package fish.crafting.logviewer.log.filter;

import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.file.LogFileManager;
import fish.crafting.logviewer.settings.custom.PerformanceSettings;
import fish.crafting.logviewer.util.Logs;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LogSearchQuery {

    private final int BUFFER_SIZE = 4096;
    private int threadCount = PerformanceSettings.get().SEARCH_THREAD_COUNT.get();
    @Getter
    private String query = "";
    @Getter
    private int searchId = 0;

    public void updateQuery(String query) {
        this.query = query;
        searchId++;
        if(searchId > 1_000_000_000) searchId = -1_000_000_000;
    }

    public void setThreadCount(int count){
        if(count < 1) return;
        this.threadCount = count;
    }

    public boolean hasQuery() {
        return !query.isEmpty();
    }

    public IntArrayList search(IntArrayList filteredLines) throws IOException, InterruptedException {
        if (filteredLines.isEmpty() || !hasQuery()) return new IntArrayList();

        String needle = query.toLowerCase();
        List<Long> lineOffsets = LogFileManager.get().getLineOffsets();
        IntArrayList result = new IntArrayList();

        int clampedThreadCount = Math.min(threadCount, Runtime.getRuntime().availableProcessors());

        try (FileChannel channel = FileChannel.open(LogFileManager.get().getActiveLogFile().toPath(), StandardOpenOption.READ);
             ExecutorService executor = Executors.newFixedThreadPool(clampedThreadCount)) {

            List<Future<IntArrayList>> futures = new ArrayList<>();
            int total = filteredLines.size();
            int chunk = Math.max(1, total / clampedThreadCount);

            for (int t = 0; t < clampedThreadCount; t++) {
                final int start = t * chunk;
                final int end = (t == clampedThreadCount - 1) ? total : (t + 1) * chunk;

                futures.add(executor.submit(() -> searchRange(channel, lineOffsets, filteredLines, start, end, needle)));
            }

            for (Future<IntArrayList> f : futures) {
                try {
                    result.addAll(f.get());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public boolean matchesQuery(int searchId){
        return searchId == this.searchId;
    }

    private IntArrayList searchRange(FileChannel channel, List<Long> lineOffsets, IntArrayList filtered,
                                     int start, int end, String needle) {
        IntArrayList out = new IntArrayList();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        needle = needle.toLowerCase();

        for (int i = start; i < end; i++) {
            int lineID = filtered.getInt(i);
            if (lineID >= lineOffsets.size()) continue;

            try {
                long startOffset = lineOffsets.get(lineID);
                long endOffset = ((lineID + 1) < lineOffsets.size())
                        ? lineOffsets.get(lineID + 1)
                        : channel.size();

                int len = (int) (endOffset - startOffset);
                if (len <= 0) continue;

                if (buffer.capacity() < len) buffer = ByteBuffer.allocate(len);
                buffer.clear();

                int bytesRead = 0;
                while (bytesRead < len) {
                    int r = channel.read(buffer, startOffset + bytesRead);
                    if (r <= 0) break;
                    bytesRead += r;
                }
                buffer.flip();

                boolean found = false;
                for (int j = 0; j <= len - needle.length(); j++) {
                    int k;
                    for (k = 0; k < needle.length(); k++) {
                        char c = (char) buffer.get(j + k);
                        if (Character.toLowerCase(c) != needle.charAt(k)) break;
                    }

                    if (k == needle.length()) {
                        found = true;
                        break;
                    }
                }

                if (found) out.add(lineID);

            } catch (IOException e) {
                Logs.logError(e);
            }
        }

        return out;
    }

    public List<LogLine> filterLines(List<LogLine> lines) {
        if(query.isEmpty()) return lines;

        List<LogLine> out = new ArrayList<>();
        String needle = query.toLowerCase();
        int needleLen = needle.length();

        for(LogLine line : lines) {
            String msg = line.message();
            if (msg.length() < needleLen) continue;

            boolean found = false;
            for (int i = 0; i <= msg.length() - needleLen; i++) {
                int j;
                for(j = 0; j < needleLen; j++) {
                    char c1 = Character.toLowerCase(msg.charAt(i + j));
                    char c2 = needle.charAt(j);
                    if (c1 != c2) break;
                }

                if(j == needleLen) {
                    found = true;
                    break;
                }
            }

            if(found) out.add(line);
        }

        return out;
    }

}
