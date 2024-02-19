package io.xlorey.xLoreyGuard.server.anticheats;

import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.utils.GeneralTools;
import io.xlorey.xLoreyGuard.server.utils.InventoryTools;
import zombie.Lua.LuaEventManager;
import zombie.MapCollisionData;
import zombie.characters.IsoPlayer;
import zombie.core.logger.LoggerManager;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.debug.DebugLog;
import zombie.debug.DebugType;
import zombie.inventory.InventoryItem;
import zombie.inventory.ItemContainer;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.iso.areas.isoregion.IsoRegions;
import zombie.iso.objects.IsoGenerator;
import zombie.iso.objects.IsoWorldInventoryObject;
import zombie.network.*;
import zombie.vehicles.PolygonalMap2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Deknil
 * Date: 18.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Checking players for item duplication
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
 */
public class ItemDupe {
    /**
     * Performing anti-cheat actions when receiving a new packet update Inventory
     * @param packet           received packet from the player
     * @param player           player object
     * @param playerConnection active player connection
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) throws IOException {
        if (!ServerPlugin.getDefaultConfig().getBoolean("antiItemDupe.isEnable") || GeneralTools.isPlayerHasRights(player)) return;

        ItemContainer inventory = player.getInventory();
        if (inventory == null) return;

        ArrayList<InventoryItem> items = inventory.getItems();
        HashMap<String, Integer> itemsCount = new HashMap<>();

        for (InventoryItem item : items) {
            if (item == null) continue;

            String itemID = item.getFullType().toLowerCase();

            String punishText = String.format("%s | ID: %s",
                    ServerPlugin.getDefaultConfig().getString("antiItemDupe.punishText"),
                    itemID);

            for (Object idRow : ServerPlugin.getDefaultConfig().getList("antiItemDupe.blackList")) {
                String blackID = (String) idRow;

                if (item.getFullType().toLowerCase().contains(blackID.toLowerCase())) {
                    GeneralTools.punishPlayer(
                            ServerPlugin.getDefaultConfig().getInt("antiItemDupe.punishType"),
                            player,
                            punishText);
                    return;
                }
            }

            Integer count = itemsCount.getOrDefault(itemID, 0);
            count++;
            itemsCount.put(itemID, count);
        }

        for (Object maxItemRow : ServerPlugin.getDefaultConfig().getList("antiItemDupe.maxCountItems")) {
            String dataMaxItem = (String) maxItemRow;

            String[] argsData = dataMaxItem.split(":");
            String itemID = argsData[0].toLowerCase().trim();
            int maxCount = 1;
            try {
                maxCount = Integer.parseInt(argsData[1].trim());
            } catch (NumberFormatException numberFormatException) {
                continue;
            }

            Integer count = itemsCount.getOrDefault(itemID, 0);
            String punishText = String.format("%s | ID: %s | Count: %s",
                    ServerPlugin.getDefaultConfig().getString("antiItemDupe.punishText"),
                    itemID,
                    count);

            if (count >= maxCount) {
                GeneralTools.punishPlayer(
                        ServerPlugin.getDefaultConfig().getInt("antiItemDupe.punishType"),
                        player,
                        punishText);
                return;
            }
        }
    }

    /**
     * Requesting inventory from players
     */
    public static void updatePlayersInventory() {
        if (GameServer.udpEngine == null) return;

        ArrayList<IsoPlayer> players = GameServer.getPlayers();
        if (players.isEmpty()) return;

        for (IsoPlayer player : players) {
            UdpConnection playerConnection = GameServer.getConnectionFromPlayer(player);
            if (playerConnection == null) continue;

            InventoryTools.requestPlayerInventory(playerConnection);
        }
    }

    /**
     * Initializing periodic updating of players' inventory (synchronization)
     */
    public static void initInventoryUpdate() {
        if (!ServerPlugin.getDefaultConfig().getBoolean("antiItemDupe.isEnable")) return;

        ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
        schedule.scheduleAtFixedRate(ItemDupe::updatePlayersInventory,
                10,
                ServerPlugin.getDefaultConfig().getInt("antiItemDupe.updateTime"),
                TimeUnit.MILLISECONDS);
    }
}
