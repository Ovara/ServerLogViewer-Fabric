package fish.crafting.logviewer.keybind;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;

public enum KeybindCategory {

    SLV("slv"),
    ;

    private final KeyBinding.Category category;

    KeybindCategory(String subid){
        this.category = KeyBinding.Category.create(Identifier.of("slv", subid));
    }

    public KeyBinding.Category category(){
        return this.category;
    }
}
