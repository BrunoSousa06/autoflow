package com.autoflow.controller.orcamento;

import com.autoflow.application.gateway.OrcamentoDocumentoGateway;
import com.autoflow.application.usecases.orcamento.ConsultarOrcamentoPorTokenUseCase;
import com.autoflow.infrastructure.security.service.CustomUserDetailsService;
import com.autoflow.infrastructure.security.service.JwtService;
import com.autoflow.domain.orcamento.ClienteOrcamentoSnapshot;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.orcamento.VeiculoOrcamentoSnapshot;
import com.autoflow.application.usecases.ordemservico.acompanhamento.AcessarOrcamentoAcompanhamentoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicOrcamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicOrcamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrcamentoDocumentoGateway orcamentoDocumentoGateway;

    @MockitoBean
    private ConsultarOrcamentoPorTokenUseCase consultarOrcamentoPorTokenUseCase;

    @MockitoBean
    private AcessarOrcamentoAcompanhamentoUseCase acessarOrcamentoAcompanhamentoUseCase;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void deveBaixarPdfValidandoTokenPublico() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        byte[] pdf = "%PDF fake".getBytes();

        when(consultarOrcamentoPorTokenUseCase.execute(10L, "tok")).thenReturn(orcamento);
        when(orcamentoDocumentoGateway.gerarPdf(orcamento)).thenReturn(pdf);

        mockMvc.perform(get("/public/orcamentos/{id}/pdf", 10L)
                        .param("token", "tok"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"orcamento-10.pdf\""));

        verify(consultarOrcamentoPorTokenUseCase).execute(10L, "tok");
        verify(orcamentoDocumentoGateway).gerarPdf(orcamento);
    }

    @Test
    void deveRetornarUnauthorizedQuandoTokenPublicoForInvalido() throws Exception {


        doThrow(new ResponseStatusException(UNAUTHORIZED, "Token invalido"))
                .when(consultarOrcamentoPorTokenUseCase)
                .execute(10L, "tok-invalido");

        mockMvc.perform(get("/public/orcamentos/{id}/pdf", 10L)
                        .param("token", "tok-invalido"))
                .andExpect(status().isUnauthorized());

        verify(consultarOrcamentoPorTokenUseCase).execute(10L, "tok-invalido");
    }

    @Test
    void deveBaixarPdfComTokenDeAcompanhamento() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        when(acessarOrcamentoAcompanhamentoUseCase.consultar(10L, "token-os")).thenReturn(orcamento);
        when(orcamentoDocumentoGateway.gerarPdf(orcamento)).thenReturn("%PDF".getBytes());

        mockMvc.perform(get("/public/orcamentos/{id}/pdf/acompanhamento", 10L)
                        .param("token", "token-os"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE));

        verify(acessarOrcamentoAcompanhamentoUseCase).consultar(10L, "token-os");
    }

    @Test
    void deveAprovarComTokenDeAcompanhamento() throws Exception {
        OrcamentoEntity orcamento = baseOrcamento();
        orcamento.setStatus(StatusOrcamento.APROVADO);
        when(acessarOrcamentoAcompanhamentoUseCase.aprovar(10L, "token-os")).thenReturn(orcamento);

        mockMvc.perform(post("/public/orcamentos/{id}/aprovar/acompanhamento", 10L)
                        .param("token", "token-os"))
                .andExpect(status().isOk());

        verify(acessarOrcamentoAcompanhamentoUseCase).aprovar(10L, "token-os");
    }

    private OrcamentoEntity baseOrcamento() {
        return OrcamentoEntity.builder()
                .id(10L)
                .ordemServicoId(1L)
                .numeroOs("OS-123")
                .tipo(TipoOrcamento.PRINCIPAL)
                .versao(1)
                .status(StatusOrcamento.DISPONIVEL)
                .criadoEm(LocalDateTime.of(2026, Month.MAY, 31, 10, 0))
                .disponibilizadoEm(LocalDateTime.of(2026, Month.MAY, 31, 10, 1))
                .totalServicos(new BigDecimal("100.00"))
                .totalItens(new BigDecimal("50.00"))
                .totalGeral(new BigDecimal("150.00"))
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
