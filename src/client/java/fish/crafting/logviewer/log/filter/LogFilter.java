package fish.crafting.logviewer.log.filter;

import fish.crafting.logviewer.log.LogLevel;
import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.log.LogPlugin;
import fish.crafting.logviewer.log.file.LogFileManager;
import fish.crafting.logviewer.log.file.SimpleLogIndex;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Getter @Setter
public class LogFilter {

    private Set<LogLevel> levels = new HashSet<>();
    private Set<LogPlugin> plugins = new HashSet<>();

    public void setLevels(Set<LogLevel> value) {
        this.levels = new HashSet<>(value);
    }

    public boolean empty(){
        return levels.isEmpty() && plugins.isEmpty();
    }

    public int count(){
        int c = 0;
        if(!levels.isEmpty()) c++;
        if(!plugins.isEmpty()) c++;
        return c;
    }

    public List<LogLine> filter(List<LogLine> lines) {
        boolean l = levels.isEmpty();
        boolean p = plugins.isEmpty();

        if(l && p) return lines;

        return lines
                .stream()
                .filter(line ->
                           (l || levels.contains(line.level()))
                        && (p || plugins.contains(line.plugin())))
                .toList();
    }
}
