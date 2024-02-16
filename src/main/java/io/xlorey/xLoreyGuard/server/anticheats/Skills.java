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

        ArrayList<IsoGameCharacter.PerkInfo> perkListNew = new ArrayList<>(player.getPerkList());

        if (perkListSaved.size() != perkListNew.size()) {
            Logger.print(String.format("AC > Player '%s' perk list sizes are inconsistent.", player.getUsername()));
            return;
        }

        for (int i = 0; i < perkListSaved.size(); i++) {
            IsoGameCharacter.PerkInfo savedPerkInfo = perkListSaved.get(i);
            IsoGameCharacter.PerkInfo newPerkInfo = perkListNew.get(i);

            if (!savedPerkInfo.perk.equals(newPerkInfo.perk)) {
                Logger.print(String.format("AC > Player '%s' has a mismatch between old and new skills at position %s", player.getUsername(), i));
                continue;
            }

            Float oldXPFloat = savedPlayerXP.get(savedPerkInfo.perk);
            if (oldXPFloat == null) continue;

            float oldXP = savedPlayerXP.get(savedPerkInfo.perk);
            float newXP = player.getXp().getXP(newPerkInfo.perk);

            if (savedPerkInfo.level != newPerkInfo.level) {
                String levelChangeMessage = String.format("AC > Player '%s' has had his skill '%s' changed: Old level: %d, New level: %d.",
                        player.username, savedPerkInfo.perk.getName(), savedPerkInfo.level, newPerkInfo.level);
                Logger.print(levelChangeMessage);
            }

            if (oldXP != newXP) {
                String xpChangeMessage = String.format("AC > Player '%s' skill '%s' experience changed from '%.1f' to '%.1f'.",
                        player.username, newPerkInfo.perk.getName(), oldXP, newXP);
                Logger.print(xpChangeMessage);

                int levelForXP = Math.max(1, Math.min(newPerkInfo.level, 10));

                float requiredXPForNewLevel = newPerkInfo.perk.getXpForLevel(levelForXP);

                double configXPChangePercentage = ServerPlugin.getDefaultConfig().getFloat("skillAntiCheat.deltaXPPercentage");
                float acceptableXPChangePercentage = configXPChangePercentage > 0f ? (float) configXPChangePercentage : 0.6f;

                int percentageXp = (int) (acceptableXPChangePercentage * 100);

                if (Math.abs(newXP - oldXP) > requiredXPForNewLevel * acceptableXPChangePercentage) {
                    Logger.print(String.format("AC > Player '%s' changed skill experience '%s' from '%.1f' to '%.1f'. Difference of over %d%%!",
                            player.username, newPerkInfo.perk.getName(), oldXP, newXP, percentageXp));

                    GeneralTools.punishPlayer(
                            ServerPlugin.getDefaultConfig().getInt("skillAntiCheat.punishType"),
                            player,
                            ServerPlugin.getDefaultConfig().getString("skillAntiCheat.punishText"));
                }
            }
        }
    }
}
