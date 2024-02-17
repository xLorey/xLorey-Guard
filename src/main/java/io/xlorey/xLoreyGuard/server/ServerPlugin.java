package io.xlorey.xLoreyGuard.server;

import io.xlorey.fluxloader.plugin.Configuration;
import io.xlorey.fluxloader.plugin.Plugin;
import io.xlorey.fluxloader.shared.EventManager;
import io.xlorey.xLoreyGuard.server.handlers.OnAddIncomingHandler;
import io.xlorey.xLoreyGuard.server.handlers.OnChatMessageProcessedHandler;
import io.xlorey.xLoreyGuard.server.handlers.OnPlayerFullyConnectedHandler;
import io.xlorey.xLoreyGuard.server.handlers.OnServerInitializeHandler;

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

        EventManager.subscribe(new OnAddIncomingHandler());
        EventManager.subscribe(new OnChatMessageProcessedHandler());
        EventManager.subscribe(new OnPlayerFullyConnectedHandler());
        EventManager.subscribe(new OnServerInitializeHandler());
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
