package com.hieu.ms.feature.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.hieu.ms.feature.issue.Issue;
import com.hieu.ms.feature.issue.IssueService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserService;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class CommentServiceErrorPathTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    IssueService issueService;

    @Mock
    UserService userService;

    @Mock
    Authentication authentication;

    @InjectMocks
    CommentService commentService;

    @Test
    @DisplayName("createComment: username not found -> AppException(USER_NOT_EXISTED)")
    void createComment_UsernameNotFound_ThrowsAppException() {
        // Arrange
        when(authentication.getName()).thenReturn("ghost");
        when(userService.findUserByUsername("ghost")).thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

        // Act & Assert
        assertThatThrownBy(() -> commentService.createComment("issue-1", "Hi", authentication))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_EXISTED);

        verify(issueService, never()).getIssueById(any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createComment: issueId not found -> AppException(ISSUE_NOT_FOUND)")
    void createComment_IssueNotFound_ThrowsAppException() {
        // Arrange
        User user = User.builder().username("alice").build();
        user.setId("user-1");

        when(authentication.getName()).thenReturn("alice");
        when(userService.findUserByUsername("alice")).thenReturn(user);
        when(issueService.getIssueById("bad-issue")).thenThrow(new AppException(ErrorCode.ISSUE_NOT_FOUND));

        // Act & Assert
        assertThatThrownBy(() -> commentService.createComment("bad-issue", "Hi", authentication))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ISSUE_NOT_FOUND);

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("createComment: happy path with explicit order check")
    void createComment_HappyPath_VerifiesOrder() {
        // Arrange
        User user = User.builder().username("alice").build();
        user.setId("user-1");
        Issue issue = new Issue();
        issue.setId("issue-1");
        Comment saved =
                Comment.builder().content("hello").user(user).issue(issue).build();

        when(authentication.getName()).thenReturn("alice");
        when(userService.findUserByUsername("alice")).thenReturn(user);
        when(issueService.getIssueById("issue-1")).thenReturn(issue);
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        // Act
        Comment result = commentService.createComment("issue-1", "hello", authentication);

        // Assert
        assertThat(result.getContent()).isEqualTo("hello");
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getIssue()).isEqualTo(issue);

        InOrder inOrder = inOrder(userService, issueService, commentRepository);
        inOrder.verify(userService).findUserByUsername("alice");
        inOrder.verify(issueService).getIssueById("issue-1");
        inOrder.verify(commentRepository).save(any(Comment.class));
    }
}
