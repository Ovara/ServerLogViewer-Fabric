package fish.crafting.logviewer.ui.component;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.packet.PacketManager;
import fish.crafting.logviewer.util.TextUtil;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class OPCheckComponent extends BoxComponent {

    private static long lastReload = 0L;
    private static final Text DOT = TextUtil.colored(".", Formatting.DARK_GRAY);
    private static final Text RELOAD = TextUtil.colored("Reload", Formatting.DARK_GRAY);

    public OPCheckComponent() {
        super(Sizing.fixed(50), Sizing.fixed(10));
        //#if MC>12110
        mouseDown().subscribe((click, doubled) -> {
        //#else
        //$$ mouseDown().subscribe((a, b, c) -> {
        //#endif
            reload();
            return true;
        });
        margins(Insets.top(10));
    }

    public void reload(){
        if(!isOnCooldown()){
            lastReload = System.currentTimeMillis();
            PacketManager.C2S_REQUEST_HANDSHAKE.sendPacket();
        }
    }

    private boolean isOnCooldown(){
        long l = System.currentTimeMillis();
        long d = l - lastReload;
        return d < 10000;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        long l = System.currentTimeMillis();
        long d = l - lastReload;

        int centerX = x + width / 2;

        if(isOnCooldown()){
            int i = Math.toIntExact((d / 500) % 3);
            for (int j = 0; j <= i; j++) {
                int x = centerX + (j - 1) * 5;
                context.drawText(MinecraftClient.getInstance().textRenderer, DOT, x, this.y, 0xFFFFFFFF, true);
            }
        }else{
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int width1 = textRenderer.getWidth(RELOAD);
            context.drawText(textRenderer, RELOAD, centerX - width1 / 2, this.y, 0xFFFFFFFF, true);
        }
    }
}
