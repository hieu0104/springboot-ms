package com.hieu.ms.feature.role.dto;

import com.hieu.ms.shared.dto.request.BaseSearchRequest;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleSearchRequest extends BaseSearchRequest {
    // Add specific role filters here if needed
    // e.g. private String status;
}
