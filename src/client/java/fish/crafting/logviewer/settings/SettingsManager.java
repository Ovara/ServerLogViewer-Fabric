package fish.crafting.logviewer.settings;

import fish.crafting.logviewer.settings.custom.PerformanceSettings;
import fish.crafting.logviewer.settings.custom.UISettings;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.*;

public class SettingsManager {

    private static SettingsManager instance;
    private final Map<SettingsCategory, SettingsProvider> providers = new HashMap<>();

    private SettingsManager(){
        instance = this;

        register(SettingsCategory.UI, UISettings.get());
        register(SettingsCategory.PERFORMANCE, PerformanceSettings.get());

        ClientTickEvents.START_CLIENT_TICK.register(new ClientTickEvents.StartTick() {
            private int ticks = 0;

            @Override
            public void onStartTick(MinecraftClient client) {
                if(ticks++ % 20 == 0) { //Just run it every 1s
                    providers.values().forEach(SettingsProvider::trySaving);
                }
            }
        });
    }

    public void saveAllDirty() {
        for (SettingsProvider value : providers.values()) {
            value.trySaving(true);
        }
    }

    private void register(SettingsCategory category, SettingsProvider provider){
        this.providers.put(category, provider);
        provider.load();
    }

    public static SettingsManager get(){
        return instance == null ? new SettingsManager() : instance;
    }

    public SettingsProvider getProvider(SettingsCategory selected) {
        return providers.get(selected);
    }
}
