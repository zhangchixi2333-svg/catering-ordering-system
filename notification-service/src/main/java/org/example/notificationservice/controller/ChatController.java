package org.example.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.common.Result;
import org.example.notificationservice.client.UserOnlineStatusClient;
import org.example.notificationservice.dto.ChatMessageRequest;
import org.example.notificationservice.dto.ChatMessageResponse;
import org.example.notificationservice.entity.ChatMessage;
import org.example.notificationservice.entity.ChatMessageRecipient;
import org.example.notificationservice.mapper.ChatMessageMapper;
import org.example.notificationservice.mapper.ChatMessageRecipientMapper;
import org.example.notificationservice.util.MessageIdGenerator;
import org.example.notificationservice.websocket.NotificationWebSocket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/notification/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageRecipientMapper recipientMapper;
    private final UserOnlineStatusClient userOnlineStatusClient;

    @PostMapping("/send")
    public Result<ChatMessageResponse> sendMessage(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestHeader(value = "X-Username", required = false) String headerUsername,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestBody @Valid ChatMessageRequest request) {

        Long senderId = headerUserId != null ? headerUserId : request.getSenderId();
        if (senderId == null) {
            return Result.error(401, "无法识别发送人");
        }

        String senderName = firstNonBlank(headerUsername, request.getSenderName(), "用户" + senderId);
        String senderRole = firstNonBlank(headerRole, request.getSenderRole(), "USER");
        List<Long> recipients = normalizeRecipients(request.getRecipientIds(), senderRole, senderId);
        if (recipients.isEmpty()) {
            return Result.error("请选择接收人，不能给自己发送消息");
        }

        ChatMessageResponse message = new ChatMessageResponse();
        message.setMessageId(MessageIdGenerator.generate());
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setSenderRole(senderRole);
        message.setRecipientIds(recipients);
        message.setShopId(request.getShopId());
        message.setContent(request.getContent());
        message.setSendTime(LocalDateTime.now());

        Map<Long, Boolean> recipientOnlineStatus = new LinkedHashMap<>();
        message.setRecipientOnlineStatus(recipientOnlineStatus);

        boolean delivered = false;
        for (Long recipientId : recipients) {
            boolean recipientDelivered = NotificationWebSocket.isUserOnline(recipientId);
            if (recipientDelivered) {
                recipientDelivered = NotificationWebSocket.sendMessage(recipientId,
                        new NotificationWebSocket.NotificationMessage("CHAT_MESSAGE", "实时对话", message));
            }
            recipientOnlineStatus.put(recipientId, recipientDelivered);
            delivered = delivered || recipientDelivered;
        }
        message.setDelivered(delivered);

        saveChatMessage(message);
        NotificationWebSocket.sendMessage(senderId,
                new NotificationWebSocket.NotificationMessage("CHAT_MESSAGE_SENT", "消息已发送", message));

        return Result.success(message, delivered ? "消息已送达" : "对方不在线，消息已保存");
    }

    @GetMapping("/conversation")
    public Result<List<ChatMessageResponse>> getConversation(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "peerId", required = false) Long peerId,
            @RequestParam(value = "peerIds", required = false) List<Long> peerIds,
            @RequestParam(value = "limit", defaultValue = "100") Integer limit) {
        if (headerUserId == null) {
            return Result.error(401, "无法识别当前用户");
        }

        List<Long> peers = resolvePeers(headerUserId, peerId, peerIds);
        if (peers.isEmpty()) {
            return Result.success(List.of());
        }

        int boundedLimit = Math.max(1, Math.min(limit == null ? 100 : limit, 300));
        List<ChatMessage> messages = chatMessageMapper.findConversationMessages(headerUserId, peers, boundedLimit);
        return Result.success(toResponses(messages));
    }

    @GetMapping("/recent")
    public Result<List<ChatMessageResponse>> getRecent(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        if (headerUserId == null) {
            return Result.error(401, "无法识别当前用户");
        }
        int boundedLimit = Math.max(1, Math.min(limit == null ? 50 : limit, 200));
        return Result.success(toResponses(chatMessageMapper.findRecentMessages(headerUserId, boundedLimit)));
    }

    @GetMapping("/online")
    public Result<Map<Long, Boolean>> getOnlineStatus(@RequestParam("userIds") List<Long> userIds) {
        List<Long> normalizedUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Boolean> status = new LinkedHashMap<>(userOnlineStatusClient.getOnlineStatuses(normalizedUserIds));
        normalizedUserIds.forEach(userId -> status.putIfAbsent(userId, NotificationWebSocket.isUserOnline(userId)));
        return Result.success(status);
    }

    private void saveChatMessage(ChatMessageResponse message) {
        ChatMessage entity = new ChatMessage();
        entity.setMessageId(message.getMessageId());
        entity.setSenderId(message.getSenderId());
        entity.setSenderName(message.getSenderName());
        entity.setSenderRole(message.getSenderRole());
        entity.setShopId(message.getShopId());
        entity.setContent(message.getContent());
        entity.setDelivered(message.isDelivered() ? 1 : 0);
        entity.setSendTime(message.getSendTime());
        chatMessageMapper.insert(entity);

        for (Long recipientId : message.getRecipientIds()) {
            boolean delivered = Boolean.TRUE.equals(message.getRecipientOnlineStatus().get(recipientId));
            ChatMessageRecipient recipient = new ChatMessageRecipient();
            recipient.setMessageId(message.getMessageId());
            recipient.setRecipientId(recipientId);
            recipient.setDelivered(delivered ? 1 : 0);
            recipient.setDeliveredTime(delivered ? message.getSendTime() : null);
            recipient.setReadStatus(0);
            recipientMapper.insert(recipient);
        }
    }

    private List<ChatMessageResponse> toResponses(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        List<String> messageIds = messages.stream().map(ChatMessage::getMessageId).toList();
        Map<String, List<ChatMessageRecipient>> recipientMap = recipientMapper.findByMessageIds(messageIds).stream()
                .collect(Collectors.groupingBy(ChatMessageRecipient::getMessageId));

        return messages.stream()
                .map(message -> toResponse(message, recipientMap.getOrDefault(message.getMessageId(), List.of())))
                .sorted(Comparator.comparing(ChatMessageResponse::getSendTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private ChatMessageResponse toResponse(ChatMessage message, List<ChatMessageRecipient> recipients) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setMessageId(message.getMessageId());
        response.setSenderId(message.getSenderId());
        response.setSenderName(message.getSenderName());
        response.setSenderRole(message.getSenderRole());
        response.setShopId(message.getShopId());
        response.setContent(message.getContent());
        response.setSendTime(message.getSendTime());
        response.setDelivered(Integer.valueOf(1).equals(message.getDelivered()));
        response.setRecipientIds(recipients.stream().map(ChatMessageRecipient::getRecipientId).toList());

        Map<Long, Boolean> status = new LinkedHashMap<>();
        recipients.forEach(recipient ->
                status.put(recipient.getRecipientId(), Integer.valueOf(1).equals(recipient.getDelivered())));
        response.setRecipientOnlineStatus(status);
        return response;
    }

    private List<Long> normalizeRecipients(List<Long> recipientIds, String senderRole, Long senderId) {
        LinkedHashSet<Long> normalized = new LinkedHashSet<>();
        if (recipientIds != null) {
            recipientIds.stream()
                    .filter(Objects::nonNull)
                    .filter(id -> !Objects.equals(id, senderId))
                    .forEach(normalized::add);
        }
        if (normalized.isEmpty() && "USER".equalsIgnoreCase(senderRole)) {
            if (!Objects.equals(senderId, 2L)) {
                normalized.add(2L);
            }
            if (!Objects.equals(senderId, 3L)) {
                normalized.add(3L);
            }
        }
        return new ArrayList<>(normalized);
    }

    private List<Long> resolvePeers(Long currentUserId, Long peerId, List<Long> peerIds) {
        LinkedHashSet<Long> peers = new LinkedHashSet<>();
        if (peerIds != null) {
            peerIds.stream().filter(Objects::nonNull).forEach(peers::add);
        }
        if (peerId != null) {
            peers.add(peerId);
        }
        peers.remove(currentUserId);
        return new ArrayList<>(peers);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
