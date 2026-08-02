package com.autoflow.handler;

import com.autoflow.application.exception.AcompanhamentoPublicoNaoEncontradoException;
import com.autoflow.application.exception.OrdemServicoNaoEncontradaException;
import com.autoflow.application.exception.TokenAcompanhamentoObrigatorioException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveRetornarBadRequestComErrosDeValidacao() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(
                        new FieldError("clienteRequest", "nome", "Nome é obrigatório"),
                        new FieldError("clienteRequest", "cpfCnpj", "CPF/CNPJ é obrigatório")
                ));

        ResponseEntity<Map<String, String>> response =
                handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertNotNull(response.getBody());
        assertEquals("Nome é obrigatório", response.getBody().get("nome"));
        assertEquals("CPF/CNPJ é obrigatório", response.getBody().get("cpfCnpj"));
    }

    @Test
    void deveRetornarErroDeRoleQuandoJsonPossuirRoleInvalida() {
        RuntimeException cause =
                new RuntimeException("Role inválida: ADMIN_TESTE");

        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Erro ao ler JSON", cause, mock(HttpInputMessage.class));

        ResponseEntity<Map<String, String>> response =
                handler.handleNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Role inválida: ADMIN_TESTE", response.getBody().get("role"));
    }

    @Test
    void deveRetornarJsonInvalidoQuandoErroNaoForRoleInvalida() {
        RuntimeException cause =
                new RuntimeException("Erro ao converter campo");

        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Erro ao ler JSON", cause, mock(HttpInputMessage.class));

        ResponseEntity<Map<String, String>> response =
                handler.handleNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("JSON inválido", response.getBody().get("erro"));
    }

    @Test
    void deveRetornarStatusEReasonDaResponseStatusException() {
        ResponseStatusException exception =
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente não encontrado"
                );

        ResponseEntity<Map<String, String>> response =
                handler.handleResponseStatusException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cliente não encontrado", response.getBody().get("erro"));
    }

    @Test
    void deveRetornarBadRequestQuandoRegraDeEstadoForViolada() {
        IllegalStateException exception =
                new IllegalStateException("Servico deve estar em execucao para finalizar.");

        ResponseEntity<Map<String, String>> response =
                handler.handleBusinessException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Servico deve estar em execucao para finalizar.", response.getBody().get("erro"));
    }

    @Test
    void deveRetornarBadRequestQuandoArgumentoForInvalido() {
        IllegalArgumentException exception =
                new IllegalArgumentException("Servico e obrigatorio.");

        ResponseEntity<Map<String, String>> response =
                handler.handleBusinessException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Servico e obrigatorio.", response.getBody().get("erro"));
    }

    @Test
    void deveRetornarNotFoundQuandoAcompanhamentoNaoExistir() {
        var exception = new AcompanhamentoPublicoNaoEncontradoException();

        var response = handler.handleAcompanhamentoNaoEncontrado(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(exception.getMessage(), response.getBody());
    }

    @Test
    void deveRetornarBadRequestQuandoTokenNaoForInformado() {
        var exception = new TokenAcompanhamentoObrigatorioException();

        var response = handler.handleTokenObrigatorio(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(exception.getMessage(), response.getBody());
    }

    @Test
    void deveRetornarNotFoundQuandoOrdemServicoNaoExistir() {
        var exception = new OrdemServicoNaoEncontradaException(123L);

        var response = handler.handleOrdemServicoNaoEncontrada(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(exception.getMessage(), response.getBody());
    }
}
