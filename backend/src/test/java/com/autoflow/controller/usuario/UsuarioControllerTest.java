package com.autoflow.controller.usuario;

import com.autoflow.controller.usuario.request.LoginRequest;
import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.controller.usuario.response.UsuarioResponse;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private List<UsuarioResponse> usuarioResponses;


    @BeforeEach
    void setup() {
        UsuarioResponse usuarioResponse;
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UsuarioController(usuarioService))
                .build();

        registroRequest = new RegistroRequest("Bruno","usuario@email.com", "52998224725","32131221","Senha@123", RoleEnum.ADMIN);
        usuarioEntity = new UsuarioEntity();
        usuarioResponse = new UsuarioResponse(1L, "Bruno1", "usuario@email.com", RoleEnum.ADMIN);
        usuarioResponses = List.of(usuarioResponse);


        loginRequest = new LoginRequest("usuario@email.com", "senha123");
    }

    @Test
    void deveCadastrarUsuario() throws Exception {
        when(usuarioService.cadastrar(registroRequest)).thenReturn(usuarioEntity);

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

    @Test
    void deveListarUsuarios() throws Exception {
        when(usuarioService.listarUsuarios()).thenReturn(usuarioResponses);

        String jsonBody = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(get("/auth/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarMecanicos() throws Exception {
        when(usuarioService.buscarMecanicos()).thenReturn(usuarioResponses);

        mockMvc.perform(get("/auth/mecanicos")
                        .param("token", "tok"))
                .andExpect(status().isOk());


    }
}
