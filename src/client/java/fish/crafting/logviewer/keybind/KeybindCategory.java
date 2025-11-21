package fish.crafting.logviewer.keybind;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;

public enum KeybindCategory {

    SLV("slv"),
    ;

    private final Identifier categoryIdentifier;
    //#if MC>12110
    private final KeyBinding.Category category;
    //#endif

    KeybindCategory(String subid) {
        this.categoryIdentifier = Identifier.of("slv", subid);
        //#if MC>12110
        this.category = KeyBinding.Category.create(this.categoryIdentifier);
        //#endif
    }

    public String translation(){
        return this.categoryIdentifier.toTranslationKey("key.category");
    }

    //#if MC>12110
    public KeyBinding.Category category(){
        return this.category;
    }
    //#endif
}
