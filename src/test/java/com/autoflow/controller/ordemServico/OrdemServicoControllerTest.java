package com.autoflow.controller.ordemServico;

import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemServico.StatusOrdemServico;
import com.autoflow.mapper.ServicoSolicitadoMapper;
import com.autoflow.service.ordemServico.OrdemServicoService;
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


import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrdemServicoControllerTest {

    private MockMvc mockMvc;
    @Mock
    private OrdemServicoService ordemServicoService;
    @Mock
    private ServicoSolicitadoMapper servicoSolicitadoMapper;
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new OrdemServicoController(ordemServicoService, servicoSolicitadoMapper))
                .build();
    }

    @Test
    void deveCriarOrdemServico() throws Exception {
        Long clienteId = 1L;
        Long veiculoId = 1L;
        Long servicoId = 1L;
        Long ordemServicoId = 1L;
        List<ServicoSolicitadoEntity> servicos = List.of(new ServicoSolicitadoEntity(servicoId, "Revisao", new BigDecimal("10.0")));
        OrdemServicoEntity ordemServicoEntity = mock(OrdemServicoEntity.class);
        when(ordemServicoEntity.getId()).thenReturn(ordemServicoId);
        when(ordemServicoEntity.getNumeroOs()).thenReturn("OS-123");
        when(ordemServicoEntity.getStatus()).thenReturn(StatusOrdemServico.RECEBIDA);
        when(ordemServicoEntity.getDataAbertura()).thenReturn(java.time.LocalDateTime.of(2026, 5, 24, 10, 30));
        when(servicoSolicitadoMapper.mapToEntities(anyList())).thenReturn(servicos);
        when(ordemServicoService.criar(clienteId, veiculoId, servicos)).thenReturn(ordemServicoEntity);

        mockMvc.perform(post("/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "%s",
                                  "veiculoId": "%s",
                                  "servicosSolicitados": [
                                    {
                                      "servicoId": "%s"
                                    }
                                  ]
                                }
                                """.formatted(clienteId, veiculoId, servicoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ordemServicoId.toString()))
                .andExpect(jsonPath("$.numeroOs").value("OS-123"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.dataAbertura").exists());

        verify(ordemServicoService).criar(eq(clienteId), eq(veiculoId), eq(servicos));
    }

    //TODO CRIAR TESTE DO NOVO ENDPOINT
}
