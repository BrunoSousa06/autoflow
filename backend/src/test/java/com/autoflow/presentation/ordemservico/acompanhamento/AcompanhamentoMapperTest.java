package com.autoflow.presentation.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.OrcamentoResumoOutput;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AcompanhamentoMapperTest {

    private final AcompanhamentoControllerMapper mapper =
            Mappers.getMapper(AcompanhamentoControllerMapper.class);

    @Test
    void devePreservarContratoConsumidoPeloFrontend() {
        var output = new AcompanhamentoOrdemServicoOutput(
                "OS-001", "ABC1D23", StatusOrdemServico.AGUARDANDO_APROVACAO,
                null, null, List.of(),
                new OrcamentoResumoOutput(null, null, null, StatusOrcamento.DISPONIVEL,
                        null, null, null, null, null, null, null,
                        "Orçamento disponível para aprovação."),
                StatusOrcamento.DISPONIVEL,
                "O orçamento está disponível e aguardando sua aprovação.",
                List.of());
        var response = mapper.toResponse(output);

        assertAll(
                () -> assertEquals("OS-001", response.numeroOs()),
                () -> assertEquals("ABC1D23", response.placa()),
                () -> assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, response.statusAtual()),
                () -> assertEquals(StatusOrcamento.DISPONIVEL, response.situacaoAprovacao()),
                () -> assertNotNull(response.orcamentoAtual()),
                () -> assertEquals(
                        "O orçamento está disponível e aguardando sua aprovação.",
                        response.mensagemParaCliente()),
                () -> assertNotNull(response.servicosSolicitados()),
                () -> assertNotNull(response.historicoStatus()));
    }
}
