package com.hieu.ms.feature.comment;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.hieu.ms.feature.issue.Issue;
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
