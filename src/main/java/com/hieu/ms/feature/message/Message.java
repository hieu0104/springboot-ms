package com.hieu.ms.feature.message;

import jakarta.persistence.*;

import com.hieu.ms.feature.project.Chat;
import com.hieu.ms.feature.user.User;
import com.hieu.ms.shared.entity.BaseEntity;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Message extends BaseEntity {

    String content;

    @ManyToOne(fetch = FetchType.LAZY)
    Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    User sender;
}
