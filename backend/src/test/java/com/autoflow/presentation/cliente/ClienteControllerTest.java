package com.autoflow.presentation.cliente;

import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.usecases.cliente.*;
import com.autoflow.mapper.ClienteMapper;
import com.autoflow.presentation.cliente.ClienteController;
import com.autoflow.presentation.cliente.request.ClienteRequest;
import com.autoflow.presentation.cliente.response.ClienteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CriarClienteUseCase criarClienteUseCase;

    @Mock
    private BuscarClientePorEmailUseCase buscarClientePorEmailUseCase;

    @Mock
    private BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    @Mock
    private ListarTodosClientesUseCase listarTodosClientesUseCase;

    @Mock
    private AtualizarClienteUseCase atualizarClienteUseCase;

    @Mock
    private DeletarClienteUseCase deletarClienteUseCase;

    @Mock
    private ListarClienteUseCase listarClienteUseCase;

    @Spy
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteController clienteController;

    private ClienteRequest clienteRequest;
    private ClienteInput clienteInput;
    private ClienteOutput clienteOutput;
    private ClienteResponse response;
    private List<ClienteResponse> responses;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(clienteController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        clienteRequest = new ClienteRequest("João Silva", "52998224725", "12312321321", "bruno@hotmail.com");
        clienteInput = new ClienteInput("João Silva", "52998224725", "12312321321", "bruno@hotmail.com");

        clienteOutput = ClienteOutput.builder()
                .id(1L)
                .nome("Bruno")
                .cpfCnpj("52998224725")
                .telefone("12312321321")
                .email("bruno@hotmail.com")
                .veiculos(null)
                .build();

        response = new ClienteResponse(1L, "Bruno", "52998224725", "12312321321", "bruno@hotmail.com", null);
        responses = List.of(response);


    }

    @Test
    void deveBuscarMeuPerfil() throws Exception {
        when(buscarClientePorEmailUseCase.execute("bruno@hotmail.com")).thenReturn(clienteOutput);
        when(clienteMapper.maptoResponse(any(ClienteOutput.class))).thenReturn(response);

        var userDetails = User.withUsername("bruno@hotmail.com").password("").roles("CLIENTE").build();
        var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            mockMvc.perform(get("/clientes/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nome").value("Bruno"))
                    .andExpect(jsonPath("$.email").value("bruno@hotmail.com"));

            verify(buscarClientePorEmailUseCase).execute("bruno@hotmail.com");
            verify(clienteMapper).maptoResponse(clienteOutput); // Verify mapper call
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void deveCadastrarCliente() throws Exception {
        when(criarClienteUseCase.execute(any(ClienteInput.class)))
                .thenReturn(clienteOutput);
        when(clienteMapper.mapToInput(any(ClienteRequest.class))).thenReturn(clienteInput);
        when(clienteMapper.maptoResponse(any(ClienteOutput.class))).thenReturn(response);

        mockMvc.perform(post("/clientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Bruno"))
                .andExpect(jsonPath("$.cpfCnpj").value("52998224725"))
                .andExpect(jsonPath("$.telefone").value("12312321321"))
                .andExpect(jsonPath("$.email").value("bruno@hotmail.com"));

        ArgumentCaptor<ClienteInput> captor =
                ArgumentCaptor.forClass(ClienteInput.class);

        verify(clienteMapper).mapToInput(any(ClienteRequest.class)); // Verify mapper call
        verify(criarClienteUseCase).execute(captor.capture());
        verify(clienteMapper).maptoResponse(clienteOutput); // Verify mapper call

        ClienteInput capturedInput = captor.getValue();

        assertEquals("João Silva", capturedInput.nome());
        assertEquals("52998224725", capturedInput.cpfCnpj());
        assertEquals("12312321321", capturedInput.telefone());
        assertEquals("bruno@hotmail.com", capturedInput.email());
    }

    @Test
    void deveListarClientePorDocumentoOuId() throws Exception {
        when(listarClienteUseCase.execute(12345678901L)).thenReturn(clienteOutput);
        when(clienteMapper.maptoResponse(any(ClienteOutput.class))).thenReturn(response);

        mockMvc.perform(get("/clientes/12345678901"))
                .andExpect(status().isOk());

        verify(listarClienteUseCase).execute(12345678901L);
        verify(clienteMapper).maptoResponse(clienteOutput); // Verify mapper call
    }

    @Test
    void deveListarTodosClientes() throws Exception {
        List<ClienteOutput> outputs = List.of(clienteOutput);
        when(listarTodosClientesUseCase.execute()).thenReturn(outputs);
        when(clienteMapper.maptoResponse(any(ClienteOutput.class))).thenReturn(response);

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk());

        verify(listarTodosClientesUseCase).execute();
        verify(clienteMapper, times(outputs.size())).maptoResponse(any(ClienteOutput.class)); // Verify mapper call for stream
    }

    @Test
    void deveAtualizarCliente() throws Exception {
        when(atualizarClienteUseCase.execute(eq(1L), any(ClienteInput.class)))
                .thenReturn(clienteOutput);
        when(clienteMapper.mapToInput(any(ClienteRequest.class))).thenReturn(clienteInput);

        when(clienteMapper.maptoResponse(any(ClienteOutput.class))).thenReturn(response);

        String jsonBody = objectMapper.writeValueAsString(clienteRequest);

        mockMvc.perform(patch("/clientes/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        ArgumentCaptor<ClienteInput> captor = ArgumentCaptor.forClass(ClienteInput.class);
        verify(clienteMapper).mapToInput(any(ClienteRequest.class)); // Verify mapper call
        verify(atualizarClienteUseCase).execute(eq(1L), captor.capture());
        verify(clienteMapper).maptoResponse(clienteOutput); // Verify mapper call

        ClienteInput capturedInput = captor.getValue();
        assertEquals("João Silva", capturedInput.nome());
    }

    @Test
    void deveDeletarCliente() throws Exception {
        doNothing().when(deletarClienteUseCase).execute(1L);

        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isOk());

        verify(deletarClienteUseCase).execute(1L);
    }
}
