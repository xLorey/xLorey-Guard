package io.xlorey.xLoreyGuard.server.handlers;

import io.xlorey.fluxloader.events.OnServerInitialize;
import io.xlorey.fluxloader.utils.Logger;

/**
 * Author: Deknil
 * Date: 14.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Called after the server is fully initialized
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
 */
public class OnServerInitializeHandler extends OnServerInitialize {
    /**
     * Called Event Handling Method
     */
    @Override
    public void handleEvent() {
        Logger.print("AC > The server protection system has been initialized!");
    }
}
