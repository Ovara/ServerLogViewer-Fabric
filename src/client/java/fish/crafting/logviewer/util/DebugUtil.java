package fish.crafting.logviewer.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;

public class DebugUtil {

    public static void sendMessage(String message){
        InGameHud inGameHud = MinecraftClient.getInstance().inGameHud;
        if(inGameHud == null) return;
        inGameHud.getChatHud().addMessage(Text.of(message));
    }
}
