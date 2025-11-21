package fish.crafting.logviewer.ui.component;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.util.UISounds;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.text.Text;

//#if MC>12110
import net.minecraft.client.gui.Click;
//#endif

public class LabelButtonComponent extends LabelComponent {
    private final Runnable onClick;
    @Setter @Accessors(fluent = true)
    protected boolean drawHorizontalLine = true;

    public LabelButtonComponent(Text text, Runnable onClick) {
        super(text);
        this.onClick = onClick;
    }

    @Override
    //#if MC>12110
    public boolean onMouseDown(Click click, boolean doubled) {
        UISounds.playInteractionSound();
        onClick.run();
        return true;
    }
    //#else
    //$$ public boolean onMouseDown(double mouseX, double mouseY, int button) {
    //$$     UISounds.playInteractionSound();
    //$$     onClick.run();
    //$$     return true;
    //$$ }
    //#endif

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);

        if(drawHorizontalLine){
            context.drawHorizontalLine(x - 1, x + width + 1, y + 9, 0xFFFFFFFF);
        }
    }
}
