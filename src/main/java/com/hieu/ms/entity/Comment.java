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
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String content;
    LocalDateTime createdDateTime;
    @ManyToOne
    User user;

    @ManyToOne
    Issue issue;

}
