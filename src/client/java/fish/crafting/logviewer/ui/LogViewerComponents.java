package fish.crafting.logviewer.ui;

import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.file.LogFileManager;
import fish.crafting.logviewer.log.storage.LogCache;
import fish.crafting.logviewer.log.storage.LogChunk;
import fish.crafting.logviewer.log.storage.SmartLogCache;
import fish.crafting.logviewer.ui.component.OpenGridLayout;
import fish.crafting.logviewer.ui.component.VerticalFlowLayout;
import fish.crafting.logviewer.util.DebugUtil;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Sizing;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LogViewerComponents {

    @Getter
    private final LogCache<?> logCache;
    private @NotNull
    final OpenGridLayout previous, next;
    @Getter
    private ChunkVerticalFlow current = null;
    @Getter
    private final VerticalFlowLayout main;
    private int prevPrev = -1, prevNext = -1;
    private int prevViewIndex0 = -1, prevViewIndex1 = -1;

    public LogViewerComponents(LogCache<?> logCache){
        this.logCache = logCache;

        this.main = new VerticalFlowLayout(Sizing.fill(99), Sizing.content());
        this.previous = buildEmptyBlock();
        this.current = buildCurrent();
        this.next = buildEmptyBlock();

        main.children(List.of(previous, current, next));
    }

    public void update(){
        update(false);
    }

    public void update(boolean checkLatest){
        var scroll = LogScreen.getScrollContainer();
        double oldScroll = scroll == null ? 0d : scroll.getScrollOffset();

        updateStructure();
        updateCurrent(checkLatest);

        if(scroll != null) scroll.scrollToOffset(oldScroll);
    }

    private void updateStructure(){
        logCache.update();

        int viewIndex0 = logCache.getMinRange().get();
        int viewIndex1 = logCache.getMaxRange().get();

        int previousSize, nextSize;
        if(viewIndex0 == -1 || viewIndex1 == -1) {
            previousSize = logCache.getLineAmount() * LogLine.LINE_COMPONENT_HEIGHT;
            nextSize = 0;
        }else{
            int chunkSize = logCache.getChunkSize();
            previousSize = viewIndex0 * chunkSize * LogLine.LINE_COMPONENT_HEIGHT;
            nextSize = (logCache.getLineAmount() - (viewIndex1 + 1) * chunkSize) * LogLine.LINE_COMPONENT_HEIGHT;
            if(nextSize < 0) nextSize = 0;
        }

        if(prevPrev != previousSize){
            prevPrev = previousSize;
            previous.verticalSizing(Sizing.fixed(previousSize));
        }

        if(prevNext != nextSize){
            prevNext = nextSize;
            next.verticalSizing(Sizing.fixed(nextSize));
        }
    }

    private OpenGridLayout buildEmptyBlock(){
        return new OpenGridLayout(Sizing.fill(), Sizing.fixed(0), 1, 1);
    }

    private ChunkVerticalFlow buildCurrent(){
        return new ChunkVerticalFlow(Sizing.fill(), Sizing.content());
    }

    public void forceUpdateCurrent(){
        if(current == null) return;
        synchronized (current.comps){
            for (LoadedChunk comp : current.comps) {
                comp.buildComponent();
            }
        }
    }

    public void updateIndex(int index){
        if(current != null){
            current.updateChunk(index);
        }
    }

    public void clearCurrent(){
        current.clearAll();
    }

    private void updateCurrent(boolean checkLatest){
        int viewIndex0 = logCache.getMinRange().get();
        int viewIndex1 = logCache.getMaxRange().get();


        if(prevViewIndex0 == viewIndex0 && prevViewIndex1 == viewIndex1) {
            if(checkLatest){ //Looking at the latest lines
                current.updateLatest();
            }

            return;
        }

        current.updateViewSpace(viewIndex0, viewIndex1);

        prevViewIndex0 = viewIndex0;
        prevViewIndex1 = viewIndex1;
    }

    public void clearLastViewData() {
        this.prevViewIndex0 = this.prevViewIndex1 = -1;
    }

    public class ChunkVerticalFlow extends VerticalFlowLayout {

        private final List<LoadedChunk> comps = new ArrayList<>();

        public ChunkVerticalFlow(Sizing horizontalSizing, Sizing verticalSizing) {
            super(horizontalSizing, verticalSizing);
        }

        public void clearAll() {
            synchronized (comps){
                comps.clear();
                clearChildren();
            }
        }

        /**
         * @param index0 First Index of the logChunk
         * @param index1 Last Index of the logChunk
         */
        private void updateViewSpace(int index0, int index1){
            List<LoadedChunk> chunksToRemove = new ArrayList<>();
            synchronized (comps){
                Iterator<LoadedChunk> iterator = comps.iterator();

                while(iterator.hasNext()){
                    LoadedChunk comp = iterator.next();
                    if(comp.index < index0 || comp.index > index1) {
                        iterator.remove();
                        chunksToRemove.add(comp);
                    }
                }

                configure(c -> {
                    chunksToRemove.forEach(chunk -> this.removeChild(chunk.component));

                    for(int v = index0; v <= index1; v++){
                        LoadedChunk loadedChunk = new LoadedChunk(v);

                        if(comps.isEmpty()){
                            child(loadedChunk.component);
                            comps.add(loadedChunk);
                        }else{
                            LoadedChunk first = comps.getFirst();
                            LoadedChunk last = comps.getLast();

                            if(v < first.index){
                                child(0, loadedChunk.component);
                                comps.addFirst(loadedChunk);
                            }else if(v > last.index){
                                child(loadedChunk.component);
                                comps.add(loadedChunk);
                            }
                        }
                    }
                });
            }
        }

        public void updateChunk(int index){
            LogInstance.runOnUIThread(() -> {
                synchronized (comps){
                    for (LoadedChunk c : comps) {
                        if (c.index == index) {
                            c.buildComponent();
                            break;
                        }
                    }
                }
            });
        }

        public void updateLatest() {
            synchronized (comps){
                if(comps.isEmpty()) return;
                LoadedChunk last = comps.getLast();
                last.buildIfDirty();
            }
        }
    }

    private final class LoadedChunk {
        private final int index;
        private final VerticalFlowLayout component;

        private LoadedChunk(int index) {
            this.index = index;
            this.component = new VerticalFlowLayout(Sizing.fill(), Sizing.content());
            buildComponent();
        }

        public void buildIfDirty(){
            LogChunk logChunk = logCache.getLogChunk(index);
            if(logChunk != null && logChunk.dirty) buildComponent();
        }

        private void buildComponent(){
            this.component.configure(c -> {
                LogChunk logChunk = logCache.getLogChunk(index);
                if(logChunk == null){
                    component.clearChildren();
                    component.child(Components.box(Sizing.fill(), Sizing.fill()).color(Color.ofArgb(0x00000000)));
                }else{
                    logChunk.dirty = false;
                    component.clearChildren();
                    component.children(logChunk.compile());
                }
            });
        }

    }

}
