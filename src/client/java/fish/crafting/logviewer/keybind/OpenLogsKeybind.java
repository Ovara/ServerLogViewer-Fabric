package fish.crafting.logviewer.keybind;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.ui.IncompatibleScreen;
import fish.crafting.logviewer.ui.LogScreen;
import fish.crafting.logviewer.ui.NotSetupScreen;
import io.wispforest.owo.Owo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class OpenLogsKeybind extends CustomKeybind{

    public static final OpenLogsKeybind instance = new OpenLogsKeybind();

    private OpenLogsKeybind() {
        super("open_logs", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, KeybindCategory.SLV);
    }

    @Override
    public void onPressed() {
        if(ConnectionManager.get().isNotSetup()){
            MinecraftClient.getInstance().setScreen(new NotSetupScreen());
        }else if(ConnectionManager.get().incompatible()){
            MinecraftClient.getInstance().setScreen(new IncompatibleScreen());
        }else{
            MinecraftClient.getInstance().setScreen(new LogScreen());
        }
    }
}
