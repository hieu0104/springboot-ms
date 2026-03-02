package com.hieu.ms.feature.message;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.hieu.ms.feature.project.Chat;
import com.hieu.ms.feature.user.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String content;
    LocalDateTime createAt;

    @ManyToOne
    Chat chat;

    @ManyToOne
    User sender;
}
