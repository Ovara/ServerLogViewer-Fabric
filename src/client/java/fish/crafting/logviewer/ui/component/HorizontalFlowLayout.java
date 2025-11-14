package fish.crafting.logviewer.ui.component;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;

public class HorizontalFlowLayout extends FlowLayout {

    public HorizontalFlowLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL);
    }
}
