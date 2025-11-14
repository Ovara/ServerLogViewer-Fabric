package fish.crafting.logviewer.mixin;

import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.util.FocusHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(BaseParentComponent.class)
public class BaseParentComponentMixin {

    @Redirect(method = "drawChildren", at = @At(value = "INVOKE", target = "Lio/wispforest/owo/ui/util/FocusHandler;lastFocusSource()Lio/wispforest/owo/ui/core/Component$FocusSource;"))
    private Component.FocusSource fix(FocusHandler instance){
        if(instance == null) return null;
        return instance.lastFocusSource();
    }
}
