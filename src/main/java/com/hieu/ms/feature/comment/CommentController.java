package com.hieu.ms.feature.comment;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Comment> createComment(
            @RequestParam String issuesId, @RequestParam String content, Authentication connectedUser) {
        Comment comment = commentService.createComment(issuesId, content, connectedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Xóa bình luận", description = "Xóa một bình luận")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId, Authentication connectedUser) {
        try {
            commentService.deleteComment(commentId, connectedUser);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException | AccessDeniedException e) {
            log.error("Error deleting comment with ID: {}", commentId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/issue/{issueId}")
    @Operation(summary = "Lấy bình luận theo issue", description = "Lấy tất cả bình luận của một issue")
    public ResponseEntity<List<Comment>> getCommentsByIssueId(@PathVariable String issueId) {
        List<Comment> comments = commentService.getCommentsByIssueId(issueId);
        return ResponseEntity.ok(comments);
    }
}
