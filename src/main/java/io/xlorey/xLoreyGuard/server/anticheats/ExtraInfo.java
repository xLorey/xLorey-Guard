package io.xlorey.xLoreyGuard.server.anticheats;

import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.utils.GeneralTools;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;

/**
 * Author: Deknil
 * Date: 15.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Anti-cheat that prevents players from using the Extra Info
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
 */
public class ExtraInfo {
    /**
     * Performing anti-cheat actions when receiving a new packet
     * @param packet           received packet from the player
     * @param player           player object
     * @param playerConnection active player connection
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        if (!ServerPlugin.getDefaultConfig().getBoolean("extraInfoAntiCheat.isEnable") || GeneralTools.isPlayerHasRights(player)) return;

        GeneralTools.punishPlayer(
                ServerPlugin.getDefaultConfig().getInt("extraInfoAntiCheat.punishType"),
                player,
                ServerPlugin.getDefaultConfig().getString("extraInfoAntiCheat.punishText")
        );
    }
}
