package com.hieu.ms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

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
