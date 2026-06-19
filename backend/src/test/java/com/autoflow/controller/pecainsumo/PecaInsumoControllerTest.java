package com.autoflow.controller.pecainsumo;

import com.autoflow.controller.pecainsumo.request.PecaInsumoRequest;
import com.autoflow.controller.pecainsumo.response.PecaInsumoResponse;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.service.pecainsumo.PecaInsumoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @InjectMocks
    private PecaInsumoController pecaInsumoController;

    @Mock
    private PecaInsumoService pecaInsumoService;

    private PecaInsumoRequest request;
    private PecaInsumoResponse response;
    private List<PecaInsumoResponse> responses;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(pecaInsumoController)
                .build();

        request = new PecaInsumoRequest("Óleo 5W30", BigDecimal.valueOf(49.90), 10, CategoriaPecaInsumo.INSUMO);
        response = new PecaInsumoResponse(1L,"Óleo 5W30", BigDecimal.valueOf(49.90), 10, CategoriaPecaInsumo.INSUMO);
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
        Page<PecaInsumoResponse> page = new PageImpl<>(responses, PageRequest.of(0, 10), responses.size());
        when(pecaInsumoService.listarPaginado(any(Pageable.class), any(), any())).thenReturn(page);

        mockMvc.perform(get("/peca-insumo"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarFiltrandoPorNomeETipo() throws Exception {
        Page<PecaInsumoResponse> page = new PageImpl<>(responses, PageRequest.of(0, 10), responses.size());
        when(pecaInsumoService.listarPaginado(any(Pageable.class), any(), any())).thenReturn(page);

        mockMvc.perform(get("/peca-insumo")
                        .param("nome", "Óleo")
                        .param("tipo", "INSUMO"))
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
