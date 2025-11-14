package fish.crafting.logviewer.ui;

import fish.crafting.logviewer.ui.component.LabelButtonComponent;
import fish.crafting.logviewer.util.AdventureUtils;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import org.jetbrains.annotations.NotNull;

public class NotSetupScreen extends BaseOwoScreen<GridLayout> {
    @Override
    protected @NotNull OwoUIAdapter<GridLayout> createAdapter() {
        return OwoUIAdapter.create(this, (a, b) -> Containers.grid(a, b, 1, 1));
    }

    @Override
    protected void build(GridLayout rootComponent) {
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .surface(Surface.VANILLA_TRANSLUCENT);

        var text = AdventureUtils.parseMiniMessageToText("""
                <gradient:#5EB1FF:#A767FF:#E467FF>Welcome to ServerLogViewer!</gradient>
                
                <red>It looks like you haven't setup your plugin yet!
                In order to use ServerLogViewer, you must choose <u><white>who can use</u><red> the tool!
                
                
                
                <gray>Please go to the plugin's config at:
                <white>/plugins/ServerLogViewer/config.yml
                <gray>And follow the steps to setup the plugin! <dark_gray><i>(It doesn't take long!)</i>
                
                <gray>Once you're done with the config, run
                <white>/slv reload
                <gray>To load the config!
                
                
                
                <gradient:#5EB1FF:#A767FF:#E467FF>That's it! Thank you for downloading ServerLogViewer :)
                -ACraftingFish
                """);

        var label = new LabelButtonComponent(text, () -> {
        });

        label.drawHorizontalLine(false);
        label.horizontalTextAlignment(HorizontalAlignment.CENTER);
        rootComponent.child(label, 0, 0);
    }

}
