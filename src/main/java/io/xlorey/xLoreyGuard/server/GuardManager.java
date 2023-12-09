package io.xlorey.xLoreyGuard.server;

import io.xlorey.FluxLoader.server.api.PlayerUtils;
import io.xlorey.FluxLoader.server.core.IncomingPacket;
import io.xlorey.FluxLoader.utils.Logger;
import zombie.GameWindow;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.inventory.InventoryItem;
import zombie.inventory.InventoryItemFactory;
import zombie.inventory.ItemContainer;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;
import zombie.network.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * AntiCheat toolkit
 */
public class GuardManager {
    private static final HashMap<String, Long> lastPlaceOnMapList = new HashMap<>();

    /**
     * Request to send inventory to server
     * @param playerConnection player connection
     */
    public static void updatePlayerInventory(UdpConnection playerConnection){
        short senderOnlineID = -1;
        short targetOnlineID = PlayerUtils.getPlayerByUdpConnection(playerConnection).getOnlineID();
        Long mapPlayerID = GameServer.IDToAddressMap.get(targetOnlineID);
        if (mapPlayerID != null) {
            for(int index = 0; index < GameServer.udpEngine.connections.size(); ++index) {
                UdpConnection connection = GameServer.udpEngine.connections.get(index);
                if (connection.getConnectedGUID() == mapPlayerID) {
                    ByteBufferWriter bufferWriter = connection.startPacket();
                    PacketTypes.PacketType.RequestInventory.doPacket(bufferWriter);
                    bufferWriter.putShort(senderOnlineID);
                    PacketTypes.PacketType.RequestInventory.send(connection);
                    break;
                }
            }
        }
    }

