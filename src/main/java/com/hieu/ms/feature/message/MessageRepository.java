package com.hieu.ms.feature.message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByChatIdOrderByCreatedAtAsc(String chatId);
}
