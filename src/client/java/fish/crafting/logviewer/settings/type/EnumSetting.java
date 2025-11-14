package fish.crafting.logviewer.settings.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fish.crafting.logviewer.settings.CustomSetting;
import fish.crafting.logviewer.settings.SettingsProvider;
import lombok.Getter;
import net.minecraft.text.Text;
import org.apache.commons.lang3.EnumUtils;

public abstract class EnumSetting<T extends Enum<T>> extends CustomSetting<T> {

    @Getter
    private final Class<T> clazz;

    public EnumSetting(String key, String description, SettingsProvider provider, T defaultValue, Class<T> clazz) {
        super(key, description, provider, defaultValue);
        this.clazz = clazz;
    }


    @Override
    public void addToJson(JsonObject obj) {
        obj.addProperty(key, value.name());
    }

    @Override
    public void grabFromJson(JsonElement element) {
        this.value = EnumUtils.getEnum(clazz, element.getAsString());
    }

    public abstract String getNameFor(T value);
}
