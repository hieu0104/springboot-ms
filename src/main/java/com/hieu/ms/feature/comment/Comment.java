package com.hieu.ms.feature.comment;

import jakarta.persistence.*;

import com.hieu.ms.feature.issue.Issue;
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
public class Comment extends BaseEntity {

    String content;

    @ManyToOne(fetch = FetchType.LAZY)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    Issue issue;
}
