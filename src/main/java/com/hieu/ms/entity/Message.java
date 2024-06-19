package com.hieu.ms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
