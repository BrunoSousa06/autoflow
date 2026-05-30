package com.autoflow.controller.cliente;

import com.autoflow.controller.cliente.request.ClienteRequest;
import com.autoflow.controller.cliente.response.ClienteResponse;
import com.autoflow.service.cliente.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    private ClienteRequest request;
    private ClienteResponse response;
    private List<ClienteResponse> responses;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(clienteController)
                .build();

        request = new ClienteRequest("João Silva", "12345678901", "12312321321", "bruno@hotmail.com");
        response = new ClienteResponse(1L, "João Silva", "12345678901", "12312321321", "bruno@hotmail.com", null);
        responses = List.of(response);
    }

    @Test
    void deveListarClientePorDocumentoOuId() throws Exception {
        when(clienteService.listar(12345678901L)).thenReturn(response);

        mockMvc.perform(get("/clientes/12345678901"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarTodosClientes() throws Exception {
        when(clienteService.listarTodosClientes()).thenReturn(responses);

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarCliente() throws Exception {
        when(clienteService.atualizar(any(ClienteRequest.class), eq(1L))).thenReturn(response);

        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/clientes/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void deveDeletarCliente() throws Exception {
        doNothing().when(clienteService).deletar(1L);

        mockMvc.perform(delete("/clientes/1"))
                .andExpect(status().isOk());

        verify(clienteService).deletar(1L);
    }
}
