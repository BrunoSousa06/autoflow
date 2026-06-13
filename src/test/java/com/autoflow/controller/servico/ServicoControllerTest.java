package com.autoflow.controller.servico;

import com.autoflow.controller.servico.request.ServicoRequest;
import com.autoflow.controller.servico.response.ServicoResponse;
import com.autoflow.controller.servico.response.TempoMedioServicoResponse;
import com.autoflow.service.servico.ServicoService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ServicoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ServicoService servicoService;

    @InjectMocks
    private ServicoController servicoController;

    private ServicoRequest request;
    private ServicoResponse response;
    private List<ServicoResponse> responses;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(servicoController)
                .build();

        request = new ServicoRequest("Troca de Óleo", "Substituição do óleo do motor", BigDecimal.valueOf(150.00));
        response = new ServicoResponse(1L, "Troca de Óleo", "Substituição do óleo do motor", BigDecimal.valueOf(150.00));
        responses = List.of(response);
    }

    @Test
    void deveCadastrarServico() throws Exception {
        when(servicoService.cadastrar(any(ServicoRequest.class))).thenReturn(response);

        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated());
    }

    @Test
    void deveListarServicoPorId() throws Exception {
        when(servicoService.buscarPorId(1L)).thenReturn(response);

        mockMvc.perform(get("/servicos/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deveListarTodosServicos() throws Exception {
        when(servicoService.listar()).thenReturn(responses);

        mockMvc.perform(get("/servicos"))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarServico() throws Exception {
        when(servicoService.atualizar(any(ServicoRequest.class), eq(1L))).thenReturn(response);

        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/servicos/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void deveDeletarServico() throws Exception {
        doNothing().when(servicoService).deletar(1L);

        mockMvc.perform(delete("/servicos/1"))
                .andExpect(status().isOk());

        verify(servicoService).deletar(1L);
    }

    @Test
    void deveListarTempoMedioPorServico() throws Exception {
        TempoMedioServicoResponse tempoMedio = new TempoMedioServicoResponse(
                1L,
                "Troca de Ã“leo",
                2L,
                3600.0
        );
        when(servicoService.listarTempoMedioPorServico()).thenReturn(List.of(tempoMedio));

        mockMvc.perform(get("/servicos/metricas/tempo-medio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].servicoId").value(1L))
                .andExpect(jsonPath("$[0].nomeServico").value("Troca de Ã“leo"))
                .andExpect(jsonPath("$[0].quantidadeExecucoes").value(2L))
                .andExpect(jsonPath("$[0].tempoMedioSegundos").value(3600.0));

        verify(servicoService).listarTempoMedioPorServico();
    }
}
