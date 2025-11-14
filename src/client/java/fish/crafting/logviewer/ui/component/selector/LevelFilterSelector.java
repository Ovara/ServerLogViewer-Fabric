package fish.crafting.logviewer.ui.component.selector;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogLevel;
import fish.crafting.logviewer.log.filter.LogFilter;
import fish.crafting.logviewer.ui.component.FancySelectorComponent;
import io.wispforest.owo.ui.core.Sizing;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class LevelFilterSelector extends FancySelectorComponent<LogLevel> {

    public LevelFilterSelector(Sizing horizontalSizing) {
        super(horizontalSizing);
    }

    @Override
    protected Sizing getExpandedSizing() {
        return Sizing.content();
    }

    @Override
    protected List<LogLevel> getValues() {
        return Arrays.stream(LogLevel.values()).toList();
    }

    @Override
    protected void onValueChanged() {
        ConnectionManager.get().use(logInstance -> {
            logInstance.getFilter().setLevels(value);
            logInstance.applyFilter();
        });
    }

    @Override
    protected @NotNull String getText(Set<LogLevel> values) {
        if(values.isEmpty()) return "Level";
        //if(values.size() == 1) return values.stream().findFirst().get().name();
        return values.size() + " Levels";
    }

    @Override
    protected @NotNull String getText(LogLevel value) {
        return value.getProperName();
    }

    @Override
    protected int getColor(Set<LogLevel> values) {
        if(values.isEmpty()) return 0xFFFFFFFF;

        int ord = -1;
        LogLevel v = LogLevel.SYSTEM;
        for (LogLevel value : values) {
            if(value.ordinal() > ord) {
                ord = value.ordinal();
                v = value;
            }
        }
        return v.getColor();
    }

    @Override
    protected void grabValues(LogFilter filter) {
        this.value.clear();
        this.value.addAll(filter.getLevels());
    }
}
