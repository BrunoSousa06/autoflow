package com.autoflow.application.policy;

import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.output.cliente.ClienteOutput;
import com.autoflow.domain.cliente.ClienteStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteAtivoPolicyTest {

    @Mock
    private ClienteGateway clienteGateway;

    @Test
    void deveManterPerfisNaoClienteInalterados() {
        var policy = new ClienteAtivoPolicy(clienteGateway);

        assertTrue(policy.podeAutenticar("admin@email.com", false));
        verifyNoInteractions(clienteGateway);
    }

    @Test
    void devePermitirClienteAtivo() {
        var policy = new ClienteAtivoPolicy(clienteGateway);
        when(clienteGateway.findByUsuarioEmail("cliente@email.com"))
                .thenReturn(Optional.of(ClienteOutput.builder().status(ClienteStatus.ATIVO).build()));

        assertTrue(policy.podeAutenticar("cliente@email.com", true));
    }

    @Test
    void deveBloquearClienteInativo() {
        var policy = new ClienteAtivoPolicy(clienteGateway);
        when(clienteGateway.findByUsuarioEmail("cliente@email.com"))
                .thenReturn(Optional.of(ClienteOutput.builder().status(ClienteStatus.INATIVO).build()));

        assertFalse(policy.podeAutenticar("cliente@email.com", true));
    }

    @Test
    void devePermitirUsuarioClienteSemVinculo() {
        var policy = new ClienteAtivoPolicy(clienteGateway);
        when(clienteGateway.findByUsuarioEmail("sem-vinculo@email.com"))
                .thenReturn(Optional.empty());

        assertTrue(policy.podeAutenticar("sem-vinculo@email.com", true));
    }
}
