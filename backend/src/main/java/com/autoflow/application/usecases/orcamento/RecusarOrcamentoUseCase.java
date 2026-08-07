package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.usecases.ordemservico.reparoadicional.RecusarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class RecusarOrcamentoUseCase {

    private final RecusarReparoAdicionalPorOrcamentoUseCase recusarReparoAdicionalPorOrcamentoUseCase;
    private final OrcamentoGateway orcamentoGateway;
    private final OrdemServicoGateway ordemServicoGateway;

    @Transactional
    public OrcamentoEntity execute(OrcamentoEntity orcamento, String motivo, String assinaturaNome){
        if (orcamento.getStatus() == StatusOrcamento.APROVADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento já aprovado, não é possivel recusar");
        }
        if (orcamento.getStatus() == StatusOrcamento.REPROVADO) {
            return orcamento;
        }

        if (orcamento.getStatus() != StatusOrcamento.DISPONIVEL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento não esta disponivel");
        }

        orcamento.setStatus(StatusOrcamento.REPROVADO);
        orcamento.setReprovadoEm(LocalDateTime.now(ZoneId.systemDefault()));
        orcamento.setAssinaturaNome(assinaturaNome);
        if(motivo != null) orcamento.setRecusaMotivo(motivo);

        OrcamentoEntity orcamentoSalvo = orcamentoGateway.save(orcamento);

        if (recusarReparoAdicionalPorOrcamentoUseCase.executeSeExistir(orcamento.getId(), motivo)) {
            return orcamentoSalvo;
        }

        OrdemServicoEntity ordemServico = ordemServicoGateway.findByNumeroOs(orcamento.getNumeroOs())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OS nao encontrada"));
        ordemServico.finalizarPorOrcamentoRecusado();
        ordemServicoGateway.save(ordemServico);
        return orcamento;
    }
}
