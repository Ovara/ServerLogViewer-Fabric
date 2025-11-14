package fish.crafting.logviewer.log.storage;

import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.file.LogFileManager;
import fish.crafting.logviewer.settings.custom.PerformanceSettings;
import fish.crafting.logviewer.ui.LogScreen;
import fish.crafting.logviewer.util.Logs;
import lombok.Getter;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class LogCache<T extends LogCache.PendingChunk> {

    protected volatile boolean running = true;
    private final List<Thread> threads = new ArrayList<>();

    private int chunkReaderThreads;
    @Getter
    protected final int chunkSize;

    @Getter
    protected final Map<Integer, LogChunk> chunks = new ConcurrentHashMap<>();

    @Getter
    protected int viewIndex0 = -1, viewIndex1 = -1;
    protected BlockingDeque<T> loadingQueue = new LinkedBlockingDeque<>();
    @Getter
    protected final AtomicInteger minRange = new AtomicInteger(-1), maxRange = new AtomicInteger(-1);

    public LogCache(int chunkSize, int readerThreads){
        this.chunkSize = chunkSize;
        this.chunkReaderThreads = readerThreads;
        startChunkReaders();
    }

    public void setReaderThreads(int threads){
        if(threads < 1) return;
        if(threads == chunkReaderThreads) return;

        chunkReaderThreads = threads;

        //Reboot threads
        shutdown();
        startChunkReaders();
    }

    private void startChunkReaders() {
        running = true;
        int clampedThreads = Math.min(Runtime.getRuntime().availableProcessors(), chunkReaderThreads);

        for (int i = 0; i < clampedThreads; i++) {
            Thread t = new Thread(this::chunkWorker, "ChunkReader-" + i);
            t.setDaemon(true);
            t.start();
            threads.add(t);
        }
    }

    public void shutdown() {
        running = false;
        for (Thread t : threads) {
            t.interrupt();
        }

        threads.clear();
        loadingQueue.clear();
    }

    protected abstract void chunkWorker();

    public LogChunk getLogChunk(int index) {
        return chunks.get(index);
    }

    public void update(){
        var scrollContainer = LogScreen.getScrollContainer();
        int firstIndex, secondIndex;
        if(scrollContainer == null) {
            firstIndex = secondIndex = 0;
        }else{
            double scroll = scrollContainer.getScrollOffset();
            int height = scrollContainer.height();

            if(height == 0) return;
            int chunkComponentSize = chunkSize * LogLine.LINE_COMPONENT_HEIGHT;

            int first = (int) scroll;
            int second = (int) Math.ceil(scroll) + height;

            firstIndex = first / chunkComponentSize;
            secondIndex = second / chunkComponentSize;
        }

        if(firstIndex == viewIndex0 && secondIndex == viewIndex1) return;
        viewIndex0 = firstIndex;
        viewIndex1 = secondIndex;

        int maxIndex = getLineAmount() / chunkSize;

        minRange.set(firstIndex > 0 ? firstIndex - 1 : 0);
        maxRange.set(secondIndex < maxIndex ? secondIndex + 1 : maxIndex);

        for(int i = firstIndex; i <= secondIndex; i++){
            queueLoad(i, true);
        }

        queueLoad(secondIndex + 1, false);
        queueLoad(firstIndex - 1, false);

        int a = minRange.get();
        int b = maxRange.get();

        //Mark chunks for decaying
        chunks.forEach((id, chunk) -> {
            if(id >= a && id <= b) return;
            int distance = id < a ? (a - id) : (id - b);

            int seconds = Math.max(10 - distance * 2, 0);
            chunk.clampedDecaysIn(seconds);
        });
    }

    public void removeDecayedChunks(){
        List<Integer> chunksToRemove = new LinkedList<>();
        int a = minRange.get();
        int b = maxRange.get();
        chunks.forEach((id, chunk) -> {
            if(chunk.isDecayed()){
                if(id >= a && id <= b) { //Loaded chunk slipped through somehow
                    chunk.decaysIn(-1);
                }else{
                    chunksToRemove.add(id);
                }
            }
        });

        for (Integer i : chunksToRemove) {
            chunks.remove(i);
        }
    }

    private void queueLoad(int id, boolean priority){
        if(id < 0) return;

        LogChunk logChunk = chunks.get(id);
        if(logChunk != null) {
            logChunk.decaysIn(-1);
            return;
        }

        T pending = createLogChunk(id);

        if (priority) loadingQueue.offerFirst(pending);
        else loadingQueue.offerLast(pending);
    }

    public void addLines(List<LogLine> lines) {
        int lineAmount = getLineAmount();
        int i = lineAmount / chunkSize;

        LogChunk logChunk = chunks.get(i);
        if(logChunk == null) return;

        lines = lines.stream().toList();
        if(lines.isEmpty()) return;

        LogLine[] lineArr = lines.toArray(new LogLine[0]);
        while(true){
            var overflow = logChunk.insert(lineArr);
            if(overflow == null) break;

            lineArr = overflow;
            i++;

            logChunk = chunks.get(i); //This will probably never run
            if(logChunk == null) break;
        }
    }

    protected abstract T createLogChunk(int id);

    public abstract int getLineAmount();

    public void clearData() {
        loadingQueue.clear();
        chunks.clear();
        viewIndex0 = viewIndex1 = -1;
    }

    public static abstract class PendingChunk {
        @Getter
        public final int id;

        public PendingChunk(int id) {
            this.id = id;
        }
    }
}
