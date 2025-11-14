package fish.crafting.logviewer.settings.custom;

import fish.crafting.logviewer.settings.SettingsProvider;
import fish.crafting.logviewer.settings.type.BoolSetting;
import fish.crafting.logviewer.settings.type.EnumSetting;
import fish.crafting.logviewer.settings.values.ViewPluginsSetting;

public class UISettings extends SettingsProvider {

    private static UISettings instance = new UISettings();

    public final EnumSetting<ViewPluginsSetting> TOGGLE_PLUGINS = setting(new TogglePlugins("toggle_plugins", "Toggle Plugin View"))
            .description("Choose whether pressing the Plugin View keybind should toggle the line plugin view.");
    public final BoolSetting PLUGINS_AND_LINE_NUM = boolSetting("plugins_with_line_id", "View Plugins with Line #", false)
            .description("Shows the Log Line # alongside the plugin in the Plugin View.");

    private UISettings() {
        super("log_ui");
    }

    public static UISettings get(){
        return instance;
    }

    private class TogglePlugins extends EnumSetting<ViewPluginsSetting> {

        public TogglePlugins(String key, String description) {
            super(key, description, UISettings.this, ViewPluginsSetting.PRESS, ViewPluginsSetting.class);
        }

        @Override
        public String getNameFor(ViewPluginsSetting value) {
            return value.getName();
        }
    }
}
