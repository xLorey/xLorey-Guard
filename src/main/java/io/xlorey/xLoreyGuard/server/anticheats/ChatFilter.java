package io.xlorey.xLoreyGuard.server.anticheats;

import io.xlorey.fluxloader.server.api.PlayerUtils;
import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.utils.GeneralTools;
import zombie.characters.IsoPlayer;
import zombie.chat.ChatBase;
import zombie.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Deknil
 * GitHub: <a href=https://github.com/Deknil>https://github.com/Deknil</a>
 * Date: 17.02.2024
 * Description: Chat filter from using prohibited words!
 * <p>xLoreyGuard © 2024. All rights reserved.</p>
 */
public class ChatFilter {
    /**
     * Called Event Handling Method
     * @param chatBase Target chat data.
     * @param chatMessage Sent message details.
     */
    public static void handlePacket(ChatBase chatBase, ChatMessage chatMessage) {
        String text = chatMessage.getText();
        String[] words = text.split(" ");
        IsoPlayer player = PlayerUtils.getPlayerByUsername(chatMessage.getAuthor());

        if (player == null) return;

        if (!ServerPlugin.getDefaultConfig().getBoolean("chatFilter.isEnable") || GeneralTools.isPlayerHasRights(player)) return;

        for (String word : words) {
            if (!isForbiddenWord(word)) continue;

            Logger.print(String.format("AC > Player '%s' used the forbidden word '%s' in chat!",
                    player.getUsername(),
                    word));

            GeneralTools.punishPlayer(ServerPlugin.getDefaultConfig().getInt("chatFilter.punishType"),
                    player,
                    ServerPlugin.getDefaultConfig().getString("chatFilter.punishText"));

            return;
        }
    }

    /**
     * Checking a word for its presence in prohibited lists
     * @param word the word to check
     * @return true - the word is prohibited, false - the word is allowed
     */
    private static boolean isForbiddenWord(String word) {
        ArrayList<String> whiteWords = new ArrayList<>(List.of(ServerPlugin.getDefaultConfig().getString("chatFilter.whiteListWords")
                .trim()
                .replace("\n","")
                .replace(" ", "")
                .split(",")));

        ArrayList<String> blackWords = new ArrayList<>(List.of(ServerPlugin.getDefaultConfig().getString("chatFilter.blackListWords")
                .trim()
                .replace("\n","")
                .replace(" ", "")
                .split(",")));

        if (whiteWords.contains(word.toLowerCase())) return false;

        if (blackWords.contains(word.toLowerCase())) return true;

        for(Object templateObj : ServerPlugin.getDefaultConfig().getList("chatFilter.patternListWords")) {
            String template = (String) templateObj;
            Pattern pattern = Pattern.compile(template);
            Matcher matcher = pattern.matcher(word);

            if (matcher.find()) return true;
        }

        return false;
    }
}
