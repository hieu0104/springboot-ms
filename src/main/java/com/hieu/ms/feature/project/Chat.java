package com.hieu.ms.feature.project;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hieu.ms.feature.message.Message;
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
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String name;

    @JsonIgnore
    @OneToOne
    Project project;

    @JsonIgnore
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Message> messages;

    @JsonIgnore
    @ManyToMany
    List<User> users = new ArrayList<>();
}
