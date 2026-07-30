package com.autoflow.presentation.acompanhamento.response;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.presentation.ordemservico.acompanhamento.response.OrcamentoResumoResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class OrcamentoResumoResponseTest {

    @ParameterizedTest
    @MethodSource("statusComMensagens")
    void deveRetornarMensagemDeAcordoComStatus(
            StatusOrcamento status,
            String mensagemEsperada
    ) {
        OrcamentoEntity orcamento = new OrcamentoEntity();
        orcamento.setId(1L);
        orcamento.setTipo(TipoOrcamento.PRINCIPAL);
        orcamento.setVersao(1);
        orcamento.setStatus(status);
        orcamento.setTotalServicos(BigDecimal.valueOf(100));
        orcamento.setTotalItens(BigDecimal.valueOf(50));
        orcamento.setTotalGeral(BigDecimal.valueOf(150));
        orcamento.setCriadoEm(LocalDateTime.now());

        OrcamentoResumoResponse response = OrcamentoResumoResponse.from(orcamento);

        assertEquals(mensagemEsperada, response.mensagem());
    }

    private static Stream<Object[]> statusComMensagens() {
        return Stream.of(
                new Object[]{
                        StatusOrcamento.DISPONIVEL,
                        "Orçamento disponível para aprovação."
                },
                new Object[]{
                        StatusOrcamento.APROVADO,
                        "Orçamento aprovado."
                },
                new Object[]{
                        StatusOrcamento.REPROVADO,
                        "Orçamento recusado."
                },
                new Object[]{
                        StatusOrcamento.SUBSTITUIDO,
                        "Este orçamento foi substituído por uma versão mais recente."
                }
        );
    }
}