package com.autoflow.controller.orcamento;

import com.autoflow.config.security.service.CustomUserDetailsService;
import com.autoflow.config.security.service.JwtService;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.service.orcamento.PublicOrcamentoService;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicOrcamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicOrcamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicOrcamentoService publicOrcamentoService;

    @MockitoBean
    private ReparoAdicionalService reparoAdicionalService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void deveConsultarOrcamentoPublico() throws Exception {
        OrcamentoEntity orc = baseOrcamento();

        when(publicOrcamentoService.consultar(10L, "tok"))
                .thenReturn(orc);

        mockMvc.perform(get("/public/orcamentos/{id}", 10L)
                        .param("token", "tok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.ordemServicoId").value(1L))
                .andExpect(jsonPath("$.tipo").value("PRINCIPAL"))
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.totalGeral").value(150.00));

        verify(publicOrcamentoService).consultar(10L, "tok");
    }

    @Test
    void deveAprovarOrcamentoPublico() throws Exception {
        OrcamentoEntity orc = baseOrcamento();
        orc.setStatus(StatusOrcamento.APROVADO);

        when(publicOrcamentoService.aprovar(10L, "tok", "Maria"))
                .thenReturn(orc);

        mockMvc.perform(post("/public/orcamentos/{id}/aprovar", 10L)
                        .param("token", "tok")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        verify(publicOrcamentoService).aprovar(10L, "tok", "Maria");
        verify(reparoAdicionalService, never()).aprovarPorOrcamentoId(10L);
    }

    @Test
    void deveAprovarOrcamentoAdicionalEAplicarReparoNaOs() throws Exception {
        OrcamentoEntity orc = baseOrcamento();
        orc.setTipo(TipoOrcamento.ADICIONAL);
        orc.setStatus(StatusOrcamento.APROVADO);

        when(publicOrcamentoService.aprovar(10L, "tok", "Maria"))
                .thenReturn(orc);

        mockMvc.perform(post("/public/orcamentos/{id}/aprovar", 10L)
                        .param("token", "tok")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("ADICIONAL"))
                .andExpect(jsonPath("$.status").value("APROVADO"));

        verify(publicOrcamentoService).aprovar(10L, "tok", "Maria");
        verify(reparoAdicionalService).aprovarPorOrcamentoId(10L);
    }

    @Test
    void deveRecusarOrcamentoPublicoComMotivo() throws Exception {
        OrcamentoEntity orc = baseOrcamento();
        orc.setStatus(StatusOrcamento.REPROVADO);

        when(publicOrcamentoService.recusar(10L, "tok", "Nao quero"))
                .thenReturn(orc);

        mockMvc.perform(post("/public/orcamentos/{id}/recusar", 10L)
                        .param("token", "tok")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo":"Nao quero"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROVADO"));

        verify(publicOrcamentoService).recusar(10L, "tok", "Nao quero");
    }

    @Test
    void deveRecusarOrcamentoPublicoSemBody() throws Exception {
        OrcamentoEntity orc = baseOrcamento();
        orc.setStatus(StatusOrcamento.REPROVADO);

        when(publicOrcamentoService.recusar(10L, "tok", null))
                .thenReturn(orc);

        mockMvc.perform(post("/public/orcamentos/{id}/recusar", 10L)
                        .param("token", "tok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROVADO"));

        verify(publicOrcamentoService).recusar(10L, "tok", null);
    }

    private OrcamentoEntity baseOrcamento() {
        return OrcamentoEntity.builder()
                .id(10L)
                .ordemServicoId(1L)
                .tipo(TipoOrcamento.PRINCIPAL)
                .versao(1)
                .status(StatusOrcamento.DISPONIVEL)
                .criadoEm(LocalDateTime.of(2026, 5, 31, 10, 0))
                .disponibilizadoEm(LocalDateTime.of(2026, 5, 31, 10, 1))
                .totalServicos(new BigDecimal("100.00"))
                .totalItens(new BigDecimal("50.00"))
                .totalGeral(new BigDecimal("150.00"))
                .build();
    }
}