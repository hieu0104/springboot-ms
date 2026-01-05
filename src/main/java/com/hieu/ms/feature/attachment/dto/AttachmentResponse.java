package com.hieu.ms.feature.attachment.dto;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttachmentResponse {
    String id;
    String originalName;
    String contentType;
    Long fileSize;
    String description;
    String downloadUrl;
    String issueId;
    String projectId;
    String uploadedById;
    String uploadedByName;
    LocalDateTime createdAt;
}
