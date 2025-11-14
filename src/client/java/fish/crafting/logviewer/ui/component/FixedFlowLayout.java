package fish.crafting.logviewer.ui.component;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;

import java.util.ArrayList;

public class FixedFlowLayout extends FlowLayout {

    protected FixedFlowLayout(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
        super(horizontalSizing, verticalSizing, algorithm);
    }

    @Override
    public FlowLayout clearChildren() {
        var copy = new ArrayList<>(this.children);
        this.children.clear();

        for (var child : copy) {
            child.dismount(DismountReason.REMOVED);
        }

        this.updateLayout();
        return this;
    }
}
