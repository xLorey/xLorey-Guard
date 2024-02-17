package io.xlorey.xLoreyGuard.server.handlers;

import io.xlorey.fluxloader.events.OnChatMessageProcessed;
import io.xlorey.fluxloader.server.api.PlayerUtils;
import io.xlorey.fluxloader.utils.Logger;
import io.xlorey.xLoreyGuard.server.ServerPlugin;
import io.xlorey.xLoreyGuard.server.anticheats.ChatFilter;
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
 * Date: 14.02.2024
 * GitHub: <a href="https://github.com/Deknil">https://github.com/Deknil</a>
 * Description: Handler for incoming chat messages from a player
 * <p> xLoreyGuard © 2024. All rights reserved. </p>
 */
public class OnChatMessageProcessedHandler extends OnChatMessageProcessed {
    /**
     * Called Event Handling Method
     * @param chatBase Target chat data.
     * @param chatMessage Sent message details.
     */
    @Override
    public void handleEvent(ChatBase chatBase, ChatMessage chatMessage) {
        if (chatMessage == null || chatBase == null) return;

        ChatFilter.handlePacket(chatBase, chatMessage);
    }
}
