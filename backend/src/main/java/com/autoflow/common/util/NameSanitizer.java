package com.autoflow.common.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public final class NameSanitizer {

    private static final Pattern SAFE_NAME_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\p{M}\\s.,;:/()&+'-]+$");

    private NameSanitizer() {
    }

    public static String sanitizeName(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("O nome não pode estar em branco");
        }

        String normalized = value.strip().replaceAll("\\s+", " ");

        if (!SAFE_NAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Nome inválido");
        }

        return normalized;
    }

    public static String sanitizeForDisplay(String value) {
        try {
            return sanitizeName(value);
        } catch (IllegalArgumentException ex) {
            return "Nome inválido";
        }
    }
}
