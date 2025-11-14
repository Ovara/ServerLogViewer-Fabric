package fish.crafting.logviewer.log.file;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.log.LogLevel;
import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.LogPlugin;
import fish.crafting.logviewer.log.filter.LogFilter;
import fish.crafting.logviewer.log.storage.LogCache;
import fish.crafting.logviewer.log.storage.LogChunk;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@Slf4j
public class SimpleLogIndex {

    private final ByteArrayList levels = new ByteArrayList();
    private final ShortArrayList plugins = new ShortArrayList();
    private PluginIndex additionalPluginIndex = null;
    private int lines = 0;

    public void addIndex(List<LogLine> lines){
        lines.forEach(this::addIndex);
    }

    public void addIndex(LogLine line){
        levels.add((byte) line.level().ordinal());
        plugins.add(line.plugin() == null ? -1 : line.plugin().getId());
        onIndexAdded(line, this.lines);
        this.lines++;
    }

    protected void onIndexAdded(LogLine line, int id){

    }

    public int getLevelIndex(int line){
        if(line >= lines) return 0;
        return levels.getByte(line);
    }

    public LogLevel getLevel(int line){
        if(line >= lines) return LogLevel.SYSTEM;
        LogLevel[] values = LogLevel.values();
        int ord = levels.getByte(line);
        if(ord < 0 || ord >= values.length) return LogLevel.SYSTEM;
        return values[ord];
    }

    public LogPlugin getPlugin(int line){
        AtomicReference<LogPlugin> ref = new AtomicReference<>(null);
        ConnectionManager.get().use(logInstance -> {
            ref.set(logInstance.plugin(getPluginId(line)));
        });

        return ref.get();
    }

    public short getPluginId(int line){
        if(additionalPluginIndex != null && additionalPluginIndex.fits(line)){
            return additionalPluginIndex.get(line);
        }else{
            if(line < 0 || line >= plugins.size()) return -1;

            return plugins.getShort(line);
        }
    }

    public boolean isLevel(int line, boolean[] arr){
        if(line >= lines) return false;
        int i = levels.getByte(line);
        return i < arr.length && arr[i];
    }

    public boolean isPlugin(int line, boolean[] arr){
        if(line >= lines) return false;

        int i = getPluginId(line);
        return i >= 0 && i < arr.length && arr[i];
    }

    public IntArrayList collect(Predicate<Integer> predicate){
        IntArrayList out = new IntArrayList();
        for (int i = 0; i < lines; i++) {
            if(predicate.test(i)) out.add(i);
        }

        return out;
    }

    private IntArrayList all(){
        IntArrayList out = new IntArrayList();
        for (int i = 0; i < lines; i++) {
            out.add(i);
        }

        return out;
    }

    public void writePlugins(int indexBegin, short[] ids) {
        additionalPluginIndex = new PluginIndex(indexBegin, ids);

        ConnectionManager.get().use(logInstance -> {
            LogCache<?> logCache = logInstance.getLogCache();
            for (LogChunk chunk : logCache.getChunks().values()) {
                for (LogLine logLine : chunk.getData()) {
                    if(logLine == null) break; //Maybe continue but I don't think it makes sense

                    int i = logLine.lineId();

                    if(additionalPluginIndex.fits(i)){
                        logLine.plugin(getPlugin(i));
                    }
                }
            }
        });
    }

    public IntArrayList get(LogFilter filter){
        if(filter.empty()) return all();

        Set<LogLevel> fLevels = filter.getLevels();
        Set<LogPlugin> fPlugins = filter.getPlugins();

        Predicate<Integer> predicate;

        if(fLevels.isEmpty() && fPlugins.isEmpty()) predicate = a -> true;
        else if(!fLevels.isEmpty() && !fPlugins.isEmpty()){
            boolean[] l = createLevelArray(fLevels);
            boolean[] p = createPluginArray(fPlugins);

            predicate = i -> isPlugin(i, p) && isLevel(i, l);
        }else if(!fLevels.isEmpty()){
            boolean[] l = createLevelArray(fLevels);
            predicate = i -> isLevel(i, l);
        }else{
            boolean[] p = createPluginArray(fPlugins);
            predicate = i -> isPlugin(i, p);
        }

        return collect(predicate);
    }

    private boolean[] createLevelArray(Set<LogLevel> levels){
        boolean[] arr = new boolean[LogLevel.values().length];
        for (LogLevel level : levels) {
            arr[level.ordinal()] = true;
        }

        return arr;
    }

    private boolean[] createPluginArray(Set<LogPlugin> plugins){
        LogInstance logInstance = ConnectionManager.get().getLogInstance();
        if(logInstance == null) return new boolean[0];

        boolean[] arr = new boolean[logInstance.getPlugins().size()];
        for (LogPlugin plugin : plugins) {
            if(plugin.getId() >= arr.length) continue;
            arr[plugin.getId()] = true;
        }

        return arr;
    }

    protected record PluginIndex(int beginIndex, short[] ids) {
        public boolean fits(int index){
            return index >= beginIndex && index < (beginIndex + ids.length);
        }

        public short get(int index){
            return ids[index - beginIndex];
        }
    }
}
