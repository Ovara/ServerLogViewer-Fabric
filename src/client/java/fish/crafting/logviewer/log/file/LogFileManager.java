package fish.crafting.logviewer.log.file;

import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.util.FileUtil;
import fish.crafting.logviewer.util.Logs;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class LogFileManager {

    private static LogFileManager instance;

    @Getter
    private int lineAmount = 0;
    private final List<Long> lineOffsets = new ArrayList<>();
    private long lastOffset = 0;
    private final ReentrantLock lock = new ReentrantLock();
    private final BlockingQueue<LogLine> writeQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = false;
    private Thread writerThread;
    @Getter
    private SimpleLogIndex logIndex = new ComplexLogIndex();

    private LogFileManager(){
        instance = this;
    }

    public static LogFileManager get(){
        return instance == null ? new LogFileManager() : instance;
    }

    public File getActiveLogFile(){
        return new File(FileUtil.getDataDir(), "latest_log");
    }

    public void startWriterThread() {
        if(running) return;

        running = true;
        lineAmount = 0;
        writerThread = new Thread(() -> {
            purge();
            boolean firstLine = true;

            try (RandomAccessFile raf = new RandomAccessFile(getActiveLogFile(), "rw");
                 FileChannel channel = raf.getChannel()) {

                while (running) {
                    try {
                        LogLine line = writeQueue.take();

                        String s = line.message();
                        if(!firstLine) s = "\n" + s;
                        firstLine = false;

                        byte[] bytes = s.getBytes();
                        ByteBuffer buffer = ByteBuffer.wrap(bytes);

                        lock.lock();
                        try {
                            raf.seek(lastOffset);
                            while (buffer.hasRemaining()) {
                                channel.write(buffer);
                            }
                            lineOffsets.add(lastOffset);
                            lastOffset += bytes.length;
                        } finally {
                            lock.unlock();
                        }

                        logIndex.addIndex(line);
                        line.onWritten();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "LogFileWriter");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    public void write(List<LogLine> lines) {
        lineAmount += lines.size();
        if (running) {
            lines.forEach(writeQueue::offer);
        }
    }

    private void purge() {
        lock.lock();
        logIndex = new SimpleLogIndex();

        try {
            Files.newBufferedWriter(getActiveLogFile().toPath(), StandardOpenOption.TRUNCATE_EXISTING).close();
            lineOffsets.clear();
            lastOffset = 0;
        } catch (IOException e) {
            Logs.logError(e);
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        if(!running) return;

        running = false;
        writerThread.interrupt();
    }

    public List<Long> getLineOffsets() {
        lock.lock();
        try {
            return new ArrayList<>(lineOffsets);
        } finally {
            lock.unlock();
        }
    }

}
