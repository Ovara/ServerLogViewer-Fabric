package fish.crafting.logviewer.log.stitch;

import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.util.LogUtil;
import fish.crafting.logviewer.util.Logs;
import lombok.Getter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class LogStitchingInstance {

    @Getter
    private final int parts;
    private final DataChunk[] chunks;
    @Getter
    private int finished = 0;

    public LogStitchingInstance(int parts){
        this.parts = parts;
        this.chunks = new DataChunk[parts];
    }

    public void addData(int stitchIndex, byte[] bytes) {
        if(stitchIndex < 0 || stitchIndex >= parts){
            Logs.logMessage("Log stitch index out of bounds! " + stitchIndex + " for [0, " + parts + ")!");
            return;
        }

        if(chunks[stitchIndex] != null){
            Logs.logMessage("Log stitch duplicate found! Index: " + stitchIndex);
            return;
        }

        chunks[stitchIndex] = new DataChunk(bytes);
        finished++;
    }

    public boolean finished(){
        //This is fine
        for (DataChunk chunk : chunks) {
            if (chunk == null) return false;
        }

        return true;
    }

    public List<LogLine> stitch(){
        try {
            return readLines(decompressGzip(combineChunks()));
        } catch (IOException e) {
            Logs.logError(e);
            return null;
        }
    }

    private List<LogLine> readLines(byte[] data) throws IOException {
        List<LogLine> lines = new ArrayList<>();
        try (ByteArrayInputStream is = new ByteArrayInputStream(data);
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(LogUtil.readLogLine(line));
            }
        }
        return lines;
    }

    private byte[] decompressGzip(byte[] compressed) throws IOException {
        try (ByteArrayInputStream is = new ByteArrayInputStream(compressed);
             GZIPInputStream gzip = new GZIPInputStream(is);
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = gzip.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return os.toByteArray();
        }
    }

    private byte[] combineChunks() {
        int totalSize = Arrays.stream(chunks).mapToInt(b -> b.data.length).sum();
        byte[] combined = new byte[totalSize];
        int offset = 0;
        for (DataChunk chunk : chunks) {
            System.arraycopy(chunk.data, 0, combined, offset, chunk.data.length);
            offset += chunk.data.length;
        }
        return combined;
    }

    private record DataChunk(byte[] data){
    }
}
