package com.hieu.ms.feature.project.dto;

import java.util.List;

import com.hieu.ms.feature.user.dto.UserResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectResponse {
    String id;
    String name;
    String description;
    String category;
    List<String> tags;
    UserResponse owner;
    List<UserResponse> teams;
}
