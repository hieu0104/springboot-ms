package com.hieu.ms.shared.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.Data;

@Data
public abstract class BaseSearchRequest {
    @Min(0)
    protected Integer page = 0; // Default 0-indexed for Spring

    @Min(1)
    @Max(100) // Prevent abuse - max 100 items per page
    protected Integer size = 10;

    protected String sort = "createdDate,desc";
    protected String keyword;

    public Pageable getPageable(Sort defaultSort) {
        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size < 1) ? 10 : size;

        Sort sorting = defaultSort;

        if (sort != null && !sort.isBlank()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length > 0) {
                String sortField = sortParams[0];
                Sort.Direction direction = (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]))
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                sorting = Sort.by(direction, sortField);
            }
        }

        return PageRequest.of(p, s, sorting);
    }
}
