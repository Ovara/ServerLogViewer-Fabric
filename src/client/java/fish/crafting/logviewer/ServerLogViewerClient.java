package fish.crafting.logviewer;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.keybind.KeybindTickListener;
import fish.crafting.logviewer.keybind.OpenLogsKeybind;
import fish.crafting.logviewer.keybind.ViewPluginsKeybind;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.packet.PacketManager;
import fish.crafting.logviewer.settings.SettingsManager;
import fish.crafting.logviewer.ui.LogScreen;
import fish.crafting.logviewer.util.FileUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class ServerLogViewerClient implements ClientModInitializer {

    public static final int COMPATIBILITY_VERSION = 1;

    @Override
    public void onInitializeClient() {
        System.out.println("Initializing ServerLogViewer...");

        File dataDir = FileUtil.getDataDir();
        if(!dataDir.exists()){
            dataDir.mkdir();
        }

        ClientTickEvents.END_CLIENT_TICK.register(new KeybindTickListener(
                OpenLogsKeybind.instance,
                ViewPluginsKeybind.instance
        ));

        ClientTickEvents.END_CLIENT_TICK.register(a -> {
            LogScreen.use(LogScreen::tick);
            ConnectionManager.get().use(LogInstance::tick);
        });

        PacketManager.get();
        SettingsManager.get();
    }
}
