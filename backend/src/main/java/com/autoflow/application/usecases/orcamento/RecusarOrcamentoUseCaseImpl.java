package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.port.in.orcamento.RecusarOrcamentoUseCase;
import com.autoflow.application.port.in.ordemservico.reparoadicional.RecusarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.usecases.ordemservico.RegistrarHistoricoStatusOsService;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;


@RequiredArgsConstructor
public class RecusarOrcamentoUseCaseImpl implements RecusarOrcamentoUseCase {

    private final RecusarReparoAdicionalPorOrcamentoUseCase recusarReparoAdicionalPorOrcamentoUseCase;
    private final OrcamentoGateway orcamentoGateway;
    private final OrdemServicoGateway ordemServicoGateway;
    private final RegistrarHistoricoStatusOsService registrarHistoricoStatusOs;
    private final Clock clock;

    @TransactionalUseCase
    @Override
    public Orcamento execute(Orcamento orcamento, String motivo, String assinaturaNome) {
        if (orcamento.getStatus() == StatusOrcamento.APROVADO) {
            throw ApplicationException.badRequest("Orçamento já aprovado, não é possivel recusar");
        }
        if (orcamento.getStatus() == StatusOrcamento.REPROVADO) {
            return orcamento;
        }

        if (orcamento.getStatus() != StatusOrcamento.DISPONIVEL) {
            throw ApplicationException.badRequest("Orçamento não esta disponivel");
        }

        String motivoNormalizado = null;
        if (motivo != null) {
            motivoNormalizado = motivo.trim();
            if (motivoNormalizado.length() > 500) {
                throw ApplicationException.badRequest(
                        "Motivo da recusa deve ter no máximo 500 caracteres");
            }
            motivoNormalizado = motivoNormalizado.isBlank() ? null : motivoNormalizado;
        }
        orcamento.recusar(motivoNormalizado, assinaturaNome, LocalDateTime.now(clock));

        Orcamento orcamentoSalvo = orcamentoGateway.save(orcamento);

        if (recusarReparoAdicionalPorOrcamentoUseCase.executeSeExistir(
                orcamento.getId(), orcamento.getRecusaMotivo())) {
            return orcamentoSalvo;
        }

        OrdemServico ordemServico = ordemServicoGateway.findByNumeroOs(orcamento.getNumeroOs())
                .orElseThrow(() -> ApplicationException.notFound("OS nao encontrada"));
        ordemServico.finalizarPorOrcamentoRecusado(LocalDateTime.now(clock));
        ordemServicoGateway.save(ordemServico);
        registrarHistoricoStatusOs.registrar(ordemServico);
        return orcamentoSalvo;
    }
}
