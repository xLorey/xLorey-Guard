package io.xlorey.xLoreyGuard.server.anticheats;

import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.utils.GeneralTools;
import zombie.GameWindow;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Author: Deknil
 * Date: 15.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Anti-cheat that prevents players from using the Vehicle Cheat
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
 */
public class VehicleCommand {
    /**
     * Performing anti-cheat actions when receiving a new packet
     * @param packet           received packet from the player
     * @param player           player object
     * @param playerConnection active player connection
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
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

        if (ServerPlugin.getDefaultConfig().getBoolean("general.isLogClientCommand") && !command.equals("ISLogSystem")){
            Logger.print(String.format("AC > Player '%s' enter the command '%s' with method '%s'",
                    player.getUsername(),
                    command,
                    method));
        }

        if (ServerPlugin.getDefaultConfig().getBoolean("vehicleAntiCheat.isEnable") && !GeneralTools.isPlayerHasRights(player)){
            for (String cheatCmd : vehicleCheats) {
                if (method.equals(cheatCmd)){
                    GeneralTools.punishPlayer(
                            ServerPlugin.getDefaultConfig().getInt("vehicleAntiCheat.punishType"),
                            player,
                            ServerPlugin.getDefaultConfig().getString("vehicleAntiCheat.punishText"));
                }
            }
        }
    }
}
