package com.hieu.ms.shared.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Generic pagination response wrapper
 * Best practice: Consistent pagination format across all APIs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    /** List of items in current page */
    List<T> content;

    /** Current page number (0-indexed) */
    int page;

    /** Number of items per page */
    int size;

    /** Total number of items across all pages */
    long totalElements;

    /** Total number of pages */
    int totalPages;

    /** Whether this is the first page */
    boolean first;

    /** Whether this is the last page */
    boolean last;

    /**
     * Convert Spring Data Page to PageResponse
     *
     * @param page Spring Data Page object
     * @param <T>  Type of content
     * @return PageResponse with same data
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
