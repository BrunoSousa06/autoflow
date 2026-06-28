package com.autoflow.controller.veiculo;

import com.autoflow.controller.veiculo.request.VeiculoRequest;
import com.autoflow.controller.veiculo.request.VeiculoUpdateRequest;
import com.autoflow.controller.veiculo.response.VeiculoResponse;
import com.autoflow.service.veiculo.VeiculoService;
import com.autoflow.service.veiculo.dto.VeiculoFiltro;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    private VeiculoRequest cadastroRequest;
    private VeiculoUpdateRequest updateRequest;
    private VeiculoResponse response;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VeiculoController(veiculoService))
                .build();

        cadastroRequest = new VeiculoRequest("11222333000181", "Honda", 2020, "ABC1234", "Civic");
        updateRequest = new VeiculoUpdateRequest("Honda", 2020, "ABC1234", "Civic");
        response = new VeiculoResponse(1L, "Honda", 2020, "ABC1234", "Civic", null);
    }

    @Test
    void deveCadastrarVeiculo() throws Exception {
        when(veiculoService.cadastrar(cadastroRequest)).thenReturn(response);

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastroRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveListarVeiculoPorId() throws Exception {
        when(veiculoService.listar(1L)).thenReturn(response);

        mockMvc.perform(get("/veiculos/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarVeiculosSemFiltros() throws Exception {
        when(veiculoService.listarComFiltros(any(VeiculoFiltro.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarVeiculosComFiltrosDePlacaEMarca() throws Exception {
        when(veiculoService.listarComFiltros(any(VeiculoFiltro.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/veiculos").param("placa", "ABC1234").param("marca", "Honda"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarVeiculo() throws Exception {
        when(veiculoService.atualizar(any(VeiculoUpdateRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(patch("/veiculos/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(veiculoService).atualizar(any(VeiculoUpdateRequest.class), eq(1L));
    }

    @Test
    void deveRetornar400QuandoAtualizarSemPlaca() throws Exception {
        var requestSemPlaca = new VeiculoUpdateRequest("Honda", 2020, "", "Civic");

        mockMvc.perform(patch("/veiculos/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSemPlaca)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(veiculoService);
    }

    @Test
    void deveDeletarVeiculo() throws Exception {
        doNothing().when(veiculoService).deletar(1L);

        mockMvc.perform(delete("/veiculos/1"))
                .andExpect(status().isOk());

        verify(veiculoService).deletar(1L);
    }
}