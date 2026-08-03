package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AcompanhamentoMapperTest {

    private final AcompanhamentoMapper mapper =
            Mappers.getMapper(AcompanhamentoMapper.class);

    @Test
    void devePreservarContratoConsumidoPeloFrontend() {
        var veiculo = new VeiculoEntity();
        veiculo.setPlaca("ABC1D23");

        var ordem = new OrdemServicoEntity();
        ordem.setNumeroOs("OS-001");
        ordem.setVeiculo(veiculo);
        ordem.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        ordem.setDataAbertura(LocalDateTime.of(2026, 7, 1, 10, 0));
        ordem.setUltimaAtualizacao(LocalDateTime.of(2026, 7, 2, 11, 0));
        ordem.setServicosSolicitados(List.of());

        var orcamento = new OrcamentoEntity();
        orcamento.setStatus(StatusOrcamento.DISPONIVEL);

        var output = mapper.mapToOutPut(ordem, orcamento, List.of());
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
