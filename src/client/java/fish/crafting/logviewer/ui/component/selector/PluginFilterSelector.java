package fish.crafting.logviewer.ui.component.selector;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.log.LogPlugin;
import fish.crafting.logviewer.log.filter.LogFilter;
import fish.crafting.logviewer.ui.component.FancySelectorComponent;
import fish.crafting.logviewer.ui.component.VerticalFlowLayout;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PluginFilterSelector extends FancySelectorComponent<LogPlugin> {

    public PluginFilterSelector(Sizing horizontalSizing) {
        super(horizontalSizing);
    }

    @Override
    protected Component transformListComponent(VerticalFlowLayout listComponent) {
        var scroll = Containers.verticalScroll(Sizing.fill(), Sizing.fill(), listComponent);

        var scrollbarColor = 0xA0FFFFFF;
        scroll.scrollbar((context, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
            if (!active) return;

            context.fill(
                    x, y, x + width, y + height,
                    scrollbarColor & 0xFFFFFF
            );
        });

        return scroll;
    }

    @Override
    protected Sizing getExpandedSizing() {
        return Sizing.fill(75);
    }

    @Override
    protected List<LogPlugin> getValues() {
        LogInstance logInstance = ConnectionManager.get().getLogInstance();
        if(logInstance == null) return new ArrayList<>();

        return logInstance.getPlugins().values().stream().toList();
    }

    @Override
    protected void onValueChanged() {
        ConnectionManager.get().use(logInstance -> {
            logInstance.getFilter().setPlugins(value);
            logInstance.applyFilter();
        });
    }

    @Override
    protected @NotNull String getText(Set<LogPlugin> values) {
        if(values.isEmpty()) return "Plugin";
        //if(values.size() == 1) return values.stream().findFirst().get().name();
        return values.size() + " Plugins";
    }

    @Override
    protected @NotNull String getText(LogPlugin value) {
        return value.getPluginName();
    }

    @Override
    protected int getColor(Set<LogPlugin> values) {
        return 0xFFFFFFFF;
    }

    @Override
    protected void grabValues(LogFilter filter) {
        this.value.clear();
        this.value.addAll(filter.getPlugins());
    }
}
