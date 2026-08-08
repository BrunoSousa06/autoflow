package com.autoflow.application.security;

import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> service.validarPermissao(veiculo));

        assertEquals(ApplicationException.ErrorType.FORBIDDEN, exception.type());
        assertEquals("Você não tem permissão para acessar este veículo.", exception.getMessage());
    }
}
