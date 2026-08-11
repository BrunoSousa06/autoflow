package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.usecases.ordemservico.reparoadicional.RecusarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;


@RequiredArgsConstructor
public class RecusarOrcamentoUseCase {

    private final RecusarReparoAdicionalPorOrcamentoUseCase recusarReparoAdicionalPorOrcamentoUseCase;
    private final OrcamentoGateway orcamentoGateway;
    private final OrdemServicoGateway ordemServicoGateway;

    @TransactionalUseCase
    public OrcamentoEntity execute(OrcamentoEntity orcamento, String motivo, String assinaturaNome) {
        if (orcamento.getStatus() == StatusOrcamento.APROVADO) {
            throw ApplicationException.badRequest("Orçamento já aprovado, não é possivel recusar");
        }
        if (orcamento.getStatus() == StatusOrcamento.REPROVADO) {
            return orcamento;
        }

        if (orcamento.getStatus() != StatusOrcamento.DISPONIVEL) {
            throw ApplicationException.badRequest("Orçamento não esta disponivel");
        }

        orcamento.setStatus(StatusOrcamento.REPROVADO);
        orcamento.setReprovadoEm(LocalDateTime.now(ZoneId.systemDefault()));
        orcamento.setAssinaturaNome(assinaturaNome);
        if (motivo != null) {
            String motivoNormalizado = motivo.trim();
            if (motivoNormalizado.length() > 500) {
                throw ApplicationException.badRequest(
                        "Motivo da recusa deve ter no máximo 500 caracteres");
            }
            orcamento.setRecusaMotivo(
                    motivoNormalizado.isBlank() ? null : motivoNormalizado);
        }

        OrcamentoEntity orcamentoSalvo = orcamentoGateway.save(orcamento);

        if (recusarReparoAdicionalPorOrcamentoUseCase.executeSeExistir(
                orcamento.getId(), orcamento.getRecusaMotivo())) {
            return orcamentoSalvo;
        }

        OrdemServicoEntity ordemServico = ordemServicoGateway.findByNumeroOs(orcamento.getNumeroOs())
                .orElseThrow(() -> ApplicationException.notFound("OS nao encontrada"));
        ordemServico.finalizarPorOrcamentoRecusado();
        ordemServicoGateway.save(ordemServico);
        return orcamentoSalvo;
    }
}
