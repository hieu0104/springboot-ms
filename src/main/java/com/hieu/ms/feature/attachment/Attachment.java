package com.hieu.ms.feature.attachment;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hieu.ms.feature.issue.Issue;
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
@Table(name = "attachment")
public class Attachment extends BaseEntity {

    @Column(nullable = false)
    String originalName; // Tên file gốc

    @Column(nullable = false)
    String storedName; // Tên file lưu trữ (UUID-based để tránh trùng)

    String contentType; // MIME type: image/png, application/pdf...

    Long fileSize; // Kích thước file (bytes)

    @Column(nullable = false)
    String filePath; // Đường dẫn lưu file

    @Column(length = 500)
    String description; // Mô tả file (optional)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    @JsonIgnore
    Issue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnore
    Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    User uploadedBy;
}
