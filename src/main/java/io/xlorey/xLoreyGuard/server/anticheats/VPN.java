package io.xlorey.xLoreyGuard.server.anticheats;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.xlorey.fluxloader.server.api.PlayerUtils;
import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.utils.GeneralTools;
import zombie.characters.IsoPlayer;
import zombie.core.raknet.UdpConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Author: Deknil
 * GitHub: <a href=https://github.com/Deknil>https://github.com/Deknil</a>
 * Date: 17.02.2024
 * Description: Blocking connections via VPN
 * <p>xLoreyGuard © 2024. All rights reserved.</p>
 */
public class VPN {
    /**
     * Performing anti-cheat actions when receiving a new packet
     */
    public static void handlePacket(UdpConnection playerConnection) {
        IsoPlayer player = PlayerUtils.getPlayerByUdpConnection(playerConnection);

        if (player == null) return;

        if (!ServerPlugin.getDefaultConfig().getBoolean("antiVPN.isEnable") || GeneralTools.isPlayerHasRights(player)) return;

        String playerIP = playerConnection.ip.trim();
        String API = ServerPlugin.getDefaultConfig().getString("antiVPN.API").trim();
        String apiUrl = "http://v2.api.iphub.info/ip/" + playerIP;

        Logger.print(String.format("AC > Checking player '%s' for VPN use by IP address: %s", playerConnection.username, playerIP));
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Key", API);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parsing JSON response using Gson
                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                int shouldBlock = jsonObject.get("block").getAsInt();

                if (shouldBlock == 1) {
                    GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("antiVPN.punishType"),
                            player,
                            ServerPlugin.getDefaultConfig().getString("antiVPN.punishText"));
                    return;
                }

                Logger.print(String.format("AC > Player '%s' has been verified and no VPN was detected!", playerConnection.username));
            }
        } catch (Exception ignored) {}
    }
}
