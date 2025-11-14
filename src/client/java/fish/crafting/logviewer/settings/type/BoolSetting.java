package fish.crafting.logviewer.settings.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fish.crafting.logviewer.settings.CustomSetting;
import fish.crafting.logviewer.settings.SettingsProvider;

public class BoolSetting extends CustomSetting<Boolean> {


    public BoolSetting(String key, String description, SettingsProvider provider, Boolean defaultValue) {
        super(key, description, provider, defaultValue);
    }

    @Override
    public void addToJson(JsonObject obj) {
        obj.addProperty(key, value);
    }

    @Override
    public void grabFromJson(JsonElement element) {
        this.value = element.getAsBoolean();
    }
}
