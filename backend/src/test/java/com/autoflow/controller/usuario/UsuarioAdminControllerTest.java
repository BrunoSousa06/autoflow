package com.autoflow.controller.usuario;

import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.controller.usuario.response.UsuarioResponse;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.service.usuario.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioAdminController usuarioAdminController;

    private UsuarioResponse usuarioResponseAdmin;
    private UsuarioResponse usuarioResponseAtendente;
    private RegistroRequest registroAtendente;
    private RegistroRequest registroCliente;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(usuarioAdminController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        usuarioResponseAdmin    = new UsuarioResponse(1L, "Admin",     "admin@autoflow.com",     RoleEnum.ADMIN);
        usuarioResponseAtendente = new UsuarioResponse(2L, "Atendente", "atendente@autoflow.com", RoleEnum.ATENDENTE);

        registroAtendente = new RegistroRequest("Atendente", "atendente@autoflow.com", "52998224725", "11999999999", "Senha@1234", RoleEnum.ATENDENTE);
        registroCliente   = new RegistroRequest("Cliente",   "cliente@autoflow.com",   "52998224725", "11999999999", "Senha@1234", RoleEnum.CLIENTE);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String email, String role) {
        var userDetails = User.withUsername(email).password("").roles(role).build();
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void deveListarUsuariosComoAdmin() throws Exception {
        when(usuarioService.listarUsuarios()).thenReturn(List.of(usuarioResponseAdmin, usuarioResponseAtendente));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("admin@autoflow.com"))
                .andExpect(jsonPath("$[1].email").value("atendente@autoflow.com"));

        verify(usuarioService).listarUsuarios();
    }

    @Test
    void deveListarUsuariosComoAtendente() throws Exception {
        when(usuarioService.listarUsuarios()).thenReturn(List.of(usuarioResponseAtendente));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(usuarioService).listarUsuarios();
    }

    @Test
    void deveCadastrarUsuarioComoAdmin() throws Exception {
        autenticarComo("admin@autoflow.com", "ADMIN");
        when(usuarioService.cadastrarComoStaff(any(RegistroRequest.class), eq(RoleEnum.ADMIN)))
                .thenReturn(usuarioResponseAtendente);

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroAtendente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("atendente@autoflow.com"))
                .andExpect(jsonPath("$.role").value("ATENDENTE"));

        verify(usuarioService).cadastrarComoStaff(any(RegistroRequest.class), eq(RoleEnum.ADMIN));
    }

    @Test
    void deveCadastrarClienteComoAtendente() throws Exception {
        autenticarComo("atendente@autoflow.com", "ATENDENTE");
        UsuarioResponse clienteResponse = new UsuarioResponse(3L, "Cliente", "cliente@autoflow.com", RoleEnum.CLIENTE);
        when(usuarioService.cadastrarComoStaff(any(RegistroRequest.class), eq(RoleEnum.ATENDENTE)))
                .thenReturn(clienteResponse);

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroCliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CLIENTE"));

        verify(usuarioService).cadastrarComoStaff(any(RegistroRequest.class), eq(RoleEnum.ATENDENTE));
    }

    @Test
    void deveRetornarCreatedComDadosDoUsuarioCriado() throws Exception {
        autenticarComo("admin@autoflow.com", "ADMIN");
        when(usuarioService.cadastrarComoStaff(any(RegistroRequest.class), eq(RoleEnum.ADMIN)))
                .thenReturn(usuarioResponseAdmin);

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registroAtendente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Admin"));
    }
}
