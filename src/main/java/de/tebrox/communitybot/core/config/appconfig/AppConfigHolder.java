package de.tebrox.communitybot.core.config.appconfig;

/**
 * Static holder so that the loaded AppConfig is accessible before Spring context starts.
 */
public final class AppConfigHolder {

    private static AppConfig config;

    private AppConfigHolder() {}

    public static void set(AppConfig cfg) {
        config = cfg;
    }

    public static AppConfig get() {
        return config;
    }
}
