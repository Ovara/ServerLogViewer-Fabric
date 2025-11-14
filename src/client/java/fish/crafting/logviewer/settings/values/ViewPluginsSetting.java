package fish.crafting.logviewer.settings.values;

import lombok.Getter;

public enum ViewPluginsSetting {
    PRESS("Activate"),
    TOGGLE("Toggle"),
    INVERT("Inverted")
    ;

    @Getter
    private final String name;

    ViewPluginsSetting(String name){
        this.name = name;
    }

}
