package com.autoflow.service.usuario;

import com.autoflow.config.security.service.JwtService;
import com.autoflow.controller.usuario.request.LoginRequest;
import com.autoflow.controller.usuario.request.RegistroRequest;
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
        // Ajuste os construtores dos Records conforme a estrutura real do seu projeto
        registroClienteRequest = new RegistroRequest("cliente@email.com", "bruno@hotmail.com", "432523432","321321321", "teste21321", RoleEnum.CLIENTE);
        registroAdminRequest = new RegistroRequest("admin@email.com", "bruno@hotmail.com", "432523432","321321321", "teste21321", RoleEnum.ADMIN);
        loginRequest = new LoginRequest("usuario@email.com", "senha123");

        usuarioEntity = new UsuarioEntity();
        usuarioEntity.setEmail("usuario@email.com");
        usuarioEntity.setRole(RoleEnum.CLIENTE); // Exemplo de atribuição de ENUM

        clienteEntity = new ClienteEntity();
    }

    // ==========================================
    // TESTES DO MÉTODO: cadastrar
    // ==========================================

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
        // Garante que o usuarioRepository.save foi chamado duas vezes (linha 16 e linha 23 do seu Service)
        verify(usuarioRepository, times(1)).save(usuarioEntity);
    }

    @Test
    void deveCadastrarUsuarioComOutraRoleENaoSalvarNaTabelaCliente() {
        when(usuarioMapper.mapToEntity(registroAdminRequest)).thenReturn(usuarioEntity);
        // Nesse fluxo, o primeiro save e o retorno final utilizam a mesma chamada/mock
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
}
