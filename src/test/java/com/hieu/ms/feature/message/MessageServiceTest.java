package com.hieu.ms.feature.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hieu.ms.feature.message.dto.MessageCreationRequest;
import com.hieu.ms.feature.project.Chat;
import com.hieu.ms.feature.project.Project;
import com.hieu.ms.feature.project.ProjectService;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.feature.user.UserService;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    ProjectService projectService;

    @Mock
    UserService userService;

    @Mock
    MessageRepository messageRepository;

    @InjectMocks
    MessageService messageService;

    private User sender;
    private Chat chat;
    private Project project;

    @BeforeEach
    void setUp() {
        sender = User.builder().username("alice").build();
        sender.setId("user-1");

        chat = new Chat();
        chat.setId("chat-1");

        project = new Project();
        project.setId("project-1");
        project.setChat(chat);
    }

    @Test
    @DisplayName("sendMessage: finds user + chat, builds message, saves and returns")
    void sendMessage_savesAndReturns() {
        MessageCreationRequest request = MessageCreationRequest.builder()
                .senderId("user-1")
                .projectId("project-1")
                .content("Hello team!")
                .build();

        Message saved = Message.builder()
                .content("Hello team!")
                .sender(sender)
                .chat(chat)
                .build();
        saved.setId("msg-1");

        when(userService.findUserById("user-1")).thenReturn(sender);
        when(projectService.getProjectById("project-1")).thenReturn(project);
        when(messageRepository.save(any(Message.class))).thenReturn(saved);

        Message result = messageService.sendMessage(request);

        assertThat(result).isEqualTo(saved);
        assertThat(result.getId()).isEqualTo("msg-1");
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("getMessageByProjectId: gets chat then returns ordered messages")
    void getMessageByProjectId_returnsMessages() {
        Message m1 = Message.builder().content("First").build();
        Message m2 = Message.builder().content("Second").build();

        when(projectService.getChatByProjectId("project-1")).thenReturn(chat);
        when(messageRepository.findByChatIdOrderByCreatedAtAsc("chat-1")).thenReturn(List.of(m1, m2));

        List<Message> result = messageService.getMessageByProjectId("project-1");

        assertThat(result).hasSize(2).containsExactly(m1, m2);
        verify(projectService).getChatByProjectId("project-1");
        verify(messageRepository).findByChatIdOrderByCreatedAtAsc("chat-1");
    }
}
