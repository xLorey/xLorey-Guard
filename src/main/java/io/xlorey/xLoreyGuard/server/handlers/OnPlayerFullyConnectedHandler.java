package io.xlorey.xLoreyGuard.server.handlers;

import io.xlorey.fluxloader.events.OnPlayerFullyConnected;
import io.xlorey.xLoreyGuard.server.anticheats.VPN;
import io.xlorey.xLoreyGuard.server.utils.InventoryTools;
import zombie.core.raknet.UdpConnection;

import java.nio.ByteBuffer;

/**
 * Author: Deknil
 * GitHub: <a href=https://github.com/Deknil>https://github.com/Deknil</a>
 * Date: 17.02.2024
 * Description: Handling the player's full connection to the server
 * <p>xLoreyGuard © 2024. All rights reserved.</p>
 */
public class OnPlayerFullyConnectedHandler extends OnPlayerFullyConnected {
    /**
     * Called Event Handling Method
     * @param data Player connection information
     * @param playerConnection Player connection
     * @param username Player nickname
     */
    @Override
    public void handleEvent(ByteBuffer data, UdpConnection playerConnection, String username) {
        VPN.handlePacket(playerConnection);
        InventoryTools.requestPlayerInventory(playerConnection);
    }
}
