package io.xlorey.xLoreyGuard.server.anticheats;

import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.utils.GeneralTools;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoObject;
import zombie.iso.IsoWorld;
import zombie.network.WorldItemTypes;
import zombie.network.ZomboidNetData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Deknil
 * Date: 15.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Anti-cheat that prevents players from using the Brush Tool
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
 */
public class BrushTool {
    /**
     * List of times for placing game objects on players
     */
    private static final Map<String, long[]> placementMap = new HashMap<>();

    /**
     * Performing anti-cheat actions when receiving a new package
     * @param packet           received packet from the player
     * @param player           player object
     * @param playerConnection active player connection
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        if (!ServerPlugin.getDefaultConfig().getBoolean("settings.antiBrushTool.isEnable") || GeneralTools.isPlayerHasRights(player)) return;

        IsoObject object = WorldItemTypes.createFromBuffer(packet.buffer);

        try {
            object.load(packet.buffer, 195);
        } catch (IOException e) {
            Logger.print("Error loading package data to generate an object for installation on the map! Player from packet: " + playerConnection.username);
            return;
        }

        int x = packet.buffer.getInt();
        int y = packet.buffer.getInt();
        int z = packet.buffer.getInt();

        object.square = IsoWorld.instance.CurrentCell.getGridSquare(x, y, z);

        if (object.square == null) return;

        object.removeFromSquare();
        object.removeFromWorld();
        object.removeAllContainers();

        String objectName = object.getName() != null ? object.getName() : object.getObjectName();
        boolean isInventoryObject = objectName.equals("WorldInventoryItem");
        String spriteName = object.getSprite().getName();
        if (object.getSprite() != null && object.getSprite().getName() != null) {
            objectName = objectName + " (" + spriteName + ")";
        }

        if (isInventoryObject) return;

        Logger.print(String.format("Player '%s' placed object '%s' at coordinates: [%s, %s, %s]",
                player.getUsername(),
                objectName,
                object.getX(),
                object.getY(),
                object.getZ()));

        for (Object blackListSprite : ServerPlugin.getDefaultConfig().getList("settings.antiBrushTool.blackListSprite")) {
            String blackListSpriteName = (String) blackListSprite;

            if (blackListSpriteName == null || blackListSpriteName.isEmpty()) continue;

            if (spriteName.equalsIgnoreCase(blackListSpriteName)) {
                GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("settings.antiBrushTool.punishType"),
                        player,
                        ServerPlugin.getDefaultConfig().getString("settings.antiBrushTool.punishText"));

                return;
            }
        }

        long currentTime = System.currentTimeMillis();
        long[] placeData = placementMap.getOrDefault(player.username, new long[]{0, 0});
        long lastPlaceTime = placeData[0];
        int objectCount = (int) placeData[1];

        if (currentTime - lastPlaceTime <= ServerPlugin.getDefaultConfig().getInt("settings.antiBrushTool.placeTimeLimit")) {
            objectCount++;
        } else {
            objectCount = 1;
        }

        placeData[0] = currentTime;
        placeData[1] = objectCount;
        placementMap.put(player.username, placeData);

        boolean isFastPlace = objectCount > ServerPlugin.getDefaultConfig().getInt("settings.antiBrushTool.maxObjectPlace");

        int minDistance = ServerPlugin.getDefaultConfig().getInt("settings.antiBrushTool.minDistance");
        int maxDistance = ServerPlugin.getDefaultConfig().getInt("settings.antiBrushTool.maxDistance");
        minDistance =  minDistance != 0 ? minDistance : 3;
        maxDistance =  maxDistance != 0 ? maxDistance : 70;

        float distance = GeneralTools.getDistanceBetweenObject(player, object);
        if ((distance > minDistance && distance < maxDistance) || isFastPlace) {
            GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("settings.antiBrushTool.punishType"),
                    player,
                    ServerPlugin.getDefaultConfig().getString("settings.antiBrushTool.punishText"));
        }
    }
}
