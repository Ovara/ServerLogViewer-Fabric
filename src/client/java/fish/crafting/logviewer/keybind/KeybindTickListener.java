package fish.crafting.logviewer.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class KeybindTickListener implements ClientTickEvents.EndTick {

    private final CustomKeybind[] keybinds;
    private int totalTicks = 0;

    public KeybindTickListener(CustomKeybind... keybinds){
        this.keybinds = keybinds;
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        for (CustomKeybind keybind : keybinds) {
            if(keybind.keyBinding.wasPressed()) {
                keybind.onPressed();
            }
        }

        totalTicks++;

    }
}
