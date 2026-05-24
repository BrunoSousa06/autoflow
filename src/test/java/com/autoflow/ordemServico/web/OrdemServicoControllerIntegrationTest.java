package com.autoflow.ordemServico.web;

import com.autoflow.ordemServico.application.OrdemServicoService;
import com.autoflow.ordemServico.domain.OrdemServico;
import com.autoflow.ordemServico.domain.ServicoSolicitado;
import com.autoflow.ordemServico.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdemServicoController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrdemServicoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdemServicoService ordemServicoService;

    @Test
    void deveCriarOrdemServico() throws Exception {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        UUID servicoId = UUID.randomUUID();
        UUID ordemServicoId = UUID.randomUUID();
        List<ServicoSolicitado> servicos = List.of(new ServicoSolicitado(servicoId, "Revisao"));
        OrdemServico ordemServico = OrdemServico.restaurar(
                ordemServicoId,
                "OS-123",
                clienteId,
                veiculoId,
                StatusOrdemServico.RECEBIDA,
                LocalDateTime.of(2026, 5, 24, 10, 30),
                servicos
        );
        when(ordemServicoService.criar(clienteId, veiculoId, servicos)).thenReturn(ordemServico);

        mockMvc.perform(post("/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "%s",
                                  "veiculoId": "%s",
                                  "servicosSolicitados": [
                                    {
                                      "servicoId": "%s",
                                      "nome": "Revisao"
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
}
