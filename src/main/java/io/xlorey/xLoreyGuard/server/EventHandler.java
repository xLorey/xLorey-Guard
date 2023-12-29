package io.xlorey.xLoreyGuard.server;

import io.xlorey.FluxLoader.annotations.SubscribeEvent;
import io.xlorey.FluxLoader.server.api.PlayerUtils;
import io.xlorey.FluxLoader.utils.Logger;
import io.xlorey.xLoreyGuard.server.utils.GeneralTools;
import io.xlorey.xLoreyGuard.server.utils.InventoryTools;
import zombie.GameWindow;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.skills.PerkFactory;
import zombie.core.raknet.UdpConnection;
import zombie.iso.IsoObject;
import zombie.network.*;
import zombie.network.packets.PlayerID;
import zombie.network.packets.hit.Perk;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Set of event handlers
 */
public class EventHandler {
    /**
     * List of times for placing game objects on players
     */
    private final HashMap<String, long[]> placementMap = new HashMap<>();

    /**
     * Example implementation of event subscription. Called after the server is fully initialized
     * @param serverStartupArgs server startup arguments
     */
    @SubscribeEvent(eventName="onServerInitialize")
    public void onServerInitializeHandler(String[] serverStartupArgs){
        Logger.print("The server protection system has been initialized!");
    }

    /**
     * Handling the player's full connection to the server
     * @param playerData player data in ByteBuffer form
     * @param playerConnection player connection information
     * @param username player nickname
     */
    @SubscribeEvent(eventName="onPlayerFullyConnected")
    public void onPlayerConnectHandler(ByteBuffer playerData, UdpConnection playerConnection, String username){
       if (ServerPlugin.getDefaultConfig().getBoolean("syncPlayerInventory")) {
           InventoryTools.requestPlayerInventory(playerConnection);
       }
    }

    /**
     * Handler of incoming packets from the player
     * @param opcode incoming packet opcode
     * @param data information about the incoming packet in ByteBuffer format
     * @param connection player connection information
     */
    @SubscribeEvent(eventName="onAddIncoming")
    public void onAddIncomingHandler(Short opcode, ByteBuffer data, UdpConnection connection){
        try {
            ZomboidNetData packet = ZomboidNetDataPool.instance.getLong(data.limit());

            data.mark();
            packet.read(opcode, data, connection);
            data.reset();

            PacketTypes.PacketType packetType = packet.type;

            IsoPlayer player = PlayerUtils.getPlayerByUsername(connection.username);

            if (player == null)
                return;

            switch (packetType) {
                /*
                Sending inventory upon request (needed for synchronization)
                 */
                case SendInventory -> {
                    if (ServerPlugin.getDefaultConfig().getBoolean("syncPlayerInventory")) {
                        InventoryTools.updatePlayerInventory(player, packet);
                    }
                }

                /*
                Sending additional data to the server (only used by cheaters)
                 */
                case ExtraInfo -> {
                    if (ServerPlugin.getDefaultConfig().getBoolean("antiCheatType.inGameCheats") && !GeneralTools.isAuthorizedPacket(player)){
                        GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("antiCheatPunishment.inGameCheats"),
                                player,
                                ServerPlugin.getDefaultConfig().getString("translation.inGameCheats"),
                                data);
                    }
                }

                /*
                Player state update
                 */
                case PlayerUpdate -> {
                    if (ServerPlugin.getDefaultConfig().getBoolean("syncPlayerInventory")) {
                        InventoryTools.requestPlayerInventory(connection);
                    }
                }

                /*
                Fire manipulation
                 */
                case StartFire, StopFire -> {
                    if (ServerPlugin.getDefaultConfig().getBoolean("antiCheatType.fireCheats") && !GeneralTools.isAuthorizedPacket(player) && !ServerOptions.getInstance().NoFire.getValue()){
                        GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("antiCheatPunishment.fireCheats"),
                                player,
                                ServerPlugin.getDefaultConfig().getString("translation.fireCheats"),
                                data);
                    }
                }

                /*
                Manipulations with other people's inventory
                 */
                case InvMngGetItem, InvMngReqItem, RequestInventory, InvMngRemoveItem, AddItemInInventory -> {
                    if (ServerPlugin.getDefaultConfig().getBoolean("antiCheatType.inventoryManipulations") && !GeneralTools.isAuthorizedPacket(player)){
                        GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("antiCheatPunishment.inventoryManipulations"),
                                player,
                                ServerPlugin.getDefaultConfig().getString("translation.inventoryManipulations"),
                                data);
                    }
                }

