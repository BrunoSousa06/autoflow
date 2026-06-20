package com.autoflow.controller.orcamento;

import com.autoflow.config.security.service.CustomUserDetailsService;
import com.autoflow.config.security.service.JwtService;
import com.autoflow.domain.orcamento.ClienteOrcamentoSnapshot;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.OrcamentoItemNecessarioEntity;
import com.autoflow.domain.orcamento.OrcamentoServicoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.orcamento.VeiculoOrcamentoSnapshot;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.service.orcamento.OrcamentoPdfService;
import com.autoflow.service.orcamento.OrcamentoService;
import com.autoflow.service.orcamento.dto.OrcamentoFiltro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrcamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        OrcamentoControllerTest.MethodSecurityTestConfig.class,
        OrcamentoControllerTest.SecurityExceptionHandler.class
})
class OrcamentoControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {}

    @RestControllerAdvice
    static class SecurityExceptionHandler {
        @ExceptionHandler(AuthorizationDeniedException.class)
        public ResponseEntity<Void> handle(AuthorizationDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrcamentoService orcamentoService;

    @MockitoBean
    private OrcamentoPdfService orcamentoPdfService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "admin@autoflow.com", roles = "ADMIN")
    void deveConsultarOrcamentoAutenticado() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        when(orcamentoService.consultarAutenticado(10L, "admin@autoflow.com")).thenReturn(orcamento);

        mockMvc.perform(get("/orcamentos/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.numeroOs").value("OS-123"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.servicos[0].servicoId").value(20L))
                .andExpect(jsonPath("$.servicos[0].nome").value("Revisao"))
                .andExpect(jsonPath("$.servicos[0].valor").value(100.00))
                .andExpect(jsonPath("$.itens[0].pecaInsumoId").value(30L))
                .andExpect(jsonPath("$.itens[0].servicoOsId").value(40L))
                .andExpect(jsonPath("$.itens[0].nome").value("Filtro"))
                .andExpect(jsonPath("$.itens[0].tipo").value("PECA"))
                .andExpect(jsonPath("$.itens[0].valorUnitario").value(25.00))
                .andExpect(jsonPath("$.itens[0].quantidade").value(2))
                .andExpect(jsonPath("$.itens[0].valorTotal").value(50.00));

        verify(orcamentoService).consultarAutenticado(10L, "admin@autoflow.com");
    }

    @Test
    @WithMockUser(username = "cliente@exemplo.com", roles = "CLIENTE")
    void deveAprovarOrcamentoAutenticadoSemTokenPublico() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        orcamento.setStatus(StatusOrcamento.APROVADO);
        when(orcamentoService.aprovar(10L, "cliente@exemplo.com")).thenReturn(orcamento);

        mockMvc.perform(post("/orcamentos/{id}/aprovar", 10L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        verify(orcamentoService).aprovar(10L, "cliente@exemplo.com");
    }

    @Test
    @WithMockUser(username = "cliente@exemplo.com", roles = "CLIENTE")
    void deveRecusarOrcamentoAutenticadoSemTokenPublico() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        orcamento.setStatus(StatusOrcamento.REPROVADO);
        when(orcamentoService.recusar(10L, "Nao quero", "cliente@exemplo.com")).thenReturn(orcamento);

        mockMvc.perform(post("/orcamentos/{id}/recusar", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo":"Nao quero"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROVADO"));

        verify(orcamentoService).recusar(10L, "Nao quero", "cliente@exemplo.com");
    }

    @Test
    @WithMockUser(username = "cliente@exemplo.com", roles = "CLIENTE")
    void deveRecusarOrcamentoAutenticadoSemBody() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        orcamento.setStatus(StatusOrcamento.REPROVADO);
        when(orcamentoService.recusar(10L, null, "cliente@exemplo.com")).thenReturn(orcamento);

        mockMvc.perform(post("/orcamentos/{id}/recusar", 10L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROVADO"));

        verify(orcamentoService).recusar(10L, null, "cliente@exemplo.com");
    }

    @Test
    @WithMockUser(username = "atendente@autoflow.com", roles = "ATENDENTE")
    void deveListarOrcamentosComFiltrosAutenticado() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        OrcamentoFiltro filtroEsperado = new OrcamentoFiltro(
                StatusOrcamento.DISPONIVEL,
                "OS-123",
                "ABC1234",
                "cliente@exemplo.com",
                "12345678901",
                TipoOrcamento.PRINCIPAL
        );

        when(orcamentoService.consultarOrcamentos("atendente@autoflow.com", filtroEsperado))
                .thenReturn(List.of(orcamento));

        mockMvc.perform(get("/orcamentos")
                        .param("statusOrcamento", "DISPONIVEL")
                        .param("numeroOs", "OS-123")
                        .param("placa", "ABC1234")
                        .param("clienteEmail", "cliente@exemplo.com")
                        .param("clienteDocumento", "12345678901")
                        .param("tipo", "PRINCIPAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].status").value("DISPONIVEL"));

        verify(orcamentoService).consultarOrcamentos(
                "atendente@autoflow.com",
                filtroEsperado
        );
    }

    @Test
    @WithMockUser(username = "admin@autoflow.com", roles = "ADMIN")
    void deveBaixarPdfComoAdmin() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(orcamentoService.consultarAutenticado(10L, "admin@autoflow.com")).thenReturn(orcamento);
        when(orcamentoPdfService.gerarPdf(orcamento)).thenReturn(pdfBytes);

        mockMvc.perform(get("/orcamentos/{id}/pdf", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"orcamento-10.pdf\""));

        verify(orcamentoService).consultarAutenticado(10L, "admin@autoflow.com");
        verify(orcamentoPdfService).gerarPdf(orcamento);
    }

    @Test
    @WithMockUser(username = "cliente@exemplo.com", roles = "CLIENTE")
    void deveBaixarPdfComoCliente() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(orcamentoService.consultarAutenticado(10L, "cliente@exemplo.com")).thenReturn(orcamento);
        when(orcamentoPdfService.gerarPdf(orcamento)).thenReturn(pdfBytes);

        mockMvc.perform(get("/orcamentos/{id}/pdf", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        verify(orcamentoService).consultarAutenticado(10L, "cliente@exemplo.com");
    }

    @Test
    @WithMockUser(username = "atendente@autoflow.com", roles = "ATENDENTE")
    void deveBaixarPdfComoAtendente() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(orcamentoService.consultarAutenticado(10L, "atendente@autoflow.com")).thenReturn(orcamento);
        when(orcamentoPdfService.gerarPdf(orcamento)).thenReturn(pdfBytes);

        mockMvc.perform(get("/orcamentos/{id}/pdf", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        verify(orcamentoService).consultarAutenticado(10L, "atendente@autoflow.com");
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void deveBloquearDownloadPdfParaMecanico() throws Exception {
        mockMvc.perform(get("/orcamentos/{id}/pdf", 10L))
                .andExpect(status().isForbidden());

        verify(orcamentoService, never()).consultarAutenticado(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(orcamentoPdfService, never()).gerarPdf(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @WithMockUser(username = "admin@autoflow.com", roles = "ADMIN")
    void deveRetornar404AoBaixarPdfDeOrcamentoInexistente() throws Exception {
        when(orcamentoService.consultarAutenticado(99L, "admin@autoflow.com"))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Orçamento não encontrado"));

        mockMvc.perform(get("/orcamentos/{id}/pdf", 99L))
                .andExpect(status().isNotFound());
    }

    private OrcamentoEntity baseOrcamento() {
        return OrcamentoEntity.builder()
                .id(10L)
                .ordemServicoId(1L)
                .numeroOs("OS-123")
                .tipo(TipoOrcamento.PRINCIPAL)
                .versao(1)
                .status(StatusOrcamento.DISPONIVEL)
                .criadoEm(LocalDateTime.of(2026, 5, 31, 10, 0))
                .disponibilizadoEm(LocalDateTime.of(2026, 5, 31, 10, 1))
                .totalServicos(new BigDecimal("100.00"))
                .totalItens(new BigDecimal("50.00"))
                .totalGeral(new BigDecimal("150.00"))
                .servicos(List.of(OrcamentoServicoEntity.builder()
                        .servicoId(20L)
                        .nome("Revisao")
                        .valor(new BigDecimal("100.00"))
                        .build()))
                .itens(List.of(OrcamentoItemNecessarioEntity.builder()
                        .pecaInsumoId(30L)
                        .servicoOsId(40L)
                        .nome("Filtro")
                        .tipo(CategoriaPecaInsumo.PECA)
                        .valorUnitario(new BigDecimal("25.00"))
                        .quantidade(2)
                        .valorTotal(new BigDecimal("50.00"))
                        .build()))
                .cliente(ClienteOrcamentoSnapshot.builder()
                        .nome("Cliente")
                        .cpfCnpj("12345678901")
                        .email("cliente@exemplo.com")
                        .telefone("11999999999")
                        .build())
                .veiculo(VeiculoOrcamentoSnapshot.builder()
                        .placa("ABC1234")
                        .marca("Fiat")
                        .modelo("Uno")
                        .ano(2020)
                        .build())
                .build();
    }
}
