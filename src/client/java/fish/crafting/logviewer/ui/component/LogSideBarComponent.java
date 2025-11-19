package fish.crafting.logviewer.ui.component;

import fish.crafting.logviewer.log.LogLine;
import fish.crafting.logviewer.settings.custom.UISettings;
import fish.crafting.logviewer.ui.LogScreen;
import fish.crafting.logviewer.util.ColorUtil;
import fish.crafting.logviewer.util.KeyUtil;
import fish.crafting.logviewer.util.TextUtil;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class LogSideBarComponent extends LabelComponent {
    private final LogLine logLine;
    private boolean simpleView = true;
    private boolean shouldBeSimpleView = !KeyUtil.isViewPluginsKeyPressed();
    private final AnimatableProperty<Color> fillColor = AnimatableProperty.of(Color.ofArgb(0x00000000));

    public LogSideBarComponent(LogLine logLine) {
        super(Text.of(" "));
        sizing(Sizing.content(), Sizing.fill());

        this.logLine = logLine;
    }

    public boolean changeView(boolean plugins){
        this.shouldBeSimpleView = plugins;
        if(shouldAnimate()){
            this.simpleView = this.shouldBeSimpleView;
            update(null, true);
            return true;
        }

        return false;
    }

    private boolean shouldAnimate(){
        LogViewerScroller<VerticalFlowLayout> c = LogScreen.getScrollContainer();
        if(c == null) return false;

        int screenHeight = c.y() + c.height() + 200;
        return y > -200 && y < screenHeight;
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        fillColor.update(delta);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if(shouldBeSimpleView != simpleView){
            simpleView = shouldBeSimpleView;
            update(null, false);
        }

        super.draw(context, mouseX, mouseY, partialTicks, delta);

        if(fillColor.get().alpha() > 0.05f){
            context.fill(x, y, x + width, y + height, fillColor.get().argb());
        }

    }

    public void update(@Nullable SingleGridLayout initParent, boolean animate){
        if(simpleView){
            Text text = TextUtil.colored(logLine.lineId() + "", Formatting.DARK_GRAY);
            text(text);
        }else if(logLine.plugin() != null){
            String str = logLine.plugin().getPluginName();
            if(UISettings.get().PLUGINS_AND_LINE_NUM.get()){
                str = logLine.lineId() + " " + str;
            }

            Text text = TextUtil.colored(str, TextColor.fromRgb(logLine.plugin().getMainColor()));
            text(text);
        }

        SingleGridLayout singleGridLayout = initParent;
        if(singleGridLayout == null && parent instanceof SingleGridLayout s){
            singleGridLayout = s;
        }

        if(singleGridLayout == null) return;

        Sizing sizing;
        int color;
        if(simpleView){
            int numbersSize = ("" + logLine.lineId()).length() * 6; //For 1M and above, increase size
            sizing = Sizing.fixed(Math.max(30, numbersSize));
            color = logLine.plugin() == null ? 0x00FFFFFF : ColorUtil.alpha(logLine.plugin().getAlphaColor(), 0);
        }else{
            sizing = Sizing.fixed(textRenderer.getWidth(text) + 5); //content() doesn't work for animations
            color = logLine.plugin() == null ? 0x00FFFFFF : logLine.plugin().getAlphaColor();
        }

        if(!animate){
            singleGridLayout.horizontalSizing().set(sizing);
            fillColor.set(Color.ofArgb(color));
        }else{
            singleGridLayout.horizontalSizing().animate(200, Easing.QUADRATIC, sizing).forwards();

            if(simpleView){
                fillColor.animate(0, Easing.QUADRATIC, Color.ofArgb(color)).forwards();
            }else{
                fillColor.animate(200, Easing.QUADRATIC, Color.ofArgb(color)).forwards();
            }
        }
    }
}
