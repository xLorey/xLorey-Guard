package io.xlorey.xLoreyGuard.server.anticheats;

import io.xlorey.fluxloader.utils.Logger;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;
import zombie.network.packets.ActionPacket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Deknil
 * Date: 15.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Packet handler for changing player animations (actions)
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
 */
public class PlayerAction {
    /**
     * Performing anti-cheat actions when receiving a new package
     * @param packet           received packet from the player
     * @param player           player object
     * @param playerConnection active player connection
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        ActionPacket actionPacket = new ActionPacket();
        actionPacket.parse(packet.buffer, playerConnection);

        String description = actionPacket.getDescription();

        if (description == null) return;

        String operation = extractValue(description, "operation");
        String action = extractValue(description, "action");

        if (operation == null || action == null) return;

        Logger.print("AC > Player '" + player.getUsername() + "' action: " +  action + " | " + operation);
    }

    /**
     * Extract the key from the action description
     * @param input action description
     * @param key the key to look for
     * @return key value
     */
    private static String extractValue(String input, String key) {
        String regex = key + "=([^|]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
