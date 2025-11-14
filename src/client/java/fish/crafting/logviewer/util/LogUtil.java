package fish.crafting.logviewer.util;

import fish.crafting.logviewer.ServerLogViewerClient;
import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.log.LogLevel;
import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.LogPlugin;
import fish.crafting.logviewer.log.file.LogFileManager;
import fish.crafting.logviewer.log.file.SimpleLogIndex;

import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

public class LogUtil {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static LogLine readIndexedLine(int lineId, String line){
        SimpleLogIndex logIndex = LogFileManager.get().getLogIndex();
        LogLevel level = logIndex.getLevel(lineId);
        LogPlugin plugin = logIndex.getPlugin(lineId);

        return new LogLine(line, level, null).plugin(plugin);
    }

    public static LogLine readLogLine(String line){
        if(line.isEmpty()) return new LogLine("Line Error", LogLevel.SYSTEM, null);

        int levelIndex = line.charAt(0);
        //short pluginIndex = (short) line.charAt(1);
        line = line.length() == 1 ? " " : line.substring(1);

        LogLevel level = LogLevel.SYSTEM;
        try{
            level = LogLevel.values()[levelIndex];
        }catch (Exception ignored){}
        return new LogLine(line, level, null);

        /*if(!line.startsWith("[") || 1 > 0 || line.startsWith("[Not Secure]")) return new LogLine(line, LogLevel.SYSTEM, null);

        int timeEnd = line.indexOf(']');
        if(timeEnd == -1) return new LogLine(line, LogLevel.SYSTEM, null);

        int threadStart = line.indexOf('[', timeEnd + 2) + 1;
        int slash = line.indexOf('/', threadStart);
        int threadEnd = line.indexOf(']', slash);
        String levelStr = line.substring(slash + 1, threadEnd);
        LogLevel level = LogLevel.INFO;
        try{
            level = LogLevel.valueOf(levelStr.toUpperCase());
        }catch (Exception ignored){}

        int messageStart = threadEnd + 3;

        String message = line;
        if(messageStart < line.length() - 1){
            message = line.substring(messageStart);
        }

        return new LogLine(message, level, null);*/
    }

    private static Instant toInstant(String timeStr) {
        // Parse the time part
        LocalTime localTime = LocalTime.parse(timeStr, TIME_FORMATTER);
        LocalDate today = LocalDate.now(ZoneId.systemDefault()); //todo sync with server to see what region is used
        return localTime.atDate(today).toInstant(ZoneOffset.UTC);
    }
}
