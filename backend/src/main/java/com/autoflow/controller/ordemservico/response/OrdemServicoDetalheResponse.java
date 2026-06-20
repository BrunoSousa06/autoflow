package com.autoflow.controller.ordemservico.response;

import com.autoflow.controller.ordemservico.acompanhamento.response.OrcamentoResumoResponse;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;
import java.util.List;

public record OrdemServicoDetalheResponse(
        Long id,
        String numeroOs,
        StatusOrdemServico status,
        LocalDateTime dataAbertura,
        LocalDateTime ultimaAtualizacao,
        LocalDateTime execucaoIniciadaEm,
        LocalDateTime finalizadaEm,
        LocalDateTime entregueEm,
        ClienteOrdemServicoResponse cliente,
        VeiculoOrdemServicoResponse veiculo,
        List<ServicoOsResponse> servicos,
        OrcamentoResumoResponse orcamentoAtual,
        DiagnosticoDetalheResponse diagnostico
) {
    public static OrdemServicoDetalheResponse fromDomain(
            OrdemServicoEntity os,
            OrcamentoEntity orcamentoAtual
    ) {
        return new OrdemServicoDetalheResponse(
                os.getId(),
                os.getNumeroOs(),
                os.getStatus(),
                os.getDataAbertura(),
                os.getUltimaAtualizacao(),
                os.getExecucaoIniciadaEm(),
                os.getFinalizadaEm(),
                os.getEntregueEm(),
                ClienteOrdemServicoResponse.fromDomain(os),
                VeiculoOrdemServicoResponse.fromDomain(os),
                os.getServicosSolicitados().stream()
                        .map(ServicoOsResponse::fromDomain)
                        .toList(),
                orcamentoAtual == null ? null : OrcamentoResumoResponse.from(orcamentoAtual),
                DiagnosticoDetalheResponse.fromDomain(os.getDiagnostico())
        );
    }
}