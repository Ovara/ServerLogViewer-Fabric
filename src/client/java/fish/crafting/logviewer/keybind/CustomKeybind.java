package fish.crafting.logviewer.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.jetbrains.annotations.NotNull;

public abstract class CustomKeybind {

    public final KeyBinding keyBinding;

    public CustomKeybind(@NotNull String keyID,
                         @NotNull InputUtil.Type type, int code,
                         @NotNull KeybindCategory category){
        this.keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "keybind.slv." + keyID,
                type,
                code,
                category.translation()
        ));
    }

    public abstract void onPressed();

}
