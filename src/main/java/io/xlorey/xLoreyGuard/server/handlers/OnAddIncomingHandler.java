package io.xlorey.xLoreyGuard.server.handlers;

import io.xlorey.fluxloader.events.OnAddIncoming;
import io.xlorey.fluxloader.server.api.PlayerUtils;
import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.anticheats.*;
import io.xlorey.xLoreyGuard.server.utils.InventoryTools;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;
import zombie.network.ZomboidNetData;
import zombie.network.ZomboidNetDataPool;

import java.nio.ByteBuffer;

/**
 * Author: Deknil
 * Date: 14.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Handler of incoming packets from the player to the server
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
 */
public class OnAddIncomingHandler extends OnAddIncoming {
    /**
     * Handler of incoming packets from the player
     * @param opcode incoming packet opcode
     * @param data information about the incoming packet in ByteBuffer format
     * @param playerConnection player connection information
     */
    @Override
    public void handleEvent(Short opcode, ByteBuffer data, UdpConnection playerConnection) {
        if (data == null || data.limit() == 0) return;

        IsoPlayer player = PlayerUtils.getPlayerByUdpConnection(playerConnection);

        if (player == null) return;

        ZomboidNetData packet;

        if (data.limit() > 2048) {
            packet = ZomboidNetDataPool.instance.getLong(data.limit());
        } else {
            packet = ZomboidNetDataPool.instance.get();
        }

        packet.read(opcode, data, playerConnection);
        data.rewind();

        PacketTypes.PacketType packetType = packet.type;

        try {
            switch (packetType) {
//              case ActionPacket -> PlayerAction.handlePacket(packet, player, playerConnection);
                case AddItemToMap -> BrushTool.handlePacket(packet, player, playerConnection);
                case ClientCommand -> VehicleCommand.handlePacket(packet, player, playerConnection);
                case ExtraInfo -> ExtraInfo.handlePacket(packet, player, playerConnection);
                case SyncXP -> Skills.handlePacket(packet, player, playerConnection);
                case AddXP -> Experience.handlePacket(packet, player, playerConnection);
                case SendInventory -> {
                    InventoryTools.updatePlayerInventory(player, packet);
                    ItemDupe.handlePacket(packet, player, playerConnection);
                }
            }
        } catch (Exception e) {
            Logger.print(String.format("AC > An error occurred while processing package '%s': %s",
                    packetType.name(), e.getMessage()));
            e.printStackTrace();
        }
    }
}
