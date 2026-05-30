package com.autoflow.controller.usuario;

import com.autoflow.controller.usuario.request.LoginRequest;
import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.service.usuario.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsuarioService usuarioService;

    private RegistroRequest registroRequest;
    private UsuarioEntity usuarioEntity;
    private LoginRequest loginRequest;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UsuarioController(usuarioService))
                .build();

        registroRequest = new RegistroRequest("Bruno","usuario@email.com", "321321123","32131221","senha123", RoleEnum.ROLE_ADMIN);
        usuarioEntity = new UsuarioEntity();

        loginRequest = new LoginRequest("usuario@email.com", "senha123");
    }

    @Test
    void deveCadastrarUsuario() throws Exception {
        when(usuarioService.cadastrar(any(RegistroRequest.class))).thenReturn(usuarioEntity);

        String jsonBody = objectMapper.writeValueAsString(registroRequest);

        mockMvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated());
    }

    @Test
    void deveFazerLogin() throws Exception {
        when(usuarioService.login(any(LoginRequest.class))).thenReturn("token-jwt-mockado");

        String jsonBody = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
    }
}
