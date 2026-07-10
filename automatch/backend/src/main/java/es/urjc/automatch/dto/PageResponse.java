package es.urjc.automatch.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Envoltura de respuesta paginada para no exponer Page de Spring directamente. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
