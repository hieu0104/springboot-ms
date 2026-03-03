package com.hieu.ms.feature.comment;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.shared.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comment Management", description = "APIs quản lý bình luận")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Tạo bình luận", description = "Tạo bình luận mới cho một issue")
    public ResponseEntity<ApiResponse<Comment>> createComment(
            @Valid @RequestBody CommentRequest request, Authentication connectedUser) {
        Comment comment = commentService.createComment(request.getIssueId(), request.getContent(), connectedUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Comment>builder().result(comment).build());
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Xóa bình luận", description = "Xóa một bình luận")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId, Authentication connectedUser) {
        commentService.deleteComment(commentId, connectedUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/issue/{issueId}")
    @Operation(summary = "Lấy bình luận theo issue", description = "Lấy tất cả bình luận của một issue")
    public ApiResponse<List<Comment>> getCommentsByIssueId(@PathVariable String issueId) {
        return ApiResponse.<List<Comment>>builder()
                .result(commentService.getCommentsByIssueId(issueId))
                .build();
    }
}
