package io.xlorey.xLoreyGuard.server;

import io.xlorey.FluxLoader.plugin.Configuration;
import io.xlorey.FluxLoader.plugin.Plugin;
import io.xlorey.FluxLoader.shared.EventManager;

/**
 * Implementing a server plugin
 */
public class ServerPlugin extends Plugin {
    /**
     * Plugin entry point instance
     */
    private static ServerPlugin instance;

    /**
     * Plugin entry point. Called when a plugin is loaded via FluxLoader.
     */
    @Override
    public void onInitialize() {
        instance = this;

        saveDefaultConfig();

        EventManager.subscribe(new EventHandler());
    }

    /**
     * Getting an instance of the plugin entry point
     * @return an instance of the plugin entry point
     */
    public static ServerPlugin getInstance() {
        return instance;
    }

    /**
     * Retrieving default plugin configuration values
     * @return default plugin configuration
     */
    public static Configuration getDefaultConfig() {
        return getInstance().getConfig();
    }
}
