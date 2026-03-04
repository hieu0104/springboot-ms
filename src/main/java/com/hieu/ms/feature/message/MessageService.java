package com.hieu.ms.feature.message;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hieu.ms.feature.message.dto.MessageCreationRequest;
import com.hieu.ms.feature.project.Chat;
import com.hieu.ms.feature.project.ProjectService;
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
public class MessageService {

    ProjectService projectService;
    UserService userService;
    MessageRepository messageRepository;

    public Message sendMessage(MessageCreationRequest request) {
        User sender = userService.findUserById(request.getSenderId());
        Chat chat = projectService.getProjectById(request.getProjectId()).getChat();

        Message message = Message.builder()
                .content(request.getContent())
                .sender(sender)
                .chat(chat)
                .build();

        return messageRepository.save(message);
    }

    public List<Message> getMessageByProjectId(String projectId) {
        Chat chat = projectService.getChatByProjectId(projectId);
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chat.getId());
    }
}
