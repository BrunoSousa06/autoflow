package com.autoflow.application.output.veiculo;

import java.util.List;

public record PageOutput<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
}
