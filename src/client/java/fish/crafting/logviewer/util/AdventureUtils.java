package fish.crafting.logviewer.util;

import net.kyori.adventure.platform.modcommon.MinecraftAudiences;
import net.kyori.adventure.platform.modcommon.MinecraftClientAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.Text;

public class AdventureUtils {

    public static Text parseMiniMessageToText(String message){
        Component comp = MiniMessage.miniMessage().deserialize(message);
        return MinecraftClientAudiences.of().asNative(comp);
    }

}
