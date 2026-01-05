package com.hieu.ms.feature.attachment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttachmentRequest {
    String issueId;
    String projectId;
    String description;
}
