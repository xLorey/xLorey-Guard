package io.xlorey.xLoreyGuard.server;

import io.xlorey.FluxLoader.annotations.SubscribeEvent;
import io.xlorey.FluxLoader.utils.Logger;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;
import zombie.network.ZomboidNetDataPool;

import java.nio.ByteBuffer;

/**
 * Set of event handlers
 */
public class EventsAdapter {
    /**
     * Example implementation of event subscription. Called after the server is fully initialized
     */
    @SubscribeEvent(eventName="onServerInitialize")
    public void onServerInitializeHandler(String[] serverStartupArgs){
        Logger.print("The server protection system has been initialized!");
    }

    /**
     * Player full connection event
     * @param playerData player data
     * @param playerConnection player connection
     * @param username player nickname
     */
    @SubscribeEvent(eventName="onPlayerFullyConnected")
    public void onPlayerConnectHandler(ByteBuffer playerData, UdpConnection playerConnection, String username){
        GuardManager.updatePlayerInventory(playerConnection);
    }

    /**
     * Packet Processing Event
     * @param opcode package code
     * @param data package data
     * @param connection connection
     */
    @SubscribeEvent(eventName="onAddIncoming")
    public void onAddIncomingHandler(Short opcode, ByteBuffer data, UdpConnection connection){
        ZomboidNetData packet = ZomboidNetDataPool.instance.getLong(data.limit());
        data.mark();
        packet.read(opcode, data, connection);
        data.reset();
        try {
            GuardManager.enforce(connection, data, packet);
        } catch (Exception e) {
            Logger.print(String.format("Packet processing error '%s': %s", connection.username, e.getMessage()));
        }
    }
}
