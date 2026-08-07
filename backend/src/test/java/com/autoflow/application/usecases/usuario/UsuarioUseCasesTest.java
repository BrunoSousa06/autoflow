package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.LoginInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.dto.security.CurrentUser;
import com.autoflow.application.gateway.AuthenticationGateway;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.gateway.CurrentUserGateway;
import com.autoflow.application.gateway.PasswordGateway;
import com.autoflow.application.gateway.TokenGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.mapper.UsuarioMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioUseCasesTest {

    @Mock UsuarioGateway gateway;
    @Mock UsuarioMapper mapper;
    @Mock PasswordGateway passwordGateway;
    @Mock ClienteGateway clienteGateway;
    @Mock AuthenticationGateway authenticationGateway;
    @Mock TokenGateway tokenGateway;

    private UsuarioEntity usuario;
    private RegistroInput input;
    private UsuarioOutput output;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setNome("Maria");
        usuario.setEmail("maria@autoflow.com");
        usuario.setRole(RoleEnum.MECANICO);
        input = new RegistroInput("Maria", usuario.getEmail(), "12345678901", "11999999999", "senha", RoleEnum.MECANICO);
        output = new UsuarioOutput(1L, "Maria", usuario.getEmail(), RoleEnum.MECANICO);
    }

    @Test
    void deveBuscarMecanicoEValidarIdERole() {
        var useCase = new BuscarMecanicoPorIdUseCase(gateway);
        when(gateway.findById(1L)).thenReturn(Optional.of(usuario));
        assertSame(usuario, useCase.execute(1L));

        when(gateway.findById(2L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> useCase.execute(2L)).getStatusCode());

        usuario.setRole(RoleEnum.CLIENTE);
        when(gateway.findById(3L)).thenReturn(Optional.of(usuario));
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> useCase.execute(3L)).getStatusCode());
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        var useCase = new BuscarUsuarioPorEmailUseCase(gateway);
        when(gateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        assertSame(usuario, useCase.execute(usuario.getEmail()));
        when(gateway.findByEmail("ausente@email.com")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class,
                        () -> useCase.execute("ausente@email.com")).getStatusCode());
    }

    @Test
    void deveListarUsuariosEMecanicos() {
        when(gateway.findAll()).thenReturn(List.of(usuario));
        when(gateway.findByRole(RoleEnum.MECANICO)).thenReturn(List.of(usuario));
        when(mapper.mapToOutput(List.of(usuario))).thenReturn(List.of(output));
        assertEquals(List.of(output), new ListarUsuariosUseCase(gateway, mapper).execute());
        assertEquals(List.of(output), new BuscarMecanicosUseCase(gateway, mapper).execute());
    }

    @Test
    void deveCadastrarClienteEImpedirDocumentoDuplicado() {
        var cliente = new ClienteEntity();
        var useCase = new CadastrarClienteUseCase(clienteGateway, mapper);
        when(mapper.mapToClienteEntity(input)).thenReturn(cliente);
        when(clienteGateway.save(cliente)).thenReturn(cliente);
        assertSame(cliente, useCase.execute(input, usuario));
        assertSame(usuario, cliente.getUsuario());

        when(clienteGateway.existsByCpfCnpj(input.cpfCnpj())).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> useCase.execute(input, usuario)).getStatusCode());
    }

    @Test
    void deveCadastrarUsuarioEClienteQuandoAplicavel() {
        var cadastrarCliente = mock(CadastrarClienteUseCase.class);
        var useCase = new CadastrarUsuarioUseCase(gateway, passwordGateway, cadastrarCliente);
        when(passwordGateway.encode(input.senha())).thenReturn("senha-hash");
        when(gateway.save(any(UsuarioEntity.class))).thenAnswer(invocation -> {
            UsuarioEntity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        assertEquals(output, useCase.execute(input));
        verifyNoInteractions(cadastrarCliente);

        var clienteInput = new RegistroInput("Cliente", "cliente@email.com", "123", "119", "senha", RoleEnum.CLIENTE);
        UsuarioOutput clienteOutput = new UsuarioOutput(
                1L,
                "Cliente",
                "cliente@email.com",
                RoleEnum.CLIENTE
        );
        assertEquals(clienteOutput, useCase.execute(clienteInput));
        verify(cadastrarCliente).execute(eq(clienteInput), any(UsuarioEntity.class));

        when(gateway.existsByEmail(input.email())).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> useCase.execute(input)).getStatusCode());
    }

    @Test
    void deveAplicarPermissoesAoCadastrarStaff() {
        var cadastrar = mock(CadastrarUsuarioUseCase.class);
        var currentUserGateway = mock(CurrentUserGateway.class);
        var useCase = new CadastrarStaffUseCase(cadastrar, currentUserGateway);
        when(currentUserGateway.getCurrentUser()).thenReturn(java.util.Optional.of(
                new CurrentUser("admin@email.com", RoleEnum.ADMIN)));
        when(cadastrar.execute(input)).thenReturn(output);
        assertEquals(output, useCase.execute(input));

        when(currentUserGateway.getCurrentUser()).thenReturn(java.util.Optional.of(
                new CurrentUser("atendente@email.com", RoleEnum.ATENDENTE)));
        assertEquals(HttpStatus.FORBIDDEN,
                assertThrows(ResponseStatusException.class,
                        () -> useCase.execute(input)).getStatusCode());
    }

    @Test
    void devePermitirCadastroPublicoSomenteParaCliente() {
        var cadastrar = mock(CadastrarUsuarioUseCase.class);
        var useCase = new CadastrarUsuarioPublicoUseCase(cadastrar);
        var clienteInput = new RegistroInput(
                "Cliente",
                "cliente@email.com",
                "123",
                "119",
                "senha",
                RoleEnum.CLIENTE
        );
        when(cadastrar.execute(clienteInput)).thenReturn(output);

        assertEquals(output, useCase.execute(clienteInput));

        assertEquals(HttpStatus.FORBIDDEN,
                assertThrows(ResponseStatusException.class,
                        () -> useCase.execute(input)).getStatusCode());
    }

    @Test
    void deveAutenticarEGerarToken() {
        var input = new LoginInput(usuario.getEmail(), "senha");
        var useCase = new LoginUsuarioUseCase(authenticationGateway, gateway, tokenGateway);
        when(gateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(tokenGateway.generateToken(usuario.getEmail(), RoleEnum.MECANICO.name())).thenReturn("token");
        assertEquals("token", useCase.execute(input).token());
        verify(authenticationGateway).authenticate(usuario.getEmail(), "senha");

        when(gateway.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> useCase.execute(input));
    }
}
