package fish.crafting.logviewer.ui;

import fish.crafting.logviewer.keybind.OpenLogsKeybind;
import fish.crafting.logviewer.settings.*;
import fish.crafting.logviewer.settings.type.BoolSetting;
import fish.crafting.logviewer.settings.type.EnumSetting;
import fish.crafting.logviewer.settings.type.IntSetting;
import fish.crafting.logviewer.ui.component.*;
import fish.crafting.logviewer.util.KeyUtil;
import fish.crafting.logviewer.util.TextUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import oshi.util.tuples.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SettingsScreen extends BaseOwoScreen<FlowLayout> {

    private VerticalFlowLayout categories;
    private SettingsCategory selected = SettingsCategory.UI;
    private VerticalFlowLayout settings;
    private final List<SettingsLayout<?>> settingsLayouts = new ArrayList<>();

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::horizontalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .surface(Surface.VANILLA_TRANSLUCENT);

        //#if MC>12110
        rootComponent.keyPress().subscribe((keyInput -> {
            if(keyInput.key() == GLFW.GLFW_KEY_ESCAPE){
        //#else
        //$$ rootComponent.keyPress().subscribe(((keyCode, scanCode, modifiers) -> {
        //$$ if(keyCode == GLFW.GLFW_KEY_ESCAPE){
        //#endif
                OpenLogsKeybind.instance.onPressed();
            }

            return true;
        }));

        categories = new VerticalFlowLayout(Sizing.fill(), Sizing.fill());
        for (SettingsCategory value : SettingsCategory.values()) {
            categories.child(new CategoryButton(value, () -> onCategoryClick(value, false)));
        }

        rootComponent.child(scrollOf(categories, Sizing.fill(20)));
        categories.margins(Insets.of(20, 0, 20, 20));
        categories.gap(5);

        settings = new VerticalFlowLayout(Sizing.fill(), Sizing.fill());
        settings.margins(Insets.of(20));
        settings.gap(5);
        rootComponent.child(scrollOf(settings, Sizing.fill(80)));

        onCategoryClick(selected, true);
    }

    private void onAnySettingChanged(){

    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void onCategoryClick(SettingsCategory category, boolean force){
        if(!force && category == selected) return;
        onSettingsApplied();
        settingsLayouts.clear();
        selected = category;

        settings.configure(c -> {
            settings.clearChildren();
            SettingsProvider provider = SettingsManager.get().getProvider(selected);
            if(provider == null) return;

            AtomicInteger i = new AtomicInteger(0);
            provider.forEach(setting -> {
                SettingsLayout<?> settingsLayout = new SettingsLayout<>(setting, i.getAndAdd(1) % 2 == 0);
                settingsLayouts.add(settingsLayout);
                settings.child(settingsLayout);
            });
        });
    }

    private void onSettingsApplied(){
        Set<SettingChangedListener.OnApplyBatch<?>> batchListeners = new HashSet<>();
        for (SettingsLayout<?> settingsLayout : settingsLayouts) {
            handleApplySettings(settingsLayout, batchListeners);
        }

        for (SettingChangedListener.OnApplyBatch<?> batchListener : batchListeners) {
            batchListener.onChanged();
        }
    }

    private <T> void handleApplySettings(SettingsLayout<T> layout, Set<SettingChangedListener.OnApplyBatch<?>> batchListeners){
        if(!layout.isDirty()) return;

        for (SettingChangedListener<T> listener : layout.setting.getListeners()) {
            if(listener instanceof SettingChangedListener.OnApplyBatch<?> batch){
                batchListeners.add(batch);
            }else if(listener instanceof SettingChangedListener.OnApplyIndividual<T> individual){
                individual.onChanged(layout.setting.get());
            }
        }
    }

    @Override
    public void close() {
        super.close();
        onSettingsApplied();
        SettingsManager.get().saveAllDirty();
    }

    private static Component scrollOf(FlowLayout flowLayout, Sizing sizing){
        return Containers.verticalScroll(sizing, Sizing.expand(), flowLayout);
    }

    private class CategoryButton extends LabelButtonComponent {

        private final SettingsCategory category;
        private Boolean oldSelected = null;

        public CategoryButton(SettingsCategory category, Runnable onClick) {
            super(Text.of(category.getName()), onClick);
            this.category = category;
            cursorStyle(CursorStyle.HAND);
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            boolean newSelected = selected == this.category;
            if(oldSelected == null || newSelected != oldSelected){
                oldSelected = newSelected;

                if(newSelected){
                    color(Color.WHITE);
                    drawHorizontalLine = true;
                }else{
                    color(Color.ofArgb(0xFF999999));
                    drawHorizontalLine = false;
                }
            }

            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    private class SettingsLayout<T> extends OpenGridLayout {

        private final CustomSetting<T> setting;
        private T oldValue;
        private final boolean fill;

        public SettingsLayout(CustomSetting<T> setting, boolean fill) {
            super(Sizing.fill(), Sizing.content(), 1, 2);
            this.setting = setting;
            this.oldValue = setting.get();
            this.fill = fill;

            verticalAlignment(VerticalAlignment.CENTER);

            var leftBox = new SingleGridLayout(Sizing.fill(50), Sizing.content());
            leftBox.horizontalAlignment(HorizontalAlignment.LEFT).margins(Insets.left(10));
            LabelComponent label = Components.label(Text.of(setting.getText()));
            if(setting.getDescription() != null){
                label.tooltip(TextUtil.colored(TextUtil.wrap(setting.getDescription(), 35), TextColor.fromRgb(0xFFC48C)));
            }
            leftBox.child(label, 0, 0);

            child(leftBox, 0, 0);

            var rightBox = new SingleGridLayout(Sizing.fill(50), Sizing.content());
            rightBox.horizontalAlignment(HorizontalAlignment.RIGHT).margins(Insets.right(10));
            rightBox.child(createComponent(setting), 0, 0);

            child(rightBox, 0, 1);
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
            if(fill){
                context.fill(x, y, x + width - 5, y + height, 0x33FFFFFF);
            }
        }

        public boolean isDirty(){
            return !Objects.equals(oldValue, setting.get());
        }
    }

    private Component createComponent(CustomSetting<?> setting){
        if(setting instanceof BoolSetting boolSetting){
            return new BoolSettingComponent(boolSetting);
        }else if(setting instanceof IntSetting intSetting){
            return new IntSettingComponent(intSetting);
        }else if(setting instanceof EnumSetting<?> enumSetting){
            return createEnumComponent(enumSetting);
        }

        return new SingleGridLayout(Sizing.fixed(0), Sizing.fixed(0));
    }

    private <T extends Enum<T>> Component createEnumComponent(EnumSetting<T> enumSetting){
        return new EnumSettingComponent<>(enumSetting, enumSetting.getClazz().getEnumConstants());
    }

    private class EnumSettingComponent<T extends Enum<T>> extends GridLayout {

        private final EnumSetting<T> enumSetting;
        private T selected;

        protected EnumSettingComponent(EnumSetting<T> enumSetting, T[] values) {
            super(Sizing.expand(), Sizing.content(), (int) Math.ceil(values.length / 3.0), Math.min(3, values.length));
            this.enumSetting = enumSetting;

            selected = enumSetting.get();

            int column = 0;
            int row = 0;
            for (T value : values) {
                child(new EnumOption<>(this, value), row, column);

                column++;
                if(column >= 3) {
                    column = 0;
                    row++;
                }
            }
        }

        private void onClick(T value){
            selected = value;
            enumSetting.set(value);
            onAnySettingChanged();
        }
    }

    private class EnumOption<T extends Enum<T>> extends LabelButtonComponent {

        private final EnumSettingComponent<T> settingComp;
        private final T value;
        private Boolean oldSelected = null;

        public EnumOption(EnumSettingComponent<T> setting, T value) {
            super(Text.of(setting.enumSetting.getNameFor(value)), () -> {
                setting.onClick(value);
            });
            cursorStyle(CursorStyle.HAND);

            this.settingComp = setting;
            this.value = value;
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            boolean newSelected = settingComp.selected == value;
            if(oldSelected == null || newSelected != oldSelected){
                oldSelected = newSelected;

                if(newSelected){
                    color(Color.WHITE);
                    drawHorizontalLine = true;
                }else{
                    color(Color.ofArgb(0xFF999999));
                    drawHorizontalLine = false;
                }
            }
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    private class IntSettingComponent extends ModernTextBoxComponent {

        private final IntSetting intSetting;

        protected IntSettingComponent(IntSetting intSetting) {
            super(Sizing.fixed(30));
            this.intSetting = intSetting;

            syncText();
            setTextPredicate(str -> {
                for (char c : str.toCharArray()) {
                    if(c < '0' || c > '9') return false;
                }

                return true;
            });

            mouseScroll().subscribe((x, y, scroll) -> {
                if(!KeyUtil.isControlPressed()) return true;

                UISounds.playInteractionSound();

                if(scroll < 0){
                    intSetting.set(intSetting.get() - 1);
                }else{
                    intSetting.set(intSetting.get() + 1);
                }

                syncText();
                onAnySettingChanged();
                return true;
            });

            //#if MC>12110
            keyPress().subscribe(keyInput -> {
                if(keyInput.key() == GLFW.GLFW_KEY_ENTER){
            //#else
            //$$ keyPress().subscribe((btn, a, b) -> {
            //$$ if(btn == GLFW.GLFW_KEY_ENTER){
            //#endif
                    updateSetting();
                }
                return true;
            });

            focusLost().subscribe(this::updateSetting);
        }

        private void updateSetting(){
            String text = getText();
            try{
                int v = Integer.parseInt(text);
                intSetting.set(v);
                onAnySettingChanged();
            }catch (Exception ignored){
            }

            syncText();
        }

        private void syncText(){
            text(intSetting.get() + "");
        }
    }

    private class BoolSettingComponent extends BoxComponent {

        private final BoolSetting boolSetting;

        public BoolSettingComponent(BoolSetting boolSetting) {
            super(Sizing.fixed(9), Sizing.fixed(9));
            this.boolSetting = boolSetting;
            cursorStyle(CursorStyle.HAND);

            //#if MC>12110
            mouseDown().subscribe((click, doubled) -> {
            //#else
            //$$ mouseDown().subscribe((a, b, c) -> {
            //#endif
                onAnySettingChanged();
                UISounds.playInteractionSound();
                boolSetting.set(!boolSetting.get());
                return true;
            });
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            if(boolSetting.get()){
                context.fill(x, y, x + width, y + width, 0xFFFFFFFF);
            }else{
                context.drawRectOutline(x, y, width, height, 0xFFFFFFFF);
            }
        }
    }

}
