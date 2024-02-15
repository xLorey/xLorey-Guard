package io.xlorey.xLoreyGuard.server.utils;

import io.xlorey.fluxloader.server.api.PlayerUtils;
import io.xlorey.fluxloader.utils.Logger;
import zombie.GameWindow;
import zombie.characters.IsoPlayer;
import zombie.core.network.ByteBufferWriter;
import zombie.core.raknet.UdpConnection;
import zombie.inventory.InventoryItem;
import zombie.inventory.InventoryItemFactory;
import zombie.inventory.ItemContainer;
import zombie.network.GameServer;
import zombie.network.PacketTypes;
import zombie.network.ZomboidNetData;

import java.nio.ByteBuffer;

/**
 * A set of tools for managing and synchronizing a player’s inventory with server data
 */
public class InventoryTools {
    /**
     * Request to send inventory to server
     * @param playerConnection player connection
     */
    public static void requestPlayerInventory(UdpConnection playerConnection) {
        short senderOnlineID = -1;
        IsoPlayer player = PlayerUtils.getPlayerByUdpConnection(playerConnection);

        if (player == null) {
            return;
        }

        short targetOnlineID = player.getOnlineID();
        Long mapPlayerID = GameServer.IDToAddressMap.get(targetOnlineID);

        if (mapPlayerID != null) {
            for (int index = 0; index < GameServer.udpEngine.connections.size(); ++index) {
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
     * Updates server data about the player's inventory
     * @param player the player being checked
     * @param packet player network data
     */
    public static void updatePlayerInventory(IsoPlayer player, ZomboidNetData packet){
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
}
