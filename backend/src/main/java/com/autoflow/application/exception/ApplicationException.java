package com.autoflow.application.exception;

public class ApplicationException extends RuntimeException {
    private final ErrorType type;

    private ApplicationException(ErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public static ApplicationException badRequest(String message) {
        return new ApplicationException(ErrorType.BAD_REQUEST, message);
    }

    public static ApplicationException notFound(String message) {
        return new ApplicationException(ErrorType.NOT_FOUND, message);
    }

    public static ApplicationException conflict(String message) {
        return new ApplicationException(ErrorType.CONFLICT, message);
    }

    public static ApplicationException forbidden(String message) {
        return new ApplicationException(ErrorType.FORBIDDEN, message);
    }

    public static ApplicationException forbidden() {
        return forbidden("Acesso negado.");
    }

    public static ApplicationException unauthorized(String message) {
        return new ApplicationException(ErrorType.UNAUTHORIZED, message);
    }

    public ErrorType type() {
        return type;
    }

    public enum ErrorType {
        BAD_REQUEST, NOT_FOUND, CONFLICT, FORBIDDEN, UNAUTHORIZED
    }
}
