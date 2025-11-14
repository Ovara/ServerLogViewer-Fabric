package fish.crafting.logviewer.ui.component;

import fish.crafting.logviewer.ui.LogScreen;
import fish.crafting.logviewer.util.ColorUtil;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.AnimatableProperty;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.text.Text;

public class LogLineComponent extends LabelComponent {

    private AnimatableProperty<Color> color = AnimatableProperty.of(Color.ofArgb(0x00000000));

    public LogLineComponent(Text text, int id) {
        super(text);
        id("line" + id);

        mouseEnter().subscribe(() -> {
            color.animate(20, Easing.CUBIC, Color.ofArgb(0x33FFFFFF)).forwards();
            LogScreen.use(logScreen -> logScreen.lastHoveredLineComponent = this);
        });

        mouseLeave().subscribe(() -> {
            color.animate(200, Easing.CUBIC, Color.ofArgb(0x00FFFFFF)).forwards();
        });
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        color.update(delta);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);

        Color c = color.get();
        if(ColorUtil.getAlpha(c.argb()) >= 2) {
            context.fill(0, y, x + width, y + width, c.argb());
        }
    }

    public void highlight() {
        color.set(Color.ofArgb(0x5500FF00));
        color.animate(2500, Easing.EXPO, Color.ofArgb(0x002B6B17)).forwards();
    }
}
