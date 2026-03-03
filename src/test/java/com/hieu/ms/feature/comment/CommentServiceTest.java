package com.hieu.ms.feature.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import com.hieu.ms.feature.issue.Issue;
import com.hieu.ms.feature.issue.IssueService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserService;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

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

    private User user;
    private Issue issue;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = User.builder().username("john").build();
        user.setId("user-1");

        issue = new Issue();
        issue.setId("issue-1");

        comment = Comment.builder().content("Hello").user(user).issue(issue).build();
        comment.setId("comment-1");
    }

    @Nested
    @DisplayName("createComment")
    class CreateCommentTests {

        @Test
        @DisplayName("happy path: saves comment with correct user and issue")
        void happyPath_savesComment() {
            when(authentication.getName()).thenReturn("john");
            when(userService.findUserByUsername("john")).thenReturn(user);
            when(issueService.getIssueById("issue-1")).thenReturn(issue);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);

            Comment result = commentService.createComment("issue-1", "Hello", authentication);

            assertThat(result).isEqualTo(comment);
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteCommentTests {

        @Test
        @DisplayName("owner: deletes successfully")
        void owner_deletesSuccessfully() {
            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));
            when(authentication.getName()).thenReturn("john");
            when(userService.findUserByUsername("john")).thenReturn(user);

            commentService.deleteComment("comment-1", authentication);

            verify(commentRepository).delete(comment);
        }

        @Test
        @DisplayName("not owner: throws AccessDeniedException")
        void notOwner_throwsAccessDeniedException() {
            User other = User.builder().username("other").build();
            other.setId("user-2");

            when(commentRepository.findById("comment-1")).thenReturn(Optional.of(comment));
            when(authentication.getName()).thenReturn("other");
            when(userService.findUserByUsername("other")).thenReturn(other);

            assertThatThrownBy(() -> commentService.deleteComment("comment-1", authentication))
                    .isInstanceOf(AccessDeniedException.class);

            verify(commentRepository, never()).delete(any());
        }

        @Test
        @DisplayName("not found: throws EntityNotFoundException")
        void notFound_throwsEntityNotFoundException() {
            when(commentRepository.findById("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.deleteComment("missing", authentication))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCommentsByIssueId")
    class GetCommentsTests {

        @Test
        @DisplayName("delegates to repo and returns list")
        void delegatesToRepo() {
            when(commentRepository.findByIssueId("issue-1")).thenReturn(List.of(comment));

            List<Comment> result = commentService.getCommentsByIssueId("issue-1");

            assertThat(result).hasSize(1).containsExactly(comment);
        }
    }
}
