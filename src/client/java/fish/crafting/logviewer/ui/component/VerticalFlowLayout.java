package fish.crafting.logviewer.ui.component;

import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.core.Sizing;

public class VerticalFlowLayout extends FixedFlowLayout {

    public VerticalFlowLayout(Sizing horizontalSizing, Sizing verticalSizing) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
    }

    public void animateHorizontal(Sizing sizing) {
        horizontalSizing.animate(250, Easing.CUBIC, sizing).forwards();
        //horizontalSizing.set(sizing);
    }
}
