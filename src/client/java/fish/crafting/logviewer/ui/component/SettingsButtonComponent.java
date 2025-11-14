package fish.crafting.logviewer.ui.component;

import fish.crafting.logviewer.ui.SettingsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class SettingsButtonComponent extends LabelButtonComponent{

    public SettingsButtonComponent() {
        super(Text.of("Settings"), () -> MinecraftClient.getInstance().setScreen(new SettingsScreen()));
        drawHorizontalLine = true;
    }
}
