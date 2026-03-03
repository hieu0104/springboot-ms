package com.hieu.ms.feature.comment;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hieu.ms.feature.issue.Issue;
import com.hieu.ms.feature.issue.IssueService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentService {

    CommentRepository commentRepository;
    IssueService issueService;
    UserService userService;

    @Transactional
    public Comment createComment(String issuesId, String content, Authentication connectedUser) {
        // Lấy username từ JWT token
        String username = connectedUser.getName();
        User user = userService.findUserByUsername(username);

        Issue issue = issueService.getIssueById(issuesId);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setIssue(issue);
        return commentRepository.save(comment);
    }

    public void deleteComment(String commentId, Authentication connectedUser) {
        Comment comment = commentRepository
                .findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        // Lấy username từ JWT token
        String username = connectedUser.getName();
        User user = userService.findUserByUsername(username);

        if (!comment.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("you do not have permission to delete this comment");
        commentRepository.delete(comment);
    }

    public List<Comment> getCommentsByIssueId(String issueId) {
        return commentRepository.findByIssueId(issueId);
    }
}
