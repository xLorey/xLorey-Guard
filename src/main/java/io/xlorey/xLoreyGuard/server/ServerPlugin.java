package io.xlorey.xLoreyGuard.server;

import io.xlorey.FluxLoader.plugin.Plugin;
import io.xlorey.FluxLoader.shared.EventManager;

/**
 * Implementing a server plugin
 */
public class ServerPlugin extends Plugin {
    /**
     * Plugin entry point. Called when a plugin is loaded via FluxLoader.
     */
    public void onInitialize() {
        EventManager.subscribe(new EventsAdapter());
    }
}
