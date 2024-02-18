package io.xlorey.xLoreyGuard.server.anticheats;

import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.utils.GeneralTools;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;
import zombie.network.packets.PlayerID;
import zombie.network.packets.hit.Perk;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Author: Deknil
 * GitHub: <a href=https://github.com/Deknil>https://github.com/Deknil</a>
 * Date: 18.02.2024
 * Description: Handling experience gain from player
 * <p>xLoreyGuard © 2024. All rights reserved.</p>
 */
public class Experience {
    /**
     * Performing anti-cheat actions when receiving a new packet
     * @param packet           received packet from the player
     * @param player           player object
     * @param playerConnection active player connection
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) throws IOException {
        if (!ServerPlugin.getDefaultConfig().getBoolean("skillAntiCheat.isEnable") || GeneralTools.isPlayerHasRights(player)) return;

        ByteBuffer byteBuffer = packet.buffer;
        PlayerID target = new PlayerID();
        target.parse(byteBuffer, playerConnection);
        target.parsePlayer(playerConnection);
        Perk perk = new Perk();
        perk.parse(byteBuffer, playerConnection);
        int amount = byteBuffer.getInt();

        Logger.print(String.format("AC > Player '%s' gained %s experience in skill '%s'", player.getUsername(), amount, perk.getPerk().getId()));
    }
}