                /*
                Sending commands (via code) to the server
                 */
                case ClientCommand -> {
                    ByteBuffer byteBuffer = packet.buffer;
                    byte index = byteBuffer.get();
                    String command = GameWindow.ReadString(byteBuffer);
                    String method = GameWindow.ReadString(byteBuffer);
                    boolean isTable = byteBuffer.get() == 1;

                    ArrayList<String> vehicleCheats = new ArrayList<>() {{
                        add("getKey");
                        add("cheatHotwire");
                        add("repairPart");
                        add("repair");
                        add("setRust");
                        add("setPartCondition");
                        add("remove");
                    }};

                    if (ServerPlugin.getDefaultConfig().getBoolean("logClientCommand") && !command.equals("ISLogSystem")){
                        Logger.print(String.format("Player '%s' enter the command '%s' with method '%s'",
                                player.getUsername(),
                                command,
                                method));
                    }

                    if (ServerPlugin.getDefaultConfig().getBoolean("antiCheatType.commandCheats") && !GeneralTools.isAuthorizedPacket(player)){
                        for (String cheatCmd : vehicleCheats) {
                            if (method.equals(cheatCmd)){
                                GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("antiCheatPunishment.commandCheats"),
                                        player,
                                        ServerPlugin.getDefaultConfig().getString("translation.commandCheats"),
                                        data);
                            }
                        }
                    }
                }

                /*
                Placing objects on the game map
                 */
                case AddItemToMap -> {
                    try {
                        IsoObject object = WorldItemTypes.createFromBuffer(packet.buffer);
                        object.loadFromRemoteBuffer(packet.buffer);

                        String objectName = object.getName() != null ? object.getName() : object.getObjectName();
                        boolean isInventoryObject = objectName.equals("WorldInventoryItem");
                        String spriteName = object.getSprite().getName();
                        if (object.getSprite() != null && object.getSprite().getName() != null) {
                            objectName = objectName + " (" + spriteName + ")";
                        }

                        if (isInventoryObject) return;

                        if (ServerPlugin.getDefaultConfig().getBoolean("logPlaceObject")) {
                            Logger.print(String.format("Player '%s' placed object '%s' at coordinates: [%s, %s, %s]",
                                    player.getUsername(),
                                    objectName,
                                    object.getX(),
                                    object.getY(),
                                    object.getZ()));
                        }

                        for (Object blackListSprite : ServerPlugin.getDefaultConfig().getList("antiCheatSettings.blackListSpriteObjects")) {
                            String blackListSpriteName = (String) blackListSprite;

                            if (blackListSpriteName == null || blackListSpriteName.isEmpty()) continue;

                            if (spriteName.equalsIgnoreCase(blackListSpriteName)) {
                                GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("antiCheatPunishment.brushTool"),
                                        player,
                                        ServerPlugin.getDefaultConfig().getString("translation.brushTool"),
                                        data);
                            }
                        }


                        long currentTime = System.currentTimeMillis();
                        long[] placeData = placementMap.getOrDefault(player.username, new long[]{0, 0});
                        long lastPlaceTime = placeData[0];
                        int objectCount = (int) placeData[1];

                        if (currentTime - lastPlaceTime <= ServerPlugin.getDefaultConfig().getInt("antiCheatSettings.brushToolTimePlace")) {
                            objectCount++;
                        } else {
                            objectCount = 1;
                        }

                        placeData[0] = currentTime;
                        placeData[1] = objectCount;
                        placementMap.put(player.username, placeData);

                        boolean isFastPlace = objectCount > 4;

                        int brushToolDistance = ServerPlugin.getDefaultConfig().getInt("antiCheatSettings.brushToolDistance");
                        brushToolDistance =  brushToolDistance != 0 ? brushToolDistance : 3;

                        if (ServerPlugin.getDefaultConfig().getBoolean("antiCheatType.brushTool") && !GeneralTools.isAuthorizedPacket(player)) {
                            if (GeneralTools.getDistanceBetweenObject(player, object) > brushToolDistance || isFastPlace) {
                                GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("antiCheatPunishment.brushTool"),
                                        player,
                                        ServerPlugin.getDefaultConfig().getString("translation.brushTool"),
                                        data);
                            }
                        }
                    } catch (Exception e) {
                        return;
                    }
                }

                /*
                Sync exp changes
                 */
                case AddXP -> {
                    if (ServerPlugin.getDefaultConfig().getBoolean("logAddXp")) {
                        ByteBuffer byteBuffer = packet.buffer;
                        PlayerID target = new PlayerID();
                        target.parse(byteBuffer, connection);
                        target.parsePlayer(connection);
                        Perk perk = new Perk();
                        perk.parse(byteBuffer, connection);
                        int amount = byteBuffer.getInt();

                        Logger.print(String.format("Player '%s' gained %s experience in skill '%s'", player.getUsername(), amount, perk.getPerk().getName()));
                    }
                }

                /*
                Sync skill changes
                 */
                case SyncXP -> {
                    if (ServerPlugin.getDefaultConfig().getBoolean("antiCheatType.expCheats") && !GeneralTools.isAuthorizedPacket(player)) {
                        ByteBuffer byteBuffer = packet.buffer;
                        short playerID = byteBuffer.getShort();

                        ArrayList<IsoGameCharacter.PerkInfo> perkListSaved = new ArrayList<>(player.getPerkList());
                        HashMap<PerkFactory.Perk, Float> savedPlayerXP = new HashMap<>(player.getXp().XPMap);

                        player.getXp().load(byteBuffer, 195);

                        ArrayList<IsoGameCharacter.PerkInfo> perkListNew = new ArrayList<>(player.getPerkList());

                        if (perkListSaved.size() != perkListNew.size()) {
                            Logger.print(String.format("Player '%s' perk list sizes are inconsistent.", player.getUsername()));
                            return;
                        }

                        for (int i = 0; i < perkListSaved.size(); i++) {
                            IsoGameCharacter.PerkInfo savedPerkInfo = perkListSaved.get(i);
                            IsoGameCharacter.PerkInfo newPerkInfo = perkListNew.get(i);

                            if (!savedPerkInfo.perk.equals(newPerkInfo.perk)) {
                                Logger.print(String.format("Player '%s' has a mismatch between old and new skills at position %s", player.getUsername(), i));
                                continue;
                            }

                            Float oldXPFloat = savedPlayerXP.get(savedPerkInfo.perk);
                            if (oldXPFloat == null) continue;

                            float oldXP = savedPlayerXP.get(savedPerkInfo.perk);
                            float newXP = player.getXp().getXP(newPerkInfo.perk);

                            if (savedPerkInfo.level != newPerkInfo.level) {
                                String levelChangeMessage = String.format("[SKILL] Player '%s' has had his skill '%s' changed: Old level: %d, New level: %d.",
                                        player.username, savedPerkInfo.perk.getName(), savedPerkInfo.level, newPerkInfo.level);
                                Logger.print(levelChangeMessage);
                            }

                            if (oldXP != newXP) {
                                String xpChangeMessage = String.format("[SKILL] Player '%s' skill '%s' experience changed from '%.1f' to '%.1f'.",
                                        player.username, newPerkInfo.perk.getName(), oldXP, newXP);
                                Logger.print(xpChangeMessage);

                                int levelForXP = Math.max(1, Math.min(newPerkInfo.level, 10));

                                float requiredXPForNewLevel = newPerkInfo.perk.getXpForLevel(levelForXP);

                                double configXPChangePercentage = ServerPlugin.getDefaultConfig().getDouble("antiCheatSettings.acceptableXPChangePercentage");
                                float acceptableXPChangePercentage = configXPChangePercentage > 0f ? (float) configXPChangePercentage : 0.6f;

                                int percentageXp = (int) (acceptableXPChangePercentage * 100);

                                if (Math.abs(newXP - oldXP) > requiredXPForNewLevel * acceptableXPChangePercentage) {
                                    Logger.print(String.format("[SKILL] Player '%s' changed skill experience '%s' from '%.1f' to '%.1f'. Difference of over %d%%!",
                                            player.username, newPerkInfo.perk.getName(), oldXP, newXP, percentageXp));

                                    GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("antiCheatPunishment.expCheats"),
                                            player,
                                            ServerPlugin.getDefaultConfig().getString("translation.expCheats"),
                                            data);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.print(String.format("Packet processing error '%s': %s", connection.username, e.getMessage()));
        }
    }
}