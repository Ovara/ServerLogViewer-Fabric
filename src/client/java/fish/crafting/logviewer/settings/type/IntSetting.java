package fish.crafting.logviewer.settings.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fish.crafting.logviewer.settings.CustomSetting;
import fish.crafting.logviewer.settings.SettingsProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class IntSetting extends CustomSetting<Integer> {

    @Setter
    private int min = -1, max = -1;

    public IntSetting(String key, String description, SettingsProvider provider, Integer defaultValue) {
        super(key, description, provider, defaultValue);
    }

    public IntSetting minMax(int min, int max){
        return min(min).max(max);
    }

    @Override
    public void addToJson(JsonObject obj) {
        obj.addProperty(key, value);
    }

    @Override
    public void grabFromJson(JsonElement element) {
        this.value = element.getAsInt();
    }

    public boolean hasMin(){
        return min != -1;
    }

    public boolean hasMax(){
        return max != -1;
    }

    @Override
    public void set(Integer value) {
        if(hasMin()){
            if(value < min) value = min;
        }

        if(hasMax()){
            if(value > max) value = max;
        }
        super.set(value);
    }
}
