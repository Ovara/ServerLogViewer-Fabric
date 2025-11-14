package fish.crafting.logviewer.log;

import fish.crafting.logviewer.log.file.LogFileManager;
import fish.crafting.logviewer.log.file.SimpleLogIndex;
import fish.crafting.logviewer.log.filter.LogFilter;
import fish.crafting.logviewer.log.filter.LogSearchQuery;
import fish.crafting.logviewer.log.stitch.LastStitchedLogLine;
import fish.crafting.logviewer.log.stitch.LogStitchingInstance;
import fish.crafting.logviewer.log.storage.FilteredLogCache;
import fish.crafting.logviewer.log.storage.LogCache;
import fish.crafting.logviewer.log.storage.LogChunk;
import fish.crafting.logviewer.log.storage.SmartLogCache;
import fish.crafting.logviewer.packet.PacketManager;
import fish.crafting.logviewer.ui.LogScreen;
import fish.crafting.logviewer.ui.LogViewerComponents;
import fish.crafting.logviewer.ui.component.LogViewerScroller;
import fish.crafting.logviewer.ui.component.VerticalFlowLayout;
import fish.crafting.logviewer.util.DebugUtil;
import fish.crafting.logviewer.util.Logs;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;

import java.util.*;

/**
 * A running connection (instance) to an SLV plugin
 */
public class LogInstance {

    @Getter
    private final UUID id;
    private int ticks = 0;

    //Used for gathering already existing logs from the server when the user connects
    private LogStitchingInstance stitchingInstance;
    //Lines that are received during the stitching phase (No log has been built yet)
    private final List<LogLine> pendingLines = new ArrayList<>();
    @Getter
    private LogCache<?> logCache;
    @Getter
    private LogViewerComponents viewerComponents;
    @Getter
    private final Map<Short, LogPlugin> plugins = new HashMap<>();
    @Getter
    private final LogFilter filter = new LogFilter();
    @Getter
    private final LogSearchQuery searchQuery = new LogSearchQuery();
    private Thread searcherThread = null;

    public LogInstance(UUID id) {
        this.id = id;
        this.logCache = new SmartLogCache();
        this.viewerComponents = new LogViewerComponents(logCache);
    }

    public void beginLogStitch(int parts) {
        LogFileManager.get().shutdown();

        stitchingInstance = new LogStitchingInstance(parts);
        pendingLines.clear();
        onPluginsChanged();
        LogScreen.use(LogScreen::rebuildLogs);

        LogFileManager.get().startWriterThread();
    }

    public LogPlugin plugin(short id){
        if(id == -1) return null;
        return plugins.computeIfAbsent(id, LogPlugin::new);
    }

    public void tick() {
        if(ticks++ >= 20){
            ticks = 0;
            logCache.removeDecayedChunks();
        }
    }

    public void registerPlugins(Map<String, Short> plugins) {
        plugins.forEach((name, id) -> {
            if(id == -1) return;
            plugin(id).resolvePlugin(name);
        });

        for (LogChunk chunk : logCache.getChunks().values()) {
            SimpleLogIndex logIndex = LogFileManager.get().getLogIndex();

            for (LogLine line : chunk.getData()) {
                if(line != null){
                    int i = line.lineId();
                    line.plugin(logIndex.getPlugin(i));
                }
            }
        }

        LogScreen.use(LogScreen::updatePlugins);
    }

