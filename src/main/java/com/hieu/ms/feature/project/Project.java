package com.hieu.ms.feature.project;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hieu.ms.feature.attachment.Attachment;
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
@Table(
        indexes = {
            @Index(name = "idx_project_owner", columnList = "owner_id"),
            @Index(name = "idx_project_category", columnList = "category")
        })
public class Project extends BaseEntity {
    // ID field inherited

    String name;
    String description;
    String category;

    @ElementCollection
    List<String> tags = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    Chat chat;

    @ManyToOne
    User owner;

    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Issue> issues;

    @JsonIgnore
    @ManyToMany
    List<User> teams = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Attachment> attachments = new ArrayList<>();
}
