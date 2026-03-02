package com.hieu.ms.feature.message.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageCreationRequest {
    String senderId;
    String content;
    String projectId;
}