    /**
     * Player Packet Processing
     * @param connection player connection
     * @param packet package from the player
     */
    public static void enforce(UdpConnection connection, ByteBuffer originalBuffer, ZomboidNetData packet) {
        PacketTypes.PacketType packetType = packet.type;

        IsoPlayer player = PlayerUtils.getPlayerByUsername(connection.username);

        if (player == null)
            return;

        if (!(packetType.equals(PacketTypes.PacketType.PlayerUpdate) || packetType.equals(PacketTypes.PacketType.SendInventory))) {
            if (player.isAccessLevel("admin") || player.isAccessLevel("moderator")) {
                return;
            }
        }

        switch (packetType) {
            /*
              Player inventory manipulation
             */
            case InvMngGetItem, InvMngReqItem, InvMngRemoveItem, AddItemInInventory, AddXpFromPlayerStatsUI -> {
                IncomingPacket.blockPacket(originalBuffer);
                PlayerUtils.kickPlayer(player, "Suspicion of an attempt to send prohibited data!");
            }
            /*
              Inventory server synchronization
             */
            case SendInventory -> {
                ByteBuffer buffer = packet.buffer;
                short someShortValue = buffer.getShort();
                int itemCount = buffer.getInt();
                float capacityWeight = buffer.getFloat();
                float maxWeight = buffer.getFloat();

                ItemContainer playerInventory = new ItemContainer();
                for (int i = 0; i < itemCount; ++i) {
                    String module = GameWindow.ReadStringUTF(buffer);
                    String type = GameWindow.ReadStringUTF(buffer);
                    String itemType = module + "." + type;
                    String itemTypeMoveables = "Moveables." + type;

                    long itemId = buffer.getLong();
                    long parentId = buffer.getLong();
                    boolean isEquipped = buffer.get() == 1;
                    float someVariable = buffer.getFloat();
                    int count = buffer.getInt();
                    String category = GameWindow.ReadStringUTF(buffer);
                    String container = GameWindow.ReadStringUTF(buffer);
                    boolean isInInventory = buffer.get() == 1;

                    InventoryItem item = InventoryItemFactory.CreateItem(itemType);
                    item = (item != null) ? item : InventoryItemFactory.CreateItem(itemTypeMoveables);
                    if (item == null) {
                        Logger.print("Item is null with type: " + type);
                        continue;
                    }

                    item.setCount(count);
                    playerInventory.addItem(item);
                }
                player.setInventory(playerInventory);
            }
            /*
              Attempting to use prohibited tools
             */
            case ExtraInfo -> {
                IncomingPacket.blockPacket(originalBuffer);
                PlayerUtils.kickPlayer(player, "Suspicion of an attempt to use administration tools!");
            }
            /*
              Updating player data
             */
            case PlayerUpdate -> {
                updatePlayerInventory(connection);

                ArrayList<Boolean> inGameCheatsList = new ArrayList<>() {{
                    add(player.isGodMod());
                    add(player.isInvincible());
                    add(player.isInvisible());
                    add(player.isWearingNightVisionGoggles());
                    add(player.isNoClip());
                    add(player.isGhostMode());
                    add(player.isCheatPlayerSeeEveryone());
                    add(player.isBuildCheat());
                    add(player.isFarmingCheat());
                    add(player.isHealthCheat());
                    add(player.isMechanicsCheat());
                    add(player.isMovablesCheat());
                    add(player.isZombiesDontAttack());
                    add(player.isTimedActionInstantCheat());
                }};
                for(boolean isCheating : inGameCheatsList){
                    if (isCheating) {
                        IncomingPacket.blockPacket(originalBuffer);
                        PlayerUtils.kickPlayer(player, "Trying to use in-game cheats!");
                        break;
                    }
                }
            }
            /*
              Starting fire
             */
            case StartFire -> {
                if (!ServerOptions.getInstance().NoFire.getValue()) {
                    IncomingPacket.blockPacket(originalBuffer);
                    PlayerUtils.kickPlayer(player, "Suspicion of an attempt to create a prohibited fire!");
                }
            }
            /*
              Placing items on the map
             */
            case AddItemToMap -> {
                IsoObject object = WorldItemTypes.createFromBuffer(packet.buffer);
                object.loadFromRemoteBuffer(packet.buffer);

                String objectName = object.getName() != null ? object.getName() : object.getObjectName();
                boolean isInventoryObject = objectName.equals("WorldInventoryItem");
                String spriteName = object.getSprite().getName();
                if (object.getSprite() != null && object.getSprite().getName() != null) {
                    objectName = objectName + " (" + spriteName+ ")";
                }

                if (isInventoryObject) return;
                Logger.print(String.format("Player '%s' placed object '%s' at coordinates: [%s, %s, %s]",
                        player.getDisplayName(),
                        objectName,
                        object.getX(),
                        object.getY(),
                        object.getZ()));


                long placeTime = System.currentTimeMillis();
                long placeTimeFromCash = lastPlaceOnMapList.getOrDefault(player.username, 0L);
                boolean isFastPlace = placeTime - placeTimeFromCash <= 1500;

                IsoCell cell = object.getCell();
                IsoGridSquare square = object.getSquare();

                if (!isPlayerWithinDistance(player, object, 3) || isFastPlace) {
                    IncomingPacket.blockPacket(originalBuffer);
                    PlayerUtils.kickPlayer(player, "Suspicion of using cell drawing mode!");
                }


                lastPlaceOnMapList.put(player.username, placeTime);
            }
            /*
              Executing client commands via Lua
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

                if (!command.equals("ISLogSystem")){
                    Logger.print(String.format("Player '%s' enter the command '%s' '%s'",
                            player.getDisplayName(),
                            command,
                            method));
                }

                for (String cheatCmd : vehicleCheats) {
                    if (method.equals(cheatCmd)){
                        IncomingPacket.blockPacket(originalBuffer);
                        PlayerUtils.kickPlayer(player, "Suspicion of an attempt to use cheats on transport!");
                    }
                }
            }
        }
    }

    /**
     * Checking if the player is within a certain point
     */
    private static boolean isPlayerWithinDistance(IsoPlayer player, IsoObject object, float distance) {
        float deltaX = Math.abs(player.getX() - object.getX());
        float deltaY = Math.abs(player.getY() - object.getY());
        float deltaZ = Math.abs(player.getZ() - object.getZ());

        float squaredDistance = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
        float squaredCheckDistance = distance * distance;

        return squaredDistance <= squaredCheckDistance;
    }

}
