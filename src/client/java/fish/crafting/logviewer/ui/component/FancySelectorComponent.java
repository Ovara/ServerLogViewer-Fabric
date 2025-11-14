package fish.crafting.logviewer.ui.component;

import fish.crafting.logviewer.log.filter.LogFilter;
import fish.crafting.logviewer.util.ColorUtil;
import fish.crafting.logviewer.util.TextUtil;
import io.wispforest.owo.config.annotation.Expanded;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class FancySelectorComponent<T> extends VerticalFlowLayout {

    protected final Set<T> value = new HashSet<>();
    private boolean expanded = true;
    private final Button button;
    private final List<ValueButton> valueButtons = new ArrayList<>();
    @Setter @Accessors(fluent = true)
    private boolean oneSelected = true;
    protected final VerticalFlowLayout listComponent;

    public FancySelectorComponent(Sizing horizontalSizing) {
        super(horizontalSizing, Sizing.content());
        this.listComponent = new VerticalFlowLayout(Sizing.fill(), Sizing.content());

        updateAvailableValues();
        updateExpanded(true);

        this.button = new Button();
        this.button.updateValue();
        this.button.mouseDown().subscribe((x, y, btn) -> {
            UISounds.playInteractionSound();
            expanded = !expanded;
            updateExpanded(false);
            return true;
        });

        child(button);
        child(transformListComponent(listComponent));
    }

    protected Component transformListComponent(VerticalFlowLayout listComponent){
        return listComponent;
    }

    private void updateExpanded(boolean instant){
        Sizing sizing;
        if(expanded){
            sizing = getExpandedSizing();
        }else{
            sizing = Sizing.fixed(button.height() + 4);
        }

        if(instant){
            verticalSizing(sizing);
        }else{
            verticalSizing().animate(500, Easing.CUBIC, sizing).forwards();
        }
    }

    protected abstract Sizing getExpandedSizing();

    protected void updateAllButtons(){
        valueButtons.forEach(ValueButton::update);
        button.updateValue();
    }

    protected void onValueChanged(){

    }

    private Component createValueComponent(ValueButton btn){
        var comp = new HorizontalFlowLayout(Sizing.fill(), Sizing.content());
        comp.margins(Insets.of(2, 2, 2, 0));
        comp.child(btn);
        T stored = btn.stored;
        LabelComponent label = Components.label(TextUtil.colored(" " + getText(stored), TextColor.fromRgb(getColor(Set.of(stored)))));

        label.cursorStyle(CursorStyle.HAND);
        label.margins(Insets.top(1));
        label.mouseDown().subscribe((x, y, b) -> {
            onClick(stored);
            return true;
        });

        btn.cursorStyle(CursorStyle.HAND);
        btn.mouseDown().subscribe((x, y, b) -> {
            onClick(stored);
            return true;
        });

        comp.child(label);
        return comp;
    }

    private void onClick(T stored){
        UISounds.playInteractionSound();
        if(value.contains(stored)) value.remove(stored);
        else {
            if(oneSelected) value.clear();
            value.add(stored);
        }
        onValueChanged();
        updateAllButtons();
    }

    public void updateAvailableValues(){
        valueButtons.clear();
        for (T availableValue : getValues()) {
            ValueButton valueButton = new ValueButton(availableValue);
            valueButtons.add(valueButton);
        }

        listComponent.configure(c -> {
            listComponent.clearChildren();
            for (ValueButton valueButton : valueButtons) {
                listComponent.child(createValueComponent(valueButton));
            }
        });
    }

    protected abstract List<T> getValues();
    protected abstract @NotNull String getText(Set<T> values);
    protected abstract @NotNull String getText(T value);
    protected abstract int getColor(Set<T> values);

    public void grabFrom(LogFilter filter){
        grabValues(filter);
        updateAllButtons();
    }

    protected abstract void grabValues(LogFilter filter);

    protected class ValueButton extends BoxComponent {

        private static final int BOX_SIZE = 9;
        private final T stored;

        public ValueButton(T stored) {
            super(Sizing.fixed(BOX_SIZE), Sizing.fixed(BOX_SIZE));
            this.stored = stored;
            color(Color.WHITE);
        }

        private void update(){
            fill(value.contains(stored));
        }
    }

    private class Button extends LabelComponent {

        protected Button() {
            super(TextUtil.colored(getText(value), TextColor.fromRgb(getColor(value))));
            margins(Insets.of(2));
        }

        public void updateValue(){
            text(TextUtil.colored(getText(value), TextColor.fromRgb(getColor(value))));
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
            int clr = getColor(value);
            int alpha = ColorUtil.alpha(clr, 60);

            context.fill(x - 1, y - 1, x + width, y + height, alpha);
            context.drawRectOutline(x - 2, y - 2, width + 3, height + 3, ColorUtil.alpha(clr, 255));
        }
    }
}