    //This stupid method caused so many errors that I don't even know what is necessary and what isn't
    //for it to work correctly. Let that be a lesson in using third party UI libs.
    public void applyFilter(){
        boolean currentlyViewingFiltered = logCache instanceof FilteredLogCache;
        boolean shouldFilter = !filter.empty() || searchQuery.hasQuery();

        if(!currentlyViewingFiltered && !shouldFilter) return;

        var oldComponents = viewerComponents;
        if(shouldFilter){
            int searchId = this.searchQuery.getSearchId();
            if(searcherThread != null) searcherThread.interrupt();

            //todo only use threading if there is a search query, instead runFilterLogic() instantly
            if(logCache instanceof FilteredLogCache filteredLogCache){
                setSearcherThread(searchId, filteredLogCache);
            }else{
                var filteredLogCache = new FilteredLogCache(new IntArrayList());

                logCache.shutdown();
                logCache = filteredLogCache;
                viewerComponents = new LogViewerComponents(logCache);

                setSearcherThread(searchId, filteredLogCache);
            }

            searcherThread.start();
        }else{
            logCache.shutdown();
            logCache = new SmartLogCache();
            viewerComponents = new LogViewerComponents(logCache);

            runOnUIThread(() -> {
                viewerComponents.update();
            });
        }

        if(viewerComponents != oldComponents){
            LogScreen.use(LogScreen::rebuildLogs);
        }
    }

    private void setSearcherThread(int searchId, FilteredLogCache filteredLogCache){
        searcherThread = new Thread(() -> {
            IntArrayList filtered = filterLines();
            if(filtered != null && this.searchQuery.matchesQuery(searchId)){
                runFilterLogic(filteredLogCache, filtered);
            }
        });
    }

    private void runFilterLogic(FilteredLogCache filteredLogCache, IntArrayList filtered){
        runOnUIThread(() -> {
            filteredLogCache.updateLines(filtered);
            viewerComponents.update();
            var scroll = LogScreen.getScrollContainer();
            if(scroll != null) scroll.clampScroll();
        });
    }
    //kill me

    public static void runOnUIThread(Runnable runnable){
        MinecraftClient.getInstance().execute(() -> {
            MinecraftClient.getInstance().submit(runnable);
        });
    }

    private IntArrayList filterLines(){
        IntArrayList integers = LogFileManager.get().getLogIndex().get(filter);
        if(searchQuery.hasQuery()){
            try {
                integers = searchQuery.search(integers);
            }catch(InterruptedException interruptedException){
                return null;
            } catch (Exception e) {
                Logs.logError("Error while searching logs!", e);
            }
        }

        return integers;
    }

    private void onPluginsChanged(){
        LogScreen.use(LogScreen::updatePlugins);
    }

    public void shutdown() {
        logCache.shutdown();
        plugins.clear();
        onPluginsChanged();
    }

    public void handleStitchPart(int stitchIndex, byte[] bytes) {
        if(stitchingInstance == null) return;
        stitchingInstance.addData(stitchIndex, bytes);

        if(stitchingInstance.finished()){
            completeStitching();

            stitchingInstance = null;
            LogScreen.use(LogScreen::rebuildLogs);
        }
    }

    private void completeStitching(){
        List<LogLine> stitch = stitchingInstance.stitch();

        stitch.addAll(pendingLines);

        LogLine lastLine = stitch.removeLast();
        lastLine = LastStitchedLogLine.copyFrom(lastLine);
        stitch.add(lastLine);

        int i = 0;
        for (LogLine logLine : stitch) {
            logLine.lineId(i++);
        }


        pendingLines.clear();
        LogFileManager.get().write(stitch);

        //This will tell the server to send us index info about the logs
        PacketManager.C2S_FINISHED_STITCHING.sendPacket();
    }

    public void addLines(List<LogLine> lines) {
        if(stitchingInstance != null){
            pendingLines.addAll(lines);
        }else{
            int lineIndex = LogFileManager.get().getLineAmount();
            for (LogLine line : lines) {
                line.lineId(lineIndex++);
            }

            logCache.addLines(searchQuery.filterLines(filter.filter(lines)));
            LogFileManager.get().write(lines);

            var scroll = LogScreen.getScrollContainer();
            boolean wasAtBottom = scroll != null && scroll.isAtBottom();

            viewerComponents.update(true);

            if (wasAtBottom) {
                MinecraftClient.getInstance().execute(scroll::scrollToBottom);
            }
        }
    }

    public boolean isStitching(){
        return stitchingInstance != null;
    }

    public void onScrolled() {
        viewerComponents.update();
    }
}
