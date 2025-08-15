package com.adithya.trackfolio.controller;

import com.adithya.trackfolio.dto.ChatRequestDTO;
import com.adithya.trackfolio.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<String> chat(@RequestBody ChatRequestDTO request) {
        String aiResponse = chatService.handleChat(request);
        return ResponseEntity.ok(aiResponse);
    }
}