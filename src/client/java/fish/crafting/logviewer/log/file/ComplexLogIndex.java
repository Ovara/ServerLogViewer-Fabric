package fish.crafting.logviewer.log.file;

import fish.crafting.logviewer.log.LogLevel;
import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.LogPlugin;
import fish.crafting.logviewer.log.filter.LogFilter;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ComplexLogIndex extends SimpleLogIndex {

    private final Map<LogLevel, IntArrayList> levelIndex = new EnumMap<>(LogLevel.class);
    private final Map<Short, IntOpenHashSet> pluginIndex = new HashMap<>();
    private final IntArrayList all = new IntArrayList();

    @Override
    protected void onIndexAdded(LogLine line, int id) {
        levelIndex.computeIfAbsent(line.level(), e -> new IntArrayList()).add(id);

        LogPlugin plugin = line.plugin();
        if(plugin != null){
            pluginIndex.computeIfAbsent(plugin.getId(), e -> new IntOpenHashSet()).add(id);
        }

        all.add(id);
    }

    @Override
    public void writePlugins(int indexBegin, short[] ids) {
        //super.writePlugins(indexBegin, ids);
        int i = indexBegin;
        for (short id : ids) {
            pluginIndex.computeIfAbsent(id, e -> new IntOpenHashSet()).add(i);
            i++;
        }
    }

    @Override
    public IntArrayList get(LogFilter filter) {
        Set<LogLevel> levels = filter.getLevels();
        Set<LogPlugin> plugins = filter.getPlugins();

        if(levels.isEmpty() && plugins.isEmpty()) return all;

        IntArrayList l = levels.isEmpty() ? null : gatherLevels(levels);
        IntArrayList p = plugins.isEmpty() ? null : gatherPlugins(plugins);

        return overlap(l, p);
    }

    private IntArrayList gatherLevels(Set<LogLevel> levels){
        IntArrayList out = new IntArrayList();

        for (LogLevel level : levels) {
            IntArrayList integers = levelIndex.get(level);
            if(integers != null) out.addAll(integers);
        }

        return out;
    }

    private IntArrayList gatherPlugins(Set<LogPlugin> plugins){
        IntArrayList out = new IntArrayList();

        for (LogPlugin plugin : plugins) {
            IntOpenHashSet integers = pluginIndex.get(plugin.getId());
            if(integers != null) out.addAll(integers);
        }

        return out;
    }

    private IntArrayList overlap(IntArrayList a, IntArrayList b){
        if(a == null) return b;
        if(b == null) return a;

        IntArrayList smaller = a.size() <= b.size() ? a : b;
        IntArrayList larger  = a.size() > b.size() ? a : b;

        IntOpenHashSet set = new IntOpenHashSet(smaller);
        IntArrayList result = new IntArrayList(Math.min(a.size(), b.size()));

        for (int i = 0; i < larger.size(); i++) {
            int val = larger.getInt(i);
            if (set.remove(val)) { // remove ensures unique results
                result.add(val);
            }
        }

        return result;
    }
}
