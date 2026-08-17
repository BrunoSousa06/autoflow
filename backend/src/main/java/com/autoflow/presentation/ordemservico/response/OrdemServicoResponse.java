package com.autoflow.presentation.ordemservico.response;

import com.autoflow.application.dto.ordemservico.OrdemServicoCriadaOutput;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;
import java.util.List;

public record OrdemServicoResponse(
        Long id,
        String numeroOs,
        String clienteNome,
        String clienteCpfCnpj,
        StatusOrdemServico status,
        LocalDateTime dataAbertura,
        LocalDateTime execucaoIniciadaEm,
        LocalDateTime finalizadaEm,
        LocalDateTime entregueEm,
        List<ServicoOsResponse> servicos,
        String acompanhamentoUrl
) {

    public static OrdemServicoResponse fromDomain(
            OrdemServicoCriadaOutput resultado,
            String acompanhamentoUrl
    ) {
        return fromEntity(resultado.ordemServico(), acompanhamentoUrl);
    }

    public static OrdemServicoResponse fromDomain(
            OrdemServico ordemServico
    ) {
        return fromEntity(ordemServico, null);
    }

    private static OrdemServicoResponse fromEntity(
            OrdemServico ordemServico,
            String acompanhamentoUrl
    ) {
        return new OrdemServicoResponse(
                ordemServico.getId(),
                ordemServico.getNumeroOs(),
                ordemServico.getCliente().getNome(),
                ordemServico.getCliente().getCpfCnpj(),
                ordemServico.getStatus(),
                ordemServico.getDataAbertura(),
                ordemServico.getExecucaoIniciadaEm(),
                ordemServico.getFinalizadaEm(),
                ordemServico.getEntregueEm(),
                ordemServico.getServicosSolicitados().stream()
                        .map(ServicoOsResponse::fromDomain)
                        .toList(),
                acompanhamentoUrl
        );
    }
}
