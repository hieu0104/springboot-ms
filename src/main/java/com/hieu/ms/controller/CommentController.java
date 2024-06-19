package com.hieu.ms.controller;

import com.hieu.ms.entity.Comment;
import com.hieu.ms.service.CommentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestParam String issuesId,
                                                 @RequestParam String content,
                                                Authentication connectedUser) {
        Comment comment = commentService.createComment(issuesId, content, connectedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId,
                                              Authentication connectedUser) {
        try {
            commentService.deleteComment(commentId, connectedUser);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException | AccessDeniedException e) {
            log.error("Error deleting comment with ID: {}", commentId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/issue/{issueId}")
    public ResponseEntity<List<Comment>> getCommentsByIssueId(@PathVariable String issueId) {
        List<Comment> comments = commentService.getCommentsByIssueId(issueId);
        return ResponseEntity.ok(comments);
    }
}
