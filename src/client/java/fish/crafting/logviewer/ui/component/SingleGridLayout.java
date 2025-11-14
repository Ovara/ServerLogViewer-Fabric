package fish.crafting.logviewer.ui.component;

import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.Sizing;

public class SingleGridLayout extends GridLayout {

    public SingleGridLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, 1, 1);
    }
}
