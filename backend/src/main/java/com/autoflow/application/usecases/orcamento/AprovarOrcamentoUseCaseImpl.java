package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.port.in.orcamento.AprovarOrcamentoUseCase;
import com.autoflow.application.port.in.ordemservico.reparoadicional.AprovarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.usecases.ordemservico.RegistrarHistoricoStatusOsService;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;


@RequiredArgsConstructor
public class AprovarOrcamentoUseCaseImpl implements AprovarOrcamentoUseCase {

    private final AprovarReparoAdicionalPorOrcamentoUseCase aprovarReparoAdicionalPorOrcamentoUseCase;
    private final OrdemServicoGateway ordemServicoGateway;
    private final OrcamentoGateway orcamentoGateway;
    private final RegistrarHistoricoStatusOsService registrarHistoricoStatusOs;
    private final Clock clock;

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

        orcamento.aprovar(assinaturaNome, LocalDateTime.now(clock));

        Orcamento orcamentoSalvo = orcamentoGateway.save(orcamento);

        if (aprovarReparoAdicionalPorOrcamentoUseCase.executeSeExistir(orcamento.getId())) {
            return orcamentoSalvo;
        }

        OrdemServico ordemServico = ordemServicoGateway.findById(orcamento.getOrdemServicoId())
                .orElseThrow(() -> ApplicationException.notFound("OS nao encontrada"));
        ordemServico.iniciarExecucao(LocalDateTime.now(clock));
        ordemServicoGateway.save(ordemServico);
        registrarHistoricoStatusOs.registrar(ordemServico);

        return orcamentoSalvo;
    }
}
