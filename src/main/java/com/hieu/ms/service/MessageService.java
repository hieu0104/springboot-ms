package com.hieu.ms.service;

import com.hieu.ms.dto.request.MessageCreationRequest;
import com.hieu.ms.entity.Chat;
import com.hieu.ms.entity.Message;
import com.hieu.ms.entity.User;
import com.hieu.ms.repository.ChatRepository;
import com.hieu.ms.repository.MessageRepository;
import com.hieu.ms.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MessageService {

    ChatRepository chatRepository;

    ProjectService projectService;
    UserRepository userRepository;
    MessageRepository messageRepository;

    public Message sendMessage(MessageCreationRequest request) {
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(
                        () -> new EntityExistsException("user not found with id" + request.getSenderId())
                );
        Chat chat = projectService.getProjectById(request.getProjectId()).getChat();

        Message message = Message.builder()
                .content(request.getContent())
                .sender(sender)
                .createAt(LocalDateTime.now())
                .chat(chat)
                .build();

        return messageRepository.save(message);

    }

    public List<Message> getMessageByProjectId(String projectId) {
        Chat chat = projectService.getChatByProjectId(projectId);
        return messageRepository.findByChatIdOrderByCreateAtAsc(chat.getId());
    }

}
