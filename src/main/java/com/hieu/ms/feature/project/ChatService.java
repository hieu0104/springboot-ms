package com.hieu.ms.feature.project;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatService {

    ChatRepository chatRepository;

    public Chat createChat(Chat chat) {
        //        Chat chat = chatMapper.toChat(request);
        //        chat = chatRepository.save(chat);
        // return chatMapper.toChatResponse(chat);
        return chatRepository.save(chat);
    }
}
