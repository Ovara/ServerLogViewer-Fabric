package fish.crafting.logviewer.settings;

import com.google.gson.*;
import fish.crafting.logviewer.settings.type.BoolSetting;
import fish.crafting.logviewer.settings.type.IntSetting;
import fish.crafting.logviewer.util.FileUtil;
import fish.crafting.logviewer.util.Logs;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SettingsProvider {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final Map<String, CustomSetting<?>> settings = new HashMap<>();
    private final String fileName;
    private long saveTimer = 0L;

    public SettingsProvider(String fileName){
        this.fileName = fileName;
    }

    protected IntSetting intSetting(String key, String name, int defaultValue){
        return setting(new IntSetting(key, name, this, defaultValue));
    }

    protected IntSetting intSetting(String key, String name){
        return intSetting(key, name, 0);
    }

    protected BoolSetting boolSetting(String key, String name, boolean defaultValue){
        return setting(new BoolSetting(key, name, this, defaultValue));
    }

    protected BoolSetting boolSetting(String key, String name){
        return boolSetting(key, name, false);
    }


    public void forEach(Consumer<CustomSetting<?>> forEach){
        settings.values().forEach(forEach);
    }

    protected  <T extends CustomSetting<?>> T setting(T setting){
        this.settings.put(setting.getKey(), setting);
        return setting;
    }

    private File getFile(){
        return new File(FileUtil.getDataDir(), fileName + ".json");
    }

    public void markDirty() {
        if(saveTimer == 0L){
            saveTimer = System.currentTimeMillis() + 5_000L;
        }
    }

    public void load(){
        File file = getFile();
        if(!file.exists()) return;

        Thread thread = new Thread(() -> {
            try (Reader reader = new FileReader(file)){
                JsonElement jsonElement = JsonParser.parseReader(reader);
                if(jsonElement == null || jsonElement.isJsonNull()) return;

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if(jsonObject == null) return;

                jsonObject.asMap().forEach((key, element) -> {
                    CustomSetting<?> customSetting = settings.get(key);
                    if(customSetting == null) return;

                    try{
                        customSetting.grabFromJson(element);
                    }catch (Exception e){
                        Logs.logError("Unable to load setting! (" + key + ", " + fileName + ")!", e);
                    }
                });
            } catch (IOException e) {
                Logs.logError("Unable to load settings '" + fileName + "'!", e);
            }
        });

        thread.start();
    }

    public void trySaving(){
        trySaving(false);
    }

    public void trySaving(boolean force){
        if(saveTimer == 0L) return;
        if(!force && saveTimer > System.currentTimeMillis()) return;
        saveTimer = 0L;

        JsonObject object = constructJsonObject();
        Thread thread = new Thread(() -> {
            try(Writer writer = new FileWriter(getFile())){
                GSON.toJson(object, writer);
            } catch (IOException e) {
                Logs.logError("Unable to save settings '" + fileName + "'!", e);
            }
        });

        thread.start();
    }

    private JsonObject constructJsonObject(){
        JsonObject obj = new JsonObject();

        settings.values().forEach(s -> s.addToJson(obj));
        return obj;
    }
}
