package io.xlorey.xLoreyGuard.server.anticheats;

import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.utils.GeneralTools;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.skills.PerkFactory;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Deknil
 * Date: 15.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Anti-cheat that prevents players from using the Skills Cheat
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
 */
public class Skills {
    /**
     * Performing anti-cheat actions when receiving a new packet
     * @param packet           received packet from the player
     * @param player           player object
     * @param playerConnection active player connection
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) throws IOException {
        if (!ServerPlugin.getDefaultConfig().getBoolean("skillAntiCheat.isEnable") || GeneralTools.isPlayerHasRights(player)) return;

        ByteBuffer byteBuffer = packet.buffer;
        short playerID = byteBuffer.getShort();

        ArrayList<IsoGameCharacter.PerkInfo> perkListSaved = new ArrayList<>(player.getPerkList());
        HashMap<PerkFactory.Perk, Float> savedPlayerXP = new HashMap<>(player.getXp().XPMap);

        player.getXp().load(byteBuffer, 195);

        double survivedHours = player.getHoursSurvived();
        int minHours = ServerPlugin.getDefaultConfig().getInt("skillAntiCheat.minHours");
        int maxLevel = ServerPlugin.getDefaultConfig().getInt("skillAntiCheat.maxLevel");

        ArrayList<IsoGameCharacter.PerkInfo> perkListNew = new ArrayList<>(player.getPerkList());

        if (perkListSaved.size() != perkListNew.size()) {
            Logger.print(String.format("AC > Player '%s' perk list sizes are inconsistent.", player.getUsername()));
            return;
        }

        for (int index = 0; index < perkListSaved.size(); index++) {
            IsoGameCharacter.PerkInfo savedPerkInfo = perkListSaved.get(index);
            IsoGameCharacter.PerkInfo newPerkInfo = perkListNew.get(index);
            String perkID = newPerkInfo.perk.getId().toLowerCase();

            if (!savedPerkInfo.perk.equals(newPerkInfo.perk)) {
                Logger.print(String.format("AC > Player '%s' has a mismatch between old and new skills at position %s", player.getUsername(), index));
                continue;
            }

            Float oldXPFloat = savedPlayerXP.get(savedPerkInfo.perk);
            if (oldXPFloat == null) continue;

            if (savedPerkInfo.level < newPerkInfo.level) {
                String levelChangeMessage = String.format("AC > Player '%s' has had his skill '%s' changed: Old level: %d, New level: %d. Hours survived: %.1f",
                        player.username,
                        perkID,
                        savedPerkInfo.level,
                        newPerkInfo.level,
                        survivedHours);
                Logger.print(levelChangeMessage);
            }

            if (perkID.equalsIgnoreCase("fitness") || perkID.equalsIgnoreCase("strength")) continue;

            if (newPerkInfo.level >= maxLevel && survivedHours < minHours) {
                String punishText = String.format("%s | Skill: %s | Level: %s | Survived hours: %.1f",
                        ServerPlugin.getDefaultConfig().getString("skillAntiCheat.punishText"),
                        perkID,
                        newPerkInfo.level,
                        survivedHours);
                GeneralTools.punishPlayer(
                        ServerPlugin.getDefaultConfig().getInt("skillAntiCheat.punishType"),
                        player,
                        punishText);
                return;
            }
        }
    }
}
