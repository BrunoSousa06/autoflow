package com.autoflow.controller.veiculo;

import com.autoflow.controller.veiculo.request.VeiculoRequest;
import com.autoflow.controller.veiculo.response.VeiculoResponse;
import com.autoflow.service.veiculo.VeiculoService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VeiculoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private VeiculoService veiculoService;

    private VeiculoRequest request;
    private VeiculoResponse response;
    private List<VeiculoResponse> responses;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VeiculoController(veiculoService))
                .build();

        // Ajuste os parâmetros do construtor conforme os campos reais de VeiculoRequest/Response
        request = new VeiculoRequest("43243242432","Honda",2020,"ABC1234", "Civic");
        response = new VeiculoResponse(1L,"Honda",2020,"ABC1234", "Civic", null);
        responses = List.of(response);
    }

    @Test
    void deveCadastrarVeiculo() throws Exception {
        when(veiculoService.cadastrar(request)).thenReturn(response);

        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated());
    }

    @Test
    void deveListarVeiculoPorId() throws Exception {
        when(veiculoService.listar(1L)).thenReturn(response);

        mockMvc.perform(get("/veiculos/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarTodosVeiculos() throws Exception {
        when(veiculoService.listarTodosVeiculos()).thenReturn(responses);

        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarVeiculo() throws Exception {
        when(veiculoService.atualizar(any(VeiculoRequest.class), eq(1L))).thenReturn(response);

        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/veiculos/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void deveDeletarVeiculo() throws Exception {
        doNothing().when(veiculoService).deletar(1L);

        mockMvc.perform(delete("/veiculos/1"))
                .andExpect(status().isOk());

        verify(veiculoService).deletar(1L);
    }
}
