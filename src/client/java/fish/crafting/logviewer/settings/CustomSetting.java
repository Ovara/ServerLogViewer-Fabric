package fish.crafting.logviewer.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomSetting<T> {

    @Getter
    protected final String key;
    @Getter
    private final String text;
    protected T value;
    private final SettingsProvider holder;
    @Getter
    private final List<SettingChangedListener<T>> listeners = new ArrayList<>();
    @Getter
    private String description = null;

    public CustomSetting(String key, String text, SettingsProvider provider, T defaultValue){
        this.key = key;
        this.text = text;
        this.value = defaultValue;
        this.holder = provider;
    }

    public <K> K description(String description){
        this.description = description;
        return (K) this;
    }

    public void attachListener(SettingChangedListener<T> listener){
        this.listeners.add(listener);
    }

    public T get(){
        return value;
    }

    public void set(T value){
        if(this.value == value) return;

        this.value = value;

        for (SettingChangedListener<T> listener : listeners) {
            if(listener instanceof SettingChangedListener.Immediate<T> immediate) immediate.onChanged(value);
        }

        holder.markDirty();
    }

    public abstract void addToJson(JsonObject obj);
    public abstract void grabFromJson(JsonElement element);
}
