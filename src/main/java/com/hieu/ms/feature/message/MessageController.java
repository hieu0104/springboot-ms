package com.hieu.ms.feature.message;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.hieu.ms.feature.message.dto.MessageCreationRequest;
import com.hieu.ms.shared.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Message Management", description = "APIs quản lý tin nhắn")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    @Operation(summary = "Gửi tin nhắn", description = "Gửi một tin nhắn mới trong dự án")
    public ApiResponse<Message> sendMessage(@Valid @RequestBody MessageCreationRequest request) {
        return ApiResponse.<Message>builder()
                .result(messageService.sendMessage(request))
                .build();
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Lấy tin nhắn theo dự án", description = "Lấy tất cả tin nhắn trong một dự án")
    public ApiResponse<List<Message>> getMessagesByProjectId(@PathVariable String projectId) {
        return ApiResponse.<List<Message>>builder()
                .result(messageService.getMessageByProjectId(projectId))
                .build();
    }
}
