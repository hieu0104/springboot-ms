package com.hieu.ms.feature.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityExistsException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hieu.ms.feature.message.dto.MessageCreationRequest;
import com.hieu.ms.feature.project.ChatRepository;
import com.hieu.ms.feature.project.Project;
import com.hieu.ms.feature.project.ProjectService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserService;
import com.hieu.ms.shared.exception.AppException;
import com.hieu.ms.shared.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class MessageServiceErrorPathTest {

    @Mock
    ChatRepository chatRepository;

    @Mock
    ProjectService projectService;

    @Mock
    UserService userService;

    @Mock
    MessageRepository messageRepository;

    @InjectMocks
    MessageService messageService;

    @Test
    @DisplayName("sendMessage: senderId not found -> propagates EntityExistsException")
    void sendMessage_SenderNotFound_ThrowsException() {
        // Arrange
        MessageCreationRequest request = MessageCreationRequest.builder()
                .senderId("bad-sender")
                .projectId("project-1")
                .content("hello")
                .build();

        when(userService.findUserById("bad-sender"))
                .thenThrow(new EntityExistsException("user not found with IDbad-sender"));

        // Act & Assert
        assertThatThrownBy(() -> messageService.sendMessage(request)).isInstanceOf(EntityExistsException.class);

        verify(projectService, never()).getProjectById(any());
        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendMessage: projectId not found -> propagates AppException(PROJECT_NOT_FOUND)")
    void sendMessage_ProjectNotFound_ThrowsAppException() {
        // Arrange
        MessageCreationRequest request = MessageCreationRequest.builder()
                .senderId("user-1")
                .projectId("bad-project")
                .content("hello")
                .build();

        User sender = User.builder().username("alice").build();
        sender.setId("user-1");

        when(userService.findUserById("user-1")).thenReturn(sender);
        when(projectService.getProjectById("bad-project")).thenThrow(new AppException(ErrorCode.PROJECT_NOT_FOUND));

        // Act & Assert
        assertThatThrownBy(() -> messageService.sendMessage(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendMessage: project exists but chat is null -> saved with null chat")
    void sendMessage_ChatIsNull_SavedWithNullChat() {
        // Arrange
        MessageCreationRequest request = MessageCreationRequest.builder()
                .senderId("user-1")
                .projectId("project-1")
                .content("hello")
                .build();

        User sender = User.builder().username("alice").build();
        sender.setId("user-1");

        Project project = new Project();
        project.setId("project-1");
        // chat is null by default

        when(userService.findUserById("user-1")).thenReturn(sender);
        when(projectService.getProjectById("project-1")).thenReturn(project);
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Message result = messageService.sendMessage(request);

        // Assert
        assertThat(result.getChat()).isNull();
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("getMessageByProjectId: projectId not found -> AppException(PROJECT_NOT_FOUND)")
    void getMessageByProjectId_ProjectNotFound_ThrowsAppException() {
        // Arrange
        when(projectService.getChatByProjectId("bad-project")).thenThrow(new AppException(ErrorCode.PROJECT_NOT_FOUND));

        // Act & Assert
        assertThatThrownBy(() -> messageService.getMessageByProjectId("bad-project"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(messageRepository, never()).findByChatIdOrderByCreatedAtAsc(any());
    }
}
