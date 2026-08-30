package com.autoflow.application.output;

import java.util.List;

public record PageResult<T>(List<T> content, long totalElements, int page, int size) {
    public PageResult {
        content = List.copyOf(content);
    }
}
