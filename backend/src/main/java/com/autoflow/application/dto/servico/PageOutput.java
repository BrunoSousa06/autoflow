package com.autoflow.application.dto.servico;

import java.util.List;

public record PageOutput<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
}
