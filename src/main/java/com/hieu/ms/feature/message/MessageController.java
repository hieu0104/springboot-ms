package com.hieu.ms.feature.message;

import java.util.List;

import jakarta.persistence.EntityExistsException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.feature.message.dto.MessageCreationRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Message Management", description = "APIs quản lý tin nhắn")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    @Operation(summary = "Gửi tin nhắn", description = "Gửi một tin nhắn mới trong dự án")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageCreationRequest request) {
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
    @Operation(summary = "Lấy tin nhắn theo dự án", description = "Lấy tất cả tin nhắn trong một dự án")
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
