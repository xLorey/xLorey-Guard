package io.xlorey.xLoreyGuard.server.handlers;

import io.xlorey.fluxloader.events.OnChatMessageProcessed;
import io.xlorey.fluxloader.utils.Logger;
import zombie.chat.ChatBase;
import zombie.chat.ChatMessage;

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
//        Logger.print("New message! > " + chatMessage.getAuthor() + " < | " + chatMessage.getText());
    }
}
