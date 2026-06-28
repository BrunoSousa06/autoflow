package com.autoflow.service.usuario;

import com.autoflow.config.security.service.JwtService;
import com.autoflow.controller.usuario.request.LoginRequest;
import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.controller.usuario.response.UsuarioResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.mapper.UsuarioMapper;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioMapper usuarioMapper;

    private RegistroRequest registroClienteRequest;
    private RegistroRequest registroAdminRequest;
    private LoginRequest loginRequest;
    private UsuarioEntity usuarioEntity;
    private ClienteEntity clienteEntity;

    @BeforeEach
    void setup() {
        registroClienteRequest = new RegistroRequest("cliente@email.com", "bruno@hotmail.com", "432523432","321321321", "teste21321", RoleEnum.CLIENTE);
        registroAdminRequest = new RegistroRequest("admin@email.com", "bruno@hotmail.com", "432523432","321321321", "teste21321", RoleEnum.ADMIN);
        loginRequest = new LoginRequest("usuario@email.com", "senha123");

        usuarioEntity = new UsuarioEntity();
        usuarioEntity.setEmail("usuario@email.com");
        usuarioEntity.setRole(RoleEnum.CLIENTE); // Exemplo de atribuição de ENUM

        clienteEntity = new ClienteEntity();
    }

    @Test
    void deveCadastrarUsuarioComRoleClienteESalvarNaTabelaCliente() {
        when(usuarioMapper.mapToEntity(registroClienteRequest)).thenReturn(usuarioEntity);
        // O método save é chamado duas vezes no seu código para o fluxo de cliente
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        when(usuarioMapper.mapToClienteEntity(registroClienteRequest)).thenReturn(clienteEntity);
        when(clienteRepository.save(clienteEntity)).thenReturn(clienteEntity);

        UsuarioEntity resultado = usuarioService.cadastrar(registroClienteRequest);

        assertNotNull(resultado);
        verify(usuarioMapper).mapToEntity(registroClienteRequest);
        verify(usuarioMapper).mapToClienteEntity(registroClienteRequest);
        verify(clienteRepository).save(clienteEntity);
        verify(usuarioRepository, times(1)).save(usuarioEntity);
    }

    @Test
    void deveCadastrarUsuarioComOutraRoleENaoSalvarNaTabelaCliente() {
        when(usuarioMapper.mapToEntity(registroAdminRequest)).thenReturn(usuarioEntity);
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);

        UsuarioEntity resultado = usuarioService.cadastrar(registroAdminRequest);

        assertNotNull(resultado);
        verify(usuarioMapper).mapToEntity(registroAdminRequest);
        verify(usuarioMapper, never()).mapToClienteEntity(any());
        verify(clienteRepository, never()).save(any());
        verify(usuarioRepository, times(1)).save(usuarioEntity);
    }

    @Test
    void deveFazerLoginEGerarTokenComSucesso() {
        Authentication authMock = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(usuarioEntity));
        when(jwtService.gerarToken(usuarioEntity.getEmail(), usuarioEntity.getRole().name())).thenReturn("token-jwt-valido");

        String tokenResult = usuarioService.login(loginRequest);

        assertNotNull(tokenResult);
        assertEquals("token-jwt-valido", tokenResult);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository).findByEmail(loginRequest.email());
        verify(jwtService).gerarToken(usuarioEntity.getEmail(), usuarioEntity.getRole().name());
    }

    @Test
    void deveLancarExcecaoNoLoginQuandoUsuarioNaoForEncontrado() {
        Authentication authMock = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(usuarioRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> usuarioService.login(loginRequest));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).gerarToken(any(), any());
    }

    @Test
    void deveBuscarMecanicoPorId() {
        Long mecanicoId = 1L;
        UsuarioEntity mecanico = new UsuarioEntity();
        mecanico.setId(mecanicoId);
        mecanico.setRole(RoleEnum.MECANICO);

        when(usuarioRepository.findById(mecanicoId)).thenReturn(Optional.of(mecanico));

        UsuarioEntity resultado = usuarioService.buscarMecanicoPorId(mecanicoId);

        assertEquals(mecanico, resultado);
        verify(usuarioRepository).findById(mecanicoId);
    }

    @Test
    void deveBuscarMecanicos() {
        Long mecanicoId = 1L;
        UsuarioEntity mecanico = new UsuarioEntity();
        mecanico.setId(mecanicoId);
        mecanico.setRole(RoleEnum.MECANICO);

        when(usuarioRepository.findByRole(RoleEnum.MECANICO)).thenReturn(List.of(mecanico));

        List<UsuarioResponse> usuarioResponses = usuarioService.buscarMecanicos();

        assertNotNull(usuarioResponses);
        verify(usuarioRepository).findByRole(RoleEnum.MECANICO);
    }

    @Test
    void deveLancarNotFoundQuandoMecanicoNaoForEncontrado() {
        Long mecanicoId = 1L;

        when(usuarioRepository.findById(mecanicoId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.buscarMecanicoPorId(mecanicoId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(usuarioRepository).findById(mecanicoId);
    }

    @Test
    void deveLancarBadRequestQuandoUsuarioNaoForMecanico() {
        Long mecanicoId = 1L;
        usuarioEntity.setRole(RoleEnum.CLIENTE);

        when(usuarioRepository.findById(mecanicoId)).thenReturn(Optional.of(usuarioEntity));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.buscarMecanicoPorId(mecanicoId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioRepository).findById(mecanicoId);
    }

    @Test
    void deveBuscarUsuarioPorEmail() {
        String email = "usuario@email.com";

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioEntity));

        UsuarioEntity resultado = usuarioService.buscarPorEmail(email);

        assertEquals(usuarioEntity, resultado);
        verify(usuarioRepository).findByEmail(email);
    }

    @Test
    void deveLancarNotFoundQuandoUsuarioAutenticadoNaoForEncontradoPorEmail() {
        String email = "usuario@email.com";

        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.buscarPorEmail(email)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(usuarioRepository).findByEmail(email);
    }

    @Test
    void deveCadastrarComoStaffComAdminCriandoQualquerRole() {
        when(usuarioMapper.mapToEntity(registroAdminRequest)).thenReturn(usuarioEntity);
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        UsuarioResponse response = new UsuarioResponse(1L, "Admin", "admin@email.com", RoleEnum.ADMIN);
        when(usuarioMapper.mapToResponse(usuarioEntity)).thenReturn(response);

        UsuarioResponse resultado = usuarioService.cadastrarComoStaff(registroAdminRequest, RoleEnum.ADMIN);

        assertNotNull(resultado);
        assertEquals(RoleEnum.ADMIN, resultado.role());
        verify(usuarioRepository).save(usuarioEntity);
    }

    @Test
    void deveCadastrarComoStaffComAtendenteCriandoAtendente() {
        RegistroRequest registroAtendente = new RegistroRequest("atendente@email.com", "bruno@hotmail.com", "432523432", "321321321", "teste21321", RoleEnum.ATENDENTE);
        when(usuarioMapper.mapToEntity(registroAtendente)).thenReturn(usuarioEntity);
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        UsuarioResponse response = new UsuarioResponse(1L, "Atendente", "atendente@email.com", RoleEnum.ATENDENTE);
        when(usuarioMapper.mapToResponse(usuarioEntity)).thenReturn(response);

        UsuarioResponse resultado = usuarioService.cadastrarComoStaff(registroAtendente, RoleEnum.ATENDENTE);

        assertNotNull(resultado);
        verify(usuarioRepository).save(usuarioEntity);
    }

    @Test
    void deveCadastrarComoStaffComAtendenteCriandoCliente() {
        when(usuarioMapper.mapToEntity(registroClienteRequest)).thenReturn(usuarioEntity);
        when(usuarioRepository.save(usuarioEntity)).thenReturn(usuarioEntity);
        when(usuarioMapper.mapToClienteEntity(registroClienteRequest)).thenReturn(clienteEntity);
        when(clienteRepository.save(clienteEntity)).thenReturn(clienteEntity);
        UsuarioResponse response = new UsuarioResponse(1L, "Cliente", "cliente@email.com", RoleEnum.CLIENTE);
        when(usuarioMapper.mapToResponse(usuarioEntity)).thenReturn(response);

        UsuarioResponse resultado = usuarioService.cadastrarComoStaff(registroClienteRequest, RoleEnum.ATENDENTE);

        assertNotNull(resultado);
        verify(usuarioRepository).save(usuarioEntity);
    }

    @Test
    void deveLancarForbiddenQuandoAtendenteCreateAdmin() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.cadastrarComoStaff(registroAdminRequest, RoleEnum.ATENDENTE)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarForbiddenQuandoAtendenteCreateMecanico() {
        RegistroRequest registroMecanico = new RegistroRequest("mecanico@email.com", "bruno@hotmail.com", "432523432", "321321321", "teste21321", RoleEnum.MECANICO);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.cadastrarComoStaff(registroMecanico, RoleEnum.ATENDENTE)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveListarUsuariosComSucesso() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setNome("João");
        usuario.setEmail("admin@email.com");
        usuario.setSenha("senhaCriptografada");
        usuario.setRole(RoleEnum.CLIENTE);
        List<UsuarioEntity> usuariosEntity = List.of(usuario);

        List<UsuarioResponse> usuariosResponse = List.of(
                new UsuarioResponse(1L, "João", "joao@hotmail.com",RoleEnum.CLIENTE
                )
        );

        when(usuarioRepository.findAll()).thenReturn(usuariosEntity);
        when(usuarioMapper.mapToResponse(usuariosEntity)).thenReturn(usuariosResponse);

        List<UsuarioResponse> resultado = usuarioService.listarUsuarios();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("João", resultado.getFirst().nome());

        verify(usuarioRepository).findAll();
        verify(usuarioMapper).mapToResponse(usuariosEntity);
    }
}
