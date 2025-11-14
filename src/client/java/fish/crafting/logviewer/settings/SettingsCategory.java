package fish.crafting.logviewer.settings;

import lombok.Getter;

public enum SettingsCategory {

    UI("UI Settings"),
    PERFORMANCE("Performance Settings")
    ;
    @Getter
    private final String name;

    SettingsCategory(String name){
        this.name = name;
    }

}
