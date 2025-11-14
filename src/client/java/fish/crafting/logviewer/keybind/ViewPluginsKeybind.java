package fish.crafting.logviewer.keybind;

import fish.crafting.logviewer.mixin.KeyBindingAccessor;
import fish.crafting.logviewer.settings.custom.PerformanceSettings;
import fish.crafting.logviewer.settings.custom.UISettings;
import fish.crafting.logviewer.ui.LogScreen;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ViewPluginsKeybind extends CustomKeybind{

    public static final ViewPluginsKeybind instance = new ViewPluginsKeybind();

    public ViewPluginsKeybind() {
        super("view_plugins", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, KeybindCategory.SLV);
    }

    @Override
    public void onPressed() {
    }

    public int getKey(){
        KeyBindingAccessor accessor = (KeyBindingAccessor) keyBinding;
        InputUtil.Key boundKey = accessor.getBoundKey();
        if(boundKey == null) return GLFW.GLFW_KEY_LEFT_ALT;

        return boundKey.getCode();
    }
}
