package fish.crafting.logviewer.ui.component;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.log.LogLevel;
import fish.crafting.logviewer.log.LogLine;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;

public class LogViewerScroller<C extends Component> extends ScrollContainer<C> {
    public LogViewerScroller(Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child);
    }

    public boolean isAtBottom() {
        return this.scrollOffset >= maxScroll;
    }

    public double getScrollOffset(){
        return this.scrollOffset;
    }

    public void scrollToTop() {
        this.scrollOffset = 0;
        ConnectionManager.get().use(LogInstance::onScrolled);
    }

    public void scrollToBottom() {
        this.scrollOffset = this.maxScroll;
        ConnectionManager.get().use(LogInstance::onScrolled);
    }

    @Override
    public void scrollBy(double offset, boolean instant, boolean showScrollbar) {
        super.scrollBy(offset, instant, showScrollbar);
        ConnectionManager.get().use(LogInstance::onScrolled);
    }

    public void size() {
        applySizing();
    }

    public void scrollToLine(int line) {
        int scroll = line * LogLine.LINE_COMPONENT_HEIGHT;
        if(scroll > maxScroll) scroll = maxScroll;
        if(scroll < 0) scroll = 0;

        this.scrollOffset = scroll;
        makeScrollInstant();
        ConnectionManager.get().use(LogInstance::onScrolled);
    }

    public void clampScroll(){
        if(currentScrollPosition > maxScroll){
            scrollOffset = maxScroll;
            makeScrollInstant();
        }
    }

    public void makeScrollInstant(){
        this.currentScrollPosition = scrollOffset;
    }

    public void scrollToOffset(double scroll) {
        if(scroll > maxScroll) scroll = maxScroll;
        this.scrollOffset = scroll;
    }
}
