package com.autoflow.controller.pecaInsumo;

import com.autoflow.controller.pecaInsumo.request.PecaInsumoRequest;
import com.autoflow.controller.pecaInsumo.response.PecaInsumoResponse;
import com.autoflow.service.pecaInsumo.PecaInsumoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PecaInsumoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PecaInsumoService pecaInsumoService;

    private PecaInsumoRequest request;
    private PecaInsumoResponse response;
    private List<PecaInsumoResponse> responses;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PecaInsumoController(pecaInsumoService))
                .build();

        request = new PecaInsumoRequest("Óleo 5W30", BigDecimal.valueOf(49.90), 10);
        response = new PecaInsumoResponse(1L,"Óleo 5W30", BigDecimal.valueOf(49.90), 10);
        responses = List.of(response);
    }

    @Test
    void deveCadastrarPecaInsumo() throws Exception {
        when(pecaInsumoService.cadastrar(any(PecaInsumoRequest.class))).thenReturn(response);

        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/peca-insumo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated());
    }

    @Test
    void deveListarPecaInsumoPorId() throws Exception {
        when(pecaInsumoService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/peca-insumo/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarTodasPecasEInsumos() throws Exception {
        when(pecaInsumoService.listar()).thenReturn(responses);

        mockMvc.perform(get("/peca-insumo"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarPecaInsumo() throws Exception {
        when(pecaInsumoService.atualizar(any(PecaInsumoRequest.class), eq(1L))).thenReturn(response);

        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/peca-insumo/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void deveDeletarPecaInsumo() throws Exception {
        doNothing().when(pecaInsumoService).deletar(1L);

        mockMvc.perform(delete("/peca-insumo/1"))
                .andExpect(status().isOk());

        verify(pecaInsumoService).deletar(1L);
    }
}