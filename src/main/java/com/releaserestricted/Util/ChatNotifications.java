package com.releaserestricted.Util;

import com.releaserestricted.ReleaseRestrictedConfig;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;

public  class ChatNotifications {

    public static void sendChatMessage(String chatMessage, Client client, ReleaseRestrictedConfig config, ChatMessageType chatMessageType)
    {
        client.addChatMessage(chatMessageType, "", chatMessage, null);
    }

    public static void sendChatNotification(String chatNotification, ChatMessageManager chatMessageManager){

        final String message = new ChatMessageBuilder()
                .append(ChatColorType.HIGHLIGHT)
                .append(chatNotification)
                .build();

        chatMessageManager.queue(
                QueuedMessage.builder()
                        .type(ChatMessageType.CONSOLE)
                        .runeLiteFormattedMessage(message)
                        .build());

    }

    public static void configNotification(ReleaseRestrictedConfig config, ChatMessageManager chatMessageManager){
        //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Content is restricted beyond year " + config.year(), null);
        sendChatNotification("Content is restricted beyond year " + config.year(), chatMessageManager);

    }

}
