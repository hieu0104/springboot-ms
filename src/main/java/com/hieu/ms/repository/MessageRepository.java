package com.hieu.ms.repository;

import com.hieu.ms.entity.Message;
import com.hieu.ms.entity.Project;
import com.hieu.ms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,String> {
List<Message> findByChatIdOrderByCreateAtAsc(String chatId);
}
