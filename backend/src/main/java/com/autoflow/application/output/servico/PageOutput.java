package com.autoflow.application.output.servico;

import java.util.List;

public record PageOutput<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
}
