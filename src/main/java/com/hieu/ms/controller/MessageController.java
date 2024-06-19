package com.hieu.ms.controller;

import com.hieu.ms.dto.request.MessageCreationRequest;
import com.hieu.ms.entity.Message;
import com.hieu.ms.service.MessageService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(
            @RequestBody MessageCreationRequest request
            ) {
        try {
            Message message = messageService.sendMessage(request);
            return ResponseEntity.ok(message);
        } catch (EntityExistsException e) {
            log.error("Error sending message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Message>> getMessagesByProjectId(@PathVariable String projectId) {
        try {
            List<Message> messages = messageService.getMessageByProjectId(projectId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error fetching messages for project {}: {}", projectId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
