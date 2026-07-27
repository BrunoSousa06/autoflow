package com.autoflow.application.security;

import com.autoflow.domain.ordemservico.ClienteOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private ClienteAutenticadoService clienteAutenticadoService;

    @InjectMocks
    private AuthorizationService authorizationService;

    private ClienteEntity cliente1;
    private ClienteEntity cliente2;
    private VeiculoEntity veiculoCliente1;
    private OrdemServicoEntity osCliente1;

    @BeforeEach
    void setUp() {

        cliente1 = new ClienteEntity();
        cliente1.setId(1L);
        cliente1.setNome("Bruno");
        cliente1.setCpfCnpj("12345678901");
        cliente1.setTelefone("11999999999");
        cliente1.setEmail("bruno@email.com");

        cliente2 = new ClienteEntity();
        cliente2.setId(2L);
        cliente2.setNome("João");
        cliente2.setCpfCnpj("98765432100");
        cliente2.setTelefone("11888888888");
        cliente2.setEmail("joao@email.com");

        veiculoCliente1 = new VeiculoEntity();
        veiculoCliente1.setId(10L);
        veiculoCliente1.setCliente(cliente1);


        osCliente1 = new OrdemServicoEntity();
        osCliente1.setId(20L);
        osCliente1.setVeiculo(veiculoCliente1);
    }

    @Test
    void devePermitirAcessoVeiculoParaNaoCliente() {
        when(usuarioAutenticadoService.isCliente()).thenReturn(false);

        assertDoesNotThrow(() -> authorizationService.validarPermissao(veiculoCliente1));

        verify(usuarioAutenticadoService).isCliente();
        verifyNoInteractions(clienteAutenticadoService);
    }

    @Test
    void devePermitirAcessoVeiculoParaClienteProprietario() {
        when(usuarioAutenticadoService.isCliente()).thenReturn(true);
        when(clienteAutenticadoService.getClienteId()).thenReturn(cliente1.getId());

        assertDoesNotThrow(() -> authorizationService.validarPermissao(veiculoCliente1));

        verify(usuarioAutenticadoService).isCliente();
        verify(clienteAutenticadoService).getClienteId();
    }

    @Test
    void deveNegarAcessoVeiculoParaClienteNaoProprietario() {
        when(usuarioAutenticadoService.isCliente()).thenReturn(true);
        when(clienteAutenticadoService.getClienteId()).thenReturn(cliente2.getId());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authorizationService.validarPermissao(veiculoCliente1)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(
                "Você não tem permissão para acessar este veículo.",
                exception.getReason()
        );

        verify(usuarioAutenticadoService).isCliente();
        verify(clienteAutenticadoService).getClienteId();
    }

    @Test
    void devePermitirAcessoOrdemServicoParaNaoCliente() {
        when(usuarioAutenticadoService.isCliente()).thenReturn(false);

        assertDoesNotThrow(() -> authorizationService.validarPermissao(osCliente1));

        verify(usuarioAutenticadoService).isCliente();
        verifyNoInteractions(clienteAutenticadoService);
    }

}