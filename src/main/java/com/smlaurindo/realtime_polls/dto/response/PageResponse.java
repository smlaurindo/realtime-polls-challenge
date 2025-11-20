package com.smlaurindo.realtime_polls.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int page,
    int pageSize,
    long pageLength,
    int totalPages,
    long totalElements,
    boolean hasNext,
    boolean hasPrevious
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getNumberOfElements(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}

