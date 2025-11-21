package fish.crafting.logviewer.ui;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.log.filter.LogFilter;
import fish.crafting.logviewer.settings.custom.UISettings;
import fish.crafting.logviewer.settings.values.ViewPluginsSetting;
import fish.crafting.logviewer.ui.component.*;
import fish.crafting.logviewer.ui.component.selector.LevelFilterSelector;
import fish.crafting.logviewer.ui.component.selector.PluginFilterSelector;
import fish.crafting.logviewer.util.KeyUtil;
import fish.crafting.logviewer.util.NumUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.FocusHandler;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class LogScreen extends BaseOwoScreen<FlowLayout> {

    private int lastLineCount = -1;

    public LogLineComponent lastHoveredLineComponent;
    private HorizontalFlowLayout viewerAndFilters;
    private VerticalFlowLayout logViewer;
    private ModernTextBoxComponent searchBox;
    private OpenGridLayout topBar;
    private LogViewerScroller<VerticalFlowLayout> scroll;
    private boolean viewingFilters = true;
    private LabelButtonComponent filterButton;
    private VerticalFlowLayout filters;
    private PluginFilterSelector pluginFilterSelector;
    private LabelComponent lineCount;

    private static boolean simpleSideView = true;
    private boolean wasViewPluginsKeybindPressed = false;

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.top(5))
                .surface(Surface.VANILLA_TRANSLUCENT);

        viewerAndFilters = new HorizontalFlowLayout(Sizing.fill(), Sizing.expand());
        viewerAndFilters.margins(Insets.of(0, 2, 2, 0));
        logViewer = new VerticalFlowLayout(Sizing.fill(), Sizing.content());

        scroll = new LogViewerScroller<>(Sizing.expand(), Sizing.fill(), logViewer);
        scroll.scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));
        scroll.fixedScrollbarLength(10);

        topBar = new OpenGridLayout(Sizing.fill(95), Sizing.fixed(20), 1, 3);
        topBar.alignment(HorizontalAlignment.CENTER, VerticalAlignment.TOP);

        searchBox = new ModernTextBoxComponent(Sizing.fill(50)).previewText("Search...");
        searchBox.onChanged().subscribe((str) -> {
            ConnectionManager.get().use(logInstance -> {
                logInstance.getSearchQuery().updateQuery(str);

                logInstance.applyFilter();
            });
        });

        var filterBox = new SingleGridLayout(Sizing.fill(32), Sizing.fill());
        filterBox.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
        filterButton = new LabelButtonComponent(Text.of(" "), () -> {
            viewingFilters = !viewingFilters;
            rebuildFilters(false);
            UISounds.playInteractionSound();
        });

        updateFilterLabels();
        filterBox.child(filterButton, 0, 0);
        topBar.child(filterBox, 0, 2);

        var settingsBox = new SingleGridLayout(Sizing.fill(32), Sizing.fill());
        settingsBox.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        settingsBox.child(new SettingsButtonComponent(), 0, 0);
        topBar.child(settingsBox, 0, 0);

        rootComponent.child(topBar);

        viewerAndFilters.child(scroll);
        rootComponent.child(viewerAndFilters);

        //#if MC>12110
        rootComponent.keyPress().subscribe(keyInput -> {
        //#else
        //$$ rootComponent.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
        //#endif

            //#if MC>12110
            if (keyInput.key() != GLFW.GLFW_KEY_F || (keyInput.modifiers() & GLFW.GLFW_MOD_CONTROL) == 0) return false;
            //#else
            //$$ if (keyCode != GLFW.GLFW_KEY_F || (modifiers & GLFW.GLFW_MOD_CONTROL) == 0) return false;
            //#endif

            FocusHandler focusHandler = uiAdapter.rootComponent.focusHandler();
            if(focusHandler != null){
                focusHandler.focus(
                        searchBox,
                        Component.FocusSource.MOUSE_CLICK
                );
            }

            return true;
        });

        filters = new VerticalFlowLayout(Sizing.fixed(0), Sizing.fill());
        var levelFilterSelector = new LevelFilterSelector(Sizing.fill());
        pluginFilterSelector = new PluginFilterSelector(Sizing.fill());

        ConnectionManager.get().use(logInstance -> {
            levelFilterSelector.grabFrom(logInstance.getFilter());
            pluginFilterSelector.grabFrom(logInstance.getFilter());
            searchBox.setText(logInstance.getSearchQuery().getQuery());
            searchBox.setCursorToStart(false);
        });

        filters.child(levelFilterSelector.oneSelected(false));
        filters.child(pluginFilterSelector.oneSelected(false));
        viewerAndFilters.child(filters);

        //-- BOTTOM BAR

        var btnUp = new MoveToScrollButtonComponent(true);
        var btnDown = new MoveToScrollButtonComponent(false);
        var gotoLine = new GotoLineComponent(Sizing.fixed(40));
        lineCount = Components.label(Text.of(" ")).color(Color.ofArgb(0x55FFFFFF));
        lineCount.sizing(Sizing.content(), Sizing.content()).margins(Insets.left(5));
        updateLineCount();

        var bottomBar = new HorizontalFlowLayout(Sizing.fill(), Sizing.content());
        bottomBar.margins(Insets.of(5, 5, 10, 0));
        bottomBar.gap(5);

        bottomBar.children(List.of(btnUp, btnDown, gotoLine, lineCount));

        rootComponent.child(bottomBar);

        topBar.child(searchBox, 0, 1);
        rebuildLogs();
        rebuildFilters(true);
    }

    public void updateLineCount(){
        if(lineCount == null) return;

        int lines = 0;
        LogInstance logInstance = ConnectionManager.get().getLogInstance();
        if(logInstance != null){
            lines = logInstance.getLogCache().getLineAmount();
        }

        if(lastLineCount == lines) return;
        lastLineCount = lines;

        String text = NumUtil.addCommas(lines) + (lines == 1 ? " Line" : " Lines");
        lineCount.text(Text.of(text));
    }

    public void tick(){
        boolean keyPressed = KeyUtil.isViewPluginsKeyPressed();
        boolean shouldBeSimpleView = simpleSideView;

        ViewPluginsSetting pluginsSetting = UISettings.get().TOGGLE_PLUGINS.get();

        if(pluginsSetting == ViewPluginsSetting.TOGGLE){
            if(wasViewPluginsKeybindPressed != keyPressed && keyPressed){
                shouldBeSimpleView = !simpleSideView;
            }

            wasViewPluginsKeybindPressed = keyPressed;
        }else{
            shouldBeSimpleView = !keyPressed;

            if(pluginsSetting == ViewPluginsSetting.INVERT){
                shouldBeSimpleView = !shouldBeSimpleView;
            }
        }


        if(shouldBeSimpleView != simpleSideView){
            simpleSideView = shouldBeSimpleView;

            //holy nest
            ConnectionManager.get().use(logInstance -> {
                var current = logInstance.getViewerComponents().getCurrent();
                if(current == null) return;

                for (Component c1 : current.children()) {
                    if(!(c1 instanceof VerticalFlowLayout v)) continue; //Blocks

                    for (Component c2 : v.children()) {
                        if(!(c2 instanceof HorizontalFlowLayout h)) continue; //Complete Log Line

                        for (Component c3 : h.children()) {
                            if(!(c3 instanceof SingleGridLayout g)) continue; //Side Bar

                            for (Component c4 : g.children()) {
                                if(!(c4 instanceof LogSideBarComponent logSideBarComponent)) continue;

                                logSideBarComponent.changeView(simpleSideView);
                            }

                        }
                    }
                }
            });
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //Line count can update in many ways, and I'm too lazy to track each one of them. This will suffice
        updateLineCount();
        super.render(context, mouseX, mouseY, delta);
    }

    private void rebuildFilters(boolean instant){
        Sizing sizing;
        if(viewingFilters){
            sizing = Sizing.fixed(100);
        }else{
            sizing = Sizing.fixed(0);
        }

        if(instant){
            filters.horizontalSizing(sizing);
        }else{
            filters.animateHorizontal(sizing);
        }
    }

    public void updateFilterLabels(){
        ConnectionManager.get().use(logInstance -> {
            LogFilter filter = logInstance.getFilter();

            int filterCount = filter.count();
            String s = filterCount == 0 ? "Filters" : ("Filters (" + filterCount + ")");
            filterButton.text(Text.of(s));
        });
    }

    public void updatePlugins(){
        pluginFilterSelector.updateAvailableValues();
    }

    @Override
    protected void init() {
        super.init();

        ConnectionManager.get().use(logInstance -> {
            logInstance.getViewerComponents().update();
        });
    }

    public void rebuildLogs(){
        ConnectionManager.get().useOr(logInstance -> {
            if(logInstance.isStitching()){
                rebuildWaiting();
            }else{
                rebuildLogs(logInstance);
                updateFilterLabels();
            }
        }, this::rebuildNotConnected);
    }

    private void rebuildNotConnected(){
        logViewer.configure(component -> {

            logViewer.clearChildren();

            SingleGridLayout layout = new SingleGridLayout(Sizing.fill(), Sizing.fill());
            layout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            OpenGridLayout grid = new OpenGridLayout(Sizing.content(), Sizing.content(), 2, 1);
            grid.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            grid.child(Components.label(Text.of("This server isn't using ServerLogViewer!")), 0, 0);
            grid.child(new OPCheckComponent(), 1, 0);

            layout.child(grid, 0, 0);
            logViewer.child(layout);
        });
    }

    private void rebuildWaiting(){
        logViewer.configure(component -> {
            logViewer.clearChildren();

            SingleGridLayout layout = new SingleGridLayout(Sizing.fill(), Sizing.fill());
            layout.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
            layout.child(Components.label(Text.of("Gathering logs... Please wait!")), 0, 0);

            logViewer.child(layout);
        });
    }

    private void rebuildLogs(LogInstance instance){
        LogInstance.runOnUIThread(() -> {
            ConnectionManager.get().use(LogInstance::onScrolled);

            logViewer.configure(c -> {
                logViewer.clearChildren();
                logViewer.child(instance.getViewerComponents().getMain());
            });
        });
    }

    public static void use(Consumer<LogScreen> consumer){
        if(MinecraftClient.getInstance().currentScreen instanceof LogScreen logScreen) consumer.accept(logScreen);
    }

    public static LogViewerScroller<VerticalFlowLayout> getScrollContainer() {
        if(MinecraftClient.getInstance().currentScreen instanceof LogScreen logScreen){
            return logScreen.scroll;
        }

        return null;
    }

    public void scrollToLine(int line) {
        scroll.scrollToLine(line);
    }
}
