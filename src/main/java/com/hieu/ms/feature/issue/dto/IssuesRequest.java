package com.hieu.ms.feature.issue.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.hieu.ms.feature.issue.IssueStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IssuesRequest {
    String title;
    String description;
    IssueStatus status;
    String projectId;
    String priority;
    LocalDateTime dueDate;
    List<String> tags;
    String assigneeId;
}
