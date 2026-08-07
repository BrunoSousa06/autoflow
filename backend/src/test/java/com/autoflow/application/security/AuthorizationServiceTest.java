package com.autoflow.application.security;

import com.autoflow.application.dto.veiculo.VeiculoOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private ClienteAutenticadoService clienteAutenticadoService;
    @InjectMocks
    private AuthorizationService service;

    private final VeiculoOutput veiculo =
            new VeiculoOutput(10L, "ABC1234", "Honda", "Civic", 2020, 1L);

    @Test
    void devePermitirAcessoParaStaff() {
        when(clienteAutenticadoService.getClienteId()).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.validarPermissao(veiculo));
    }

    @Test
    void devePermitirAcessoAoProprietario() {
        when(clienteAutenticadoService.getClienteId()).thenReturn(Optional.of(1L));

        assertDoesNotThrow(() -> service.validarPermissao(veiculo));
        verify(clienteAutenticadoService).getClienteId();
    }

    @Test
    void deveNegarAcessoAClienteDiferente() {
        when(clienteAutenticadoService.getClienteId()).thenReturn(Optional.of(2L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validarPermissao(veiculo));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Você não tem permissão para acessar este veículo.", exception.getReason());
    }
}
