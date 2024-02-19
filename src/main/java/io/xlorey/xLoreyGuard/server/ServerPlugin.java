package io.xlorey.xLoreyGuard.server;

import io.xlorey.fluxloader.plugin.Configuration;
import io.xlorey.fluxloader.plugin.Plugin;
import io.xlorey.fluxloader.shared.EventManager;
import io.xlorey.xLoreyGuard.server.anticheats.ItemDupe;
import io.xlorey.xLoreyGuard.server.handlers.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        EventManager.subscribe(new OnPlayerBanHandler());
        EventManager.subscribe(new OnPlayerKickHandler());
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
