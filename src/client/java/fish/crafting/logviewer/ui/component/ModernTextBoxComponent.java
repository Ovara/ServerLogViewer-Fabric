package fish.crafting.logviewer.ui.component;

import fish.crafting.logviewer.util.TextUtil;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModernTextBoxComponent extends TextBoxComponent {

    @Setter @Accessors(fluent = true)
    protected String previewText = null;

    public ModernTextBoxComponent(Sizing horizontalSizing) {
        super(horizontalSizing);
        setDrawsBackground(false);
        setMaxLength(1000);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderWidget(context, mouseX, mouseY, deltaTicks);
        if(this.isVisible()){
            context.drawHorizontalLine(getX() - 1, getX() + getWidth() + 1, getY() + 9, 0xFFFFFFFF);

            if(previewText != null && !this.isFocused() && this.getText().isEmpty()){
                Text text = TextUtil.colored(previewText, Formatting.GRAY);
                text = text.getWithStyle(text.getStyle().withItalic(true)).getFirst();

                context.drawText(
                        MinecraftClient.getInstance().textRenderer,
                        text,
                        getX(),
                        getY(),
                        0xFFFFFFFF,
                        true
                );
            }
        }
    }
}
