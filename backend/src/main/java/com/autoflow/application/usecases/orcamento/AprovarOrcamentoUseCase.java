package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.usecases.ordemservico.reparoadicional.AprovarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;


@RequiredArgsConstructor
public class AprovarOrcamentoUseCase {

    private final AprovarReparoAdicionalPorOrcamentoUseCase aprovarReparoAdicionalPorOrcamentoUseCase;
    private final OrdemServicoGateway ordemServicoGateway;
    private final OrcamentoGateway orcamentoGateway;

    @TransactionalUseCase
    public OrcamentoEntity execute(OrcamentoEntity orcamento, String assinaturaNome
    ) {
        if (orcamento.getStatus() == StatusOrcamento.APROVADO) {
            return orcamento;
        }
        if (orcamento.getStatus() == StatusOrcamento.REPROVADO) {
            throw ApplicationException.badRequest(
                    "Orçamento já recusado, não é possível aprovar");
        }
        if (orcamento.getStatus() != StatusOrcamento.DISPONIVEL) {
            throw ApplicationException.badRequest("Orçamento nao esta disponível");
        }

        orcamento.setStatus(StatusOrcamento.APROVADO);
        orcamento.setAssinaturaNome(assinaturaNome);
        orcamento.setAprovadoEm(LocalDateTime.now(ZoneId.systemDefault()));

        OrcamentoEntity orcamentoSalvo = orcamentoGateway.save(orcamento);

        if (aprovarReparoAdicionalPorOrcamentoUseCase.executeSeExistir(orcamento.getId())) {
            return orcamentoSalvo;
        }

        OrdemServico ordemServico = ordemServicoGateway.findById(orcamento.getOrdemServicoId())
                .orElseThrow(() -> ApplicationException.notFound("OS nao encontrada"));
        ordemServico.iniciarExecucao();
        ordemServicoGateway.save(ordemServico);

        return orcamentoSalvo;
    }
}
