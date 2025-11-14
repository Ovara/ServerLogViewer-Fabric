package fish.crafting.logviewer.log.stitch;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.log.LogLevel;
import fish.crafting.logviewer.log.LogLine;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

/**
 * When receiving the stitched lines, we don't add them directly to the LogCache.
 * Instead, we assume that when the LogCache is queried, the stitched lines will
 * have already been written to the file. However, when reloading from the menu,
 * the screen will build before all the lines had a chance to be written.
 * So this will reload the menu once the last stitched line was written.
 */
public class LastStitchedLogLine extends LogLine {

    public LastStitchedLogLine(String message, LogLevel level, @Nullable Instant timestamp) {
        super(message, level, timestamp);
    }

    public static LastStitchedLogLine copyFrom(LogLine logLine) {
        return new LastStitchedLogLine(logLine.message(), logLine.level(), logLine.timestamp());
    }

    @Override
    public void onWritten() {
        ConnectionManager.get().use(logInstance -> {
            logInstance.getViewerComponents().clearLastViewData();
            logInstance.getLogCache().clearData();
            logInstance.onScrolled();
            //logInstance.getViewerComponents().forceUpdateCurrent();
        });
    }
}
