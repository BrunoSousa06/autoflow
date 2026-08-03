package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.mapper.UsuarioMapper;
import com.autoflow.infrastructure.persistence.repository.UsuarioRepository;
import com.autoflow.infrastructure.security.service.JwtService;
import com.autoflow.presentation.usuario.request.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioUseCasesTest {

    @Mock UsuarioGateway gateway;
    @Mock UsuarioRepository repository;
    @Mock UsuarioMapper mapper;
    @Mock ClienteGateway clienteGateway;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;

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
        var useCase = new CadastrarUsuarioUseCase(repository, mapper, cadastrarCliente);
        when(mapper.mapToEntity(input)).thenReturn(usuario);
        when(repository.save(usuario)).thenReturn(usuario);
        when(mapper.mapToOutput(usuario)).thenReturn(output);
        assertEquals(output, useCase.execute(input));
        verifyNoInteractions(cadastrarCliente);

        var clienteInput = new RegistroInput("Cliente", "cliente@email.com", "123", "119", "senha", RoleEnum.CLIENTE);
        when(mapper.mapToEntity(clienteInput)).thenReturn(usuario);
        assertEquals(output, useCase.execute(clienteInput));
        verify(cadastrarCliente).execute(clienteInput, usuario);

        when(repository.existsByEmail(input.email())).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> useCase.execute(input)).getStatusCode());
    }

    @Test
    void deveAplicarPermissoesAoCadastrarStaff() {
        var cadastrar = mock(CadastrarUsuarioUseCase.class);
        var useCase = new CadastrarStaffUseCase(cadastrar);
        when(cadastrar.execute(input)).thenReturn(output);
        assertEquals(output, useCase.execute(input, RoleEnum.ADMIN));

        assertEquals(HttpStatus.FORBIDDEN,
                assertThrows(ResponseStatusException.class,
                        () -> useCase.execute(input, RoleEnum.ATENDENTE)).getStatusCode());
    }

    @Test
    void deveAutenticarEGerarToken() {
        var request = new LoginRequest(usuario.getEmail(), "senha");
        var useCase = new LoginUsuarioUseCase(authenticationManager, repository, jwtService);
        when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken(usuario.getEmail(), RoleEnum.MECANICO.name())).thenReturn("token");
        assertEquals("token", useCase.execute(request));
        verify(authenticationManager).authenticate(any());

        when(repository.findByEmail(usuario.getEmail())).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> useCase.execute(request));
    }
}
