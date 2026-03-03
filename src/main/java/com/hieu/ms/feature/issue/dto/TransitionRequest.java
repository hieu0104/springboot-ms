package com.hieu.ms.feature.issue.dto;

import com.hieu.ms.feature.issue.IssueStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransitionRequest {
    IssueStatus targetStatus;
}
