package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.port.in.ordemservico.reparoadicional.AprovarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.application.port.in.orcamento.AprovarOrcamentoUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;


@RequiredArgsConstructor
public class AprovarOrcamentoUseCaseImpl implements AprovarOrcamentoUseCase {

    private final AprovarReparoAdicionalPorOrcamentoUseCase aprovarReparoAdicionalPorOrcamentoUseCase;
    private final OrdemServicoGateway ordemServicoGateway;
    private final OrcamentoGateway orcamentoGateway;

    @TransactionalUseCase
    @Override
    public Orcamento execute(Orcamento orcamento, String assinaturaNome
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

        orcamento.aprovar(assinaturaNome, LocalDateTime.now(ZoneId.systemDefault()));

        Orcamento orcamentoSalvo = orcamentoGateway.save(orcamento);

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
