package com.hieu.ms.feature.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    ChatRepository chatRepository;

    @InjectMocks
    ChatService chatService;

    @Test
    @DisplayName("createChat: delegates to repo.save and returns saved chat")
    void createChat_delegatesToRepo() {
        Chat chat = new Chat();
        chat.setName("General");

        Chat saved = new Chat();
        saved.setId("chat-1");
        saved.setName("General");

        when(chatRepository.save(chat)).thenReturn(saved);

        Chat result = chatService.createChat(chat);

        assertThat(result).isEqualTo(saved);
        assertThat(result.getId()).isEqualTo("chat-1");
        verify(chatRepository).save(chat);
    }
}
