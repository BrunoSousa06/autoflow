package com.autoflow.presentation.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.usecases.usuario.CadastrarStaffUseCase;
import com.autoflow.application.usecases.usuario.ListarUsuariosUseCase;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.infrastructure.persistence.mapper.UsuarioMapper;
import com.autoflow.infrastructure.persistence.mapper.UsuarioMapperImpl;
import com.autoflow.presentation.usuario.UsuarioAdminController;
import com.autoflow.presentation.usuario.request.RegistroRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioAdminControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ListarUsuariosUseCase listarUsuariosUseCase;

    @Mock
    private CadastrarStaffUseCase cadastrarStaffUseCase;

    private UsuarioMapper usuarioMapper;

    private UsuarioAdminController usuarioAdminController;

    private UsuarioOutput usuarioOutputAdmin;
    private UsuarioOutput usuarioOutputAtendente;

    private RegistroRequest registroAtendente;
    private RegistroRequest registroCliente;

    @BeforeEach
    void setup() {

        usuarioMapper = new UsuarioMapperImpl();

        usuarioAdminController = new UsuarioAdminController(
                listarUsuariosUseCase,
                cadastrarStaffUseCase,
                usuarioMapper
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(usuarioAdminController)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver())
                .build();

        usuarioOutputAdmin = UsuarioOutput.builder()
                .id(1L)
                .nome("Admin")
                .email("admin@autoflow.com")
                .role(RoleEnum.ADMIN)
                .build();

        usuarioOutputAtendente = UsuarioOutput.builder()
                .id(2L)
                .nome("Atendente")
                .email("atendente@autoflow.com")
                .role(RoleEnum.ATENDENTE)
                .build();

        registroAtendente = new RegistroRequest(
                "Atendente",
                "atendente@autoflow.com",
                "52998224725",
                "11999999999",
                "Senha@1234",
                RoleEnum.ATENDENTE
        );

        registroCliente = new RegistroRequest(
                "Cliente",
                "cliente@autoflow.com",
                "52998224725",
                "11999999999",
                "Senha@1234",
                RoleEnum.CLIENTE
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String email, String role) {
        var userDetails = User.withUsername(email)
                .password("")
                .roles(role)
                .build();

        var auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void deveListarUsuariosComoAdmin() throws Exception {

        when(listarUsuariosUseCase.execute())
                .thenReturn(List.of(
                        usuarioOutputAdmin,
                        usuarioOutputAtendente));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email")
                        .value("admin@autoflow.com"))
                .andExpect(jsonPath("$[1].email")
                        .value("atendente@autoflow.com"));

        verify(listarUsuariosUseCase).execute();
    }

    @Test
    void deveListarUsuariosComoAtendente() throws Exception {

        when(listarUsuariosUseCase.execute())
                .thenReturn(List.of(usuarioOutputAtendente));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email")
                        .value("atendente@autoflow.com"));

        verify(listarUsuariosUseCase).execute();
    }

    @Test
    void deveCadastrarUsuarioComoAdmin() throws Exception {

        autenticarComo("admin@autoflow.com", "ADMIN");

        when(cadastrarStaffUseCase.execute(
                any(RegistroInput.class),
                eq(RoleEnum.ADMIN)))
                .thenReturn(usuarioOutputAtendente);

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroAtendente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("atendente@autoflow.com"))
                .andExpect(jsonPath("$.role")
                        .value("ATENDENTE"));

        verify(cadastrarStaffUseCase)
                .execute(any(RegistroInput.class), eq(RoleEnum.ADMIN));
    }

    @Test
    void deveCadastrarClienteComoAtendente() throws Exception {

        autenticarComo("atendente@autoflow.com", "ATENDENTE");

        UsuarioOutput clienteOutput = UsuarioOutput.builder()
                .id(3L)
                .nome("Cliente")
                .email("cliente@autoflow.com")
                .role(RoleEnum.CLIENTE)
                .build();

        when(cadastrarStaffUseCase.execute(
                any(RegistroInput.class),
                eq(RoleEnum.ATENDENTE)))
                .thenReturn(clienteOutput);

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroCliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role")
                        .value("CLIENTE"));

        verify(cadastrarStaffUseCase)
                .execute(any(RegistroInput.class),
                        eq(RoleEnum.ATENDENTE));
    }

    @Test
    void deveRetornarCreatedComDadosDoUsuarioCriado() throws Exception {

        autenticarComo("admin@autoflow.com", "ADMIN");

        when(cadastrarStaffUseCase.execute(
                any(RegistroInput.class),
                eq(RoleEnum.ADMIN)))
                .thenReturn(usuarioOutputAdmin);

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroAtendente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Admin"))
                .andExpect(jsonPath("$.email")
                        .value("admin@autoflow.com"))
                .andExpect(jsonPath("$.role")
                        .value("ADMIN"));
    }
}
