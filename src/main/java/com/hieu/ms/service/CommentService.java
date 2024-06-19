package com.hieu.ms.service;

import com.hieu.ms.entity.*;
import com.hieu.ms.mapper.ProjectMapper;
import com.hieu.ms.repository.CommentRepository;
import com.hieu.ms.repository.ProjectRepository;
import com.hieu.ms.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentService {

    CommentRepository commentRepository;
    IssueService issueService;

    public Comment createComment(String issuesId, String content, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();

//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Issue issue = issueService.getIssueById(issuesId);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreatedDateTime(LocalDateTime.now());
        comment.setUser(user);
        comment.setIssue(issue);
        comment = commentRepository.save(comment);
        issue.getComments().add(comment);
        return comment;
    }

    public void deleteComment(String commentId, Authentication connectedUser) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new EntityNotFoundException("Comment not found"));
       User user = (User) connectedUser.getPrincipal();
       // Optional<User> user = userRepository.findById(userId);
       if (!comment.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("you do not have permission to delete this comment");
        commentRepository.delete(comment);
    }

    public List<Comment> getCommentsByIssueId(String issueId) {
       Issue issue = issueService.getIssueById(issueId);
        return commentRepository.findByIssueId(issue.getId());
    }
}
