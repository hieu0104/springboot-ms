package com.hieu.ms.feature.issue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hieu.ms.feature.attachment.Attachment;
import com.hieu.ms.feature.comment.Comment;
import com.hieu.ms.feature.project.Project;
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
public class Issue extends BaseEntity {
    // ID inherited

    String title;
    String description;

    @Enumerated(EnumType.STRING)
    IssueStatus status;

    // String projectID; // Removed redundant field
    String priority;

    @Column(name = "due_date")
    LocalDateTime dueDate;

    @ElementCollection
    List<String> tags;

    @ManyToOne
    User assignee;

    @ManyToOne
    Project project;

    @JsonIgnore
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Attachment> attachments = new ArrayList<>();
}
