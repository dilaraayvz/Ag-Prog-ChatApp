package com.alibou.websocket.chat;

import com.alibou.websocket.chatroom.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.alibou.websocket.util.EncryptionUtil;


import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;

    public ChatMessage save(ChatMessage chatMessage) {
        try {
            var chatId = chatRoomService
                    .getChatRoomId(chatMessage.getSenderId(), chatMessage.getRecipientId(), true)
                    .orElseThrow(); // You can create your own dedicated exception

            chatMessage.setChatId(chatId);
            chatMessage.setContent(EncryptionUtil.encrypt(chatMessage.getContent())); // Şifreleme
            repository.save(chatMessage);
            return chatMessage;
        } catch (Exception e) {
            throw new RuntimeException("Message encryption failed", e);
        }
    }

    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        try {
            var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
            List<ChatMessage> messages = chatId.map(repository::findByChatId).orElse(new ArrayList<>());

            for (ChatMessage message : messages) {
                message.setContent(EncryptionUtil.decrypt(message.getContent())); // Şifre çözme
            }

            return messages;
        } catch (Exception e) {
            throw new RuntimeException("Message decryption failed", e);
        }
    }
}
