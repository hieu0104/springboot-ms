package com.hieu.ms.feature.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {

    @NotNull(message = "Issue ID must not be null")
    private String issueId;

    @NotBlank(message = "Content must not be blank")
    private String content;
}
