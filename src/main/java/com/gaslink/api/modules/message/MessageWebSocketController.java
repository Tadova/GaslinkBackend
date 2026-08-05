//package com.gaslink.api.modules.message;
//import com.gaslink.api.modules.message.dto.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.messaging.handler.annotation.*;
//import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
//import org.springframework.stereotype.Controller;
//import java.util.UUID;
//
//@Controller
////@RequiredArgsConstructor
//public class MessageWebSocketController {
//    private final MessageService messageService;
//
//    public MessageWebSocketController(MessageService messageService) {
//        this.messageService = messageService;
//    }
//
//    @MessageMapping("/chat.send")
//    public void send(@Payload SendMessageRequest req, SimpMessageHeaderAccessor headerAccessor) {
//        String senderId = (String) headerAccessor.getSessionAttributes().get("userId");
//        if (senderId != null) messageService.sendMessage(UUID.fromString(senderId), req);
//    }
//}