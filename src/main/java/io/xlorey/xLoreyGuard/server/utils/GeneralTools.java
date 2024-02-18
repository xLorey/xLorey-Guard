package io.xlorey.xLoreyGuard.server.utils;

import io.xlorey.fluxloader.server.api.PlayerUtils;
import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.discord.WebHook;
import io.xlorey.xLoreyGuard.server.enums.PunishType;
import zombie.characters.IsoPlayer;
import zombie.iso.IsoObject;

/**
 * Author: Deknil
 * Date: 15.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: A set of basic tools for various tasks
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
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
        if (punishType == PunishType.NOTHING.ordinal()) return;

        Logger.print(String.format("AC > Player %s | %s", player.getDisplayName(), reason));

        if (punishType == PunishType.LOGGING.ordinal()) {
            return;
        };

        if (punishType == PunishType.KICK.ordinal()) {
            PlayerUtils.kickPlayer(player, reason);
        };

        if (punishType == PunishType.BAN.ordinal()) {
            PlayerUtils.banPlayer(player, reason, false, false);
        };
    }

    /**
     * Checks if the player has rights to ignore anti-cheat
     * @param player The player whose package is being checked.
     * @return true if the package is authorized, false otherwise.
     */
    public static boolean isPlayerHasRights(IsoPlayer player){
        for (Object whiteListedUsername : ServerPlugin.getDefaultConfig().getList("general.whiteListUsername")) {
            String userName = (String) whiteListedUsername;

            if (userName == null || userName.isEmpty()) continue;

            if(userName.equalsIgnoreCase(player.getUsername())) return true;
        }

        for (Object whiteListedGroup : ServerPlugin.getDefaultConfig().getList("general.whiteListGroup")) {
            String group = (String) whiteListedGroup;

            if (group == null || group.isEmpty()) continue;

            if(group.equalsIgnoreCase(player.getAccessLevel())) return true;
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
