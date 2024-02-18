package io.xlorey.xLoreyGuard.server.handlers;

import io.xlorey.fluxloader.events.OnPlayerBan;
import io.xlorey.xLoreyGuard.server.discord.WebHook;
import zombie.characters.IsoPlayer;

/**
 * Author: Deknil
 * Date: 18.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Handling player bans
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
 */
public class OnPlayerBanHandler extends OnPlayerBan {
    /**
     * Called Event Handling Method
     * @param player An object of the player who was banned.
     * @param adminName Nickname of the administrator who banned the player
     * @param reason Reason for blocking the player.
     */
    @Override
    public void handleEvent(IsoPlayer player, String adminName, String reason) {
        WebHook.sendMessage(player, adminName, reason);
    }
}
