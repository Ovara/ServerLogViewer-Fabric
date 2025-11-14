package fish.crafting.logviewer.ui.component;

import fish.crafting.logviewer.ui.LogScreen;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

public class MoveToScrollButtonComponent extends LabelButtonComponent{

    private final boolean up;

    public MoveToScrollButtonComponent(boolean up) {
        super(Text.of(">"), () -> {
            LogViewerScroller<VerticalFlowLayout> scrollContainer = LogScreen.getScrollContainer();
            if(scrollContainer != null){
                if(up){
                    scrollContainer.scrollToTop();
                }else{
                    scrollContainer.scrollToBottom();
                }
            }
        });

        cursorStyle(CursorStyle.HAND);
        sizing(Sizing.fixed(9));
        this.drawHorizontalLine = false;
        this.up = up;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        Matrix3x2fStack matrices = context.getMatrices();

        matrices.pushMatrix();

        matrices.translate(0f, up ? -1 : 2);
        matrices.translateLocal(1, 0);

        matrices.translate(this.x + this.width / 2f - 1, this.y + this.height / 2f - 1);
        matrices.rotate((float) Math.toRadians(up ? -90f : 90f));
        matrices.translate(-(this.x + this.width / 2f - 1), -(this.y + this.height / 2f - 1));


        super.draw(context, mouseX, mouseY, partialTicks, delta);
        matrices.popMatrix();
    }
}
