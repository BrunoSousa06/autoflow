package com.autoflow.application.dto;

import java.util.List;

public record PageResult<T>(List<T> content, long totalElements, int page, int size) {
    public PageResult {
        content = List.copyOf(content);
    }
}
