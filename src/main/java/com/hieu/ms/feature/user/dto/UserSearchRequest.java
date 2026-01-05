package com.hieu.ms.feature.user.dto;

import com.hieu.ms.shared.dto.request.BaseSearchRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserSearchRequest extends BaseSearchRequest {
    // Add specific user filters here if needed in the future
    // e.g. private String role;
    // e.g. private Boolean active;
}
