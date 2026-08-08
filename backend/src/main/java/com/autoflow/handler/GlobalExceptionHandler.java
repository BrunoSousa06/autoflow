package com.autoflow.handler;

import com.autoflow.application.exception.AcompanhamentoPublicoNaoEncontradoException;
import com.autoflow.application.exception.OrdemServicoNaoEncontradaException;
import com.autoflow.application.exception.TokenAcompanhamentoObrigatorioException;
import com.autoflow.application.exception.ClienteDuplicadoException;
import com.autoflow.application.exception.ClienteAutenticadoNaoEncontradoException;
import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.exception.VeiculoDadosInvalidosException;
import com.autoflow.application.exception.VeiculoDuplicadoException;
import com.autoflow.application.exception.VeiculoNaoEncontradoException;
import com.autoflow.application.exception.EstoqueItemNaoEncontradoException;
import com.autoflow.application.exception.UsuarioNaoAutenticadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(
            HttpMessageNotReadableException ex) {

        Map<String, String> errors = new HashMap<>();

        Throwable cause = ex.getMostSpecificCause();

        if (cause.getMessage() != null && cause.getMessage().contains("Role inválida")) {

            errors.put("role", cause.getMessage());
        } else {
            errors.put("erro", "JSON inválido");
        }

        return ResponseEntity.badRequest().body(errors);
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(
            ResponseStatusException ex) {

        Map<String, String> error = new HashMap<>();

        error.put("erro", ex.getReason());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(error);
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleClienteNaoEncontrado(
            ClienteNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(ClienteDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleClienteDuplicado(
            ClienteDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(ClienteAutenticadoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleClienteAutenticadoNaoEncontrado(
            ClienteAutenticadoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNaoAutenticadoException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioNaoAutenticado(
            UsuarioNaoAutenticadoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(VeiculoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleVeiculoNaoEncontrado(
            VeiculoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(VeiculoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleVeiculoDuplicado(
            VeiculoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(VeiculoDadosInvalidosException.class)
    public ResponseEntity<Map<String, String>> handleVeiculoDadosInvalidos(
            VeiculoDadosInvalidosException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(EstoqueItemNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleEstoqueItemNaoEncontrado(
            EstoqueItemNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBusinessException(
            RuntimeException ex) {

        Map<String, String> error = new HashMap<>();

        error.put("erro", ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(AcompanhamentoPublicoNaoEncontradoException.class)
    public ResponseEntity<String> handleAcompanhamentoNaoEncontrado(
            AcompanhamentoPublicoNaoEncontradoException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(TokenAcompanhamentoObrigatorioException.class)
    public ResponseEntity<String> handleTokenObrigatorio(
            TokenAcompanhamentoObrigatorioException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(OrdemServicoNaoEncontradaException.class)
    public ResponseEntity<String> handleOrdemServicoNaoEncontrada(
            OrdemServicoNaoEncontradaException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

}
