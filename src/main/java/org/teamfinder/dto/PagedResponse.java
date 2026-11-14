package org.teamfinder.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Stable JSON shape for a page of results (avoids serializing Spring's PageImpl directly). */
public record PagedResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
