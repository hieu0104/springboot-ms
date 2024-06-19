package com.hieu.ms.dto.request;

import jakarta.persistence.ElementCollection;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IssuesRequest {
    String title;
    String description;
    String status;
    String projectId;
    String priority;
    LocalDateTime dueDate;
    List<String> tags;
    String assigneeId;
}
