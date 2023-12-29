package io.xlorey.xLoreyGuard.server.utils;

import io.xlorey.FluxLoader.server.api.IncomingPacket;
import io.xlorey.FluxLoader.server.api.PlayerUtils;
import io.xlorey.FluxLoader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.enums.PunishType;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoObject;

import java.nio.ByteBuffer;

/**
 * A set of basic tools for various tasks
 */
public class GeneralTools {
    /**
     * Punishes the player according to the specified punishment type.
     * @param punishType The punishment type, represented as an integer value.
     *                   Must match the ordinal value of the {@link PunishType} enumeration.
     * @param player The player ({@link IsoPlayer}) to whom the penalty is assigned.
     * @param reason The reason for the penalty, represented as a string.
     */
    public static void punishPlayer(int punishType, IsoPlayer player, String reason){
        punishPlayer(punishType, player, reason, null);
    }

    /**
     * Punishes the player according to the specified punishment type.
     * @param punishType The punishment type, represented as an integer value.
     *                   Must match the ordinal value of the {@link PunishType} enumeration.
     * @param player The player ({@link IsoPlayer}) to whom the penalty is assigned.
     * @param reason The reason for the penalty, represented as a string.
     * @param packetData information about the received packet to block it
     */
    public static void punishPlayer(int punishType, IsoPlayer player, String reason, ByteBuffer packetData){
        if (punishType == PunishType.NOTHING.ordinal()) return;

        Logger.print(String.format("AC > Player %s | %s", player.getDisplayName(), reason));
        if (packetData != null) IncomingPacket.blockPacket(packetData);

        if (punishType == PunishType.LOGGING.ordinal()) {
            return;
        };

        if (punishType == PunishType.KICK.ordinal()) {
            PlayerUtils.kickPlayer(player, reason);
        };

        if (punishType == PunishType.BAN.ordinal()) {
            PlayerUtils.banPlayer(player, reason);
        };
    }

    /**
     * Checks if the packet is authorized from the player.
     * @param player The player whose package is being checked.
     * @return true if the package is authorized, false otherwise.
     */
    public static boolean isAuthorizedPacket(IsoPlayer player){
        if (ServerPlugin.getDefaultConfig().getBoolean("antiCheatWorksOnAdmins")) return false;

        for (Object whiteListedUsername : ServerPlugin.getDefaultConfig().getList("whiteListUsername")) {
            String userName = (String) whiteListedUsername;

            if (userName == null || userName.isEmpty()) continue;

            if(userName.equalsIgnoreCase(player.getUsername())) return true;
        }

        for (Object whiteListedGroup : ServerPlugin.getDefaultConfig().getList("whiteListGroup")) {
            String group = (String) whiteListedGroup;

            if (group == null || group.isEmpty()) continue;

            if(group.equalsIgnoreCase(player.getAccessLevel())) return true;
        }

        for (Object whiteListedIP : ServerPlugin.getDefaultConfig().getList("whiteListIP")) {
            String ipAddress = (String) whiteListedIP;

            if (ipAddress == null || ipAddress.isEmpty()) continue;

            if(ipAddress.equalsIgnoreCase(PlayerUtils.getPlayerIP(player))) return true;
        }

        return false;
    }

    /**
     * Getting the distance between the player and the object
     */
    public static float getDistanceBetweenObject(IsoPlayer player, IsoObject object) {
        float deltaX = player.getX() - object.getX();
        float deltaY = player.getY() - object.getY();

        float deltaSquareX = deltaX * deltaX;
        float deltaSquareY = deltaY * deltaY;

        return (float) Math.sqrt(deltaSquareX + deltaSquareY);
    }
}
