package fish.crafting.logviewer.settings.custom;

import fish.crafting.logviewer.connection.ConnectionManager;
import fish.crafting.logviewer.log.LogInstance;
import fish.crafting.logviewer.log.filter.LogSearchQuery;
import fish.crafting.logviewer.settings.SettingChangedListener;
import fish.crafting.logviewer.settings.SettingsProvider;
import fish.crafting.logviewer.settings.type.BoolSetting;
import fish.crafting.logviewer.settings.type.IntSetting;

public class PerformanceSettings extends SettingsProvider {

    private static PerformanceSettings instance = new PerformanceSettings();

    public final IntSetting SEARCH_THREAD_COUNT = intSetting("search_threads", "Search Thread Count", 2)
            .min(1)
            .description("Choose how many threads should work for Search Queries. These threads are only active while the search is running.");
    public final IntSetting LOAD_THREAD_COUNT = intSetting("load_threads", "Log Load Thread Count", 2)
            .min(1)
            .description("Choose how many threads should work for Loading Logs to display. These threads are always active if you're connected to SLV.");

    private PerformanceSettings() {
        super("performance");

        SEARCH_THREAD_COUNT.attachListener(SettingChangedListener.onAppliedIndividual(newValue -> {
            ConnectionManager.get().use(logInstance -> {
                logInstance.getSearchQuery().setThreadCount(newValue);
            });
        }));

        LOAD_THREAD_COUNT.attachListener(SettingChangedListener.onAppliedIndividual(newValue -> {
            ConnectionManager.get().use(logInstance -> {
                logInstance.getLogCache().setReaderThreads(newValue);
            });
        }));
    }

    public static PerformanceSettings get(){
        return instance;
    }
}
