package com.example.SmartLearning.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartLearning.DTO.ChatRequest;
import com.example.SmartLearning.DTO.ChatResponse;
import com.example.SmartLearning.chatbot.ChatService;
 
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
 
    private final ChatService chatService;
 
    /**
     * POST /api/chat
     *
     * Body : { "apprenantId": 1, "message": "Que dois-je apprendre ensuite ?" }
     *
     * Réponse :
     * {
     *   "reply": "Bonjour Ahmed ! Voici mes recommandations...",
     *   "recommendations": [
     *     { "courseId": 3, "title": "...", "score": 87, "reason": "..." },
     *     ...
     *   ]
     * }
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request));
    }
}
