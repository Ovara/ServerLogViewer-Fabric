package fish.crafting.logviewer.ui;

import fish.crafting.logviewer.ServerLogViewerClient;
import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.ui.component.LabelButtonComponent;
import fish.crafting.logviewer.ui.component.SingleGridLayout;
import fish.crafting.logviewer.util.TextUtil;
import io.netty.channel.epoll.Epoll;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

public class IncompatibleScreen extends BaseOwoScreen<GridLayout> {
    @Override
    protected @NotNull OwoUIAdapter<GridLayout> createAdapter() {
        return OwoUIAdapter.create(this, (a, b) -> Containers.grid(a, b, 1, 1));
    }

    @Override
    protected void build(GridLayout rootComponent) {
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .surface(Surface.VANILLA_TRANSLUCENT);

        int pluginVersion = ConnectionManager.get().getPluginVersion();
        int modVersion = ServerLogViewerClient.COMPATIBILITY_VERSION;

        Text text = pluginVersion < modVersion ? getPluginOutdatedText(pluginVersion) : getModOutdatedText(pluginVersion);
        String url = pluginVersion < modVersion ? "https://slvplugin.crafting.fish" : "https://slvmod.crafting.fish";
        var label = new LabelButtonComponent(text, () -> {
            UISounds.playInteractionSound();
            Util.getOperatingSystem().open(url);
        });

        label.drawHorizontalLine(false);
        label.horizontalTextAlignment(HorizontalAlignment.CENTER);
        rootComponent.child(label, 0, 0);
    }

    private Text getPluginOutdatedText(int version){
        int diff = Math.abs(version - ServerLogViewerClient.COMPATIBILITY_VERSION);
        return TextUtil.colored(
                "Incompatible ServerLogViewer versions!" +
                        "\n" +
                        "\nThis server's SLV Plugin is " + diff + " compatibility version(s) behind!" +
                        "\nPlease update the plugin or downgrade your mod in order to view logs!" +
                        "\n\nClick to open the website and download!"
                , Formatting.RED);
    }

    private Text getModOutdatedText(int version){
        int diff = Math.abs(version - ServerLogViewerClient.COMPATIBILITY_VERSION);
        return TextUtil.colored(
                "Incompatible ServerLogViewer versions!" +
                        "\n" +
                        "\nYour ServerLogViewer mod is " + diff + " compatibility version(s) behind!" +
                        "\nPlease update your mod in order to view logs!" +
                        "\n\nClick to open the website and download!"
                , Formatting.RED);
    }

}
