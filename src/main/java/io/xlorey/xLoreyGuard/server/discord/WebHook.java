package io.xlorey.xLoreyGuard.server.discord;

import com.google.gson.JsonObject;
import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import zombie.characters.IsoPlayer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: Deknil
 * GitHub: <a href=https://github.com/Deknil>https://github.com/Deknil</a>
 * Date: 17.02.2024
 * Description: Tool for sending messages via Discord WebHook
 * <p>xLoreyGuard © 2024. All rights reserved.</p>
 */
public class WebHook {
    /**
     * Sending a message via DiscordWebHook
     * @param player player object
     * @param reason message text
     */
    public static void sendMessage(IsoPlayer player, String adminName, String reason) {
        if (player == null) return;

        if (!ServerPlugin.getDefaultConfig().getBoolean("discordAlert.isEnable")) return;

        String text = formatTemplate(player, adminName, reason).trim();
        String webHookUrl = ServerPlugin.getDefaultConfig().getString("discordAlert.webHookURL").trim();
        String avatarURL = ServerPlugin.getDefaultConfig().getString("discordAlert.botAvatarURL").trim();
        String botUsername = ServerPlugin.getDefaultConfig().getString("discordAlert.botUsername").trim();

        try {
            JsonObject json = new JsonObject();
            json.addProperty("content", text);
            json.addProperty("username", botUsername);
            json.addProperty("avatar_url", avatarURL);

            String jsonString = json.toString();
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webHookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(System.out::println)
                    .join();
        } catch (Exception e) {
            Logger.print("AC > An error occurred while sending a message to discord:" + e.getMessage());
        }
    }

    /**
     * Formatting a message template for data
     * @param player player object
     * @param reason reason for punishment
     * @return formatted text
     */
    private static String formatTemplate(IsoPlayer player, String adminName, String reason) {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        String formattedDate = dateFormat.format(currentDate);

        return ServerPlugin.getDefaultConfig().getString("discordAlert.messageTemplate")
                .replace("<PLAYER_NAME>", player.getUsername())
                .replace("<DATE>", formattedDate)
                .replace("<ADMIN_NAME>", adminName.isEmpty() ? "Console" : adminName )
                .replace("<REASON>", reason.isEmpty() ? "-" : reason);
    }
}
