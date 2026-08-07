package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.usecases.ordemservico.reparoadicional.AprovarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
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
public class AprovarOrcamentoUseCase {

    private final AprovarReparoAdicionalPorOrcamentoUseCase aprovarReparoAdicionalPorOrcamentoUseCase;
    private final OrdemServicoGateway ordemServicoGateway;
    private final OrcamentoGateway orcamentoGateway;

    @Transactional
    public OrcamentoEntity execute(OrcamentoEntity orcamento, String assinaturaNome
    ){
        if (orcamento.getStatus() == StatusOrcamento.APROVADO || orcamento.getStatus() == StatusOrcamento.REPROVADO) {
            return orcamento;
        }
        if (orcamento.getStatus() != StatusOrcamento.DISPONIVEL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento nao esta disponível");
        }

        orcamento.setStatus(StatusOrcamento.APROVADO);
        orcamento.setAssinaturaNome(assinaturaNome);
        orcamento.setAprovadoEm(LocalDateTime.now(ZoneId.systemDefault()));

        OrcamentoEntity orcamentoSalvo = orcamentoGateway.save(orcamento);

        if (aprovarReparoAdicionalPorOrcamentoUseCase.executeSeExistir(orcamento.getId())) {
            return orcamentoSalvo;
        }

        OrdemServicoEntity ordemServico = ordemServicoGateway.findById(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OS nao encontrada"));
        ordemServico.iniciarExecucao();
        ordemServicoGateway.save(ordemServico);

        return orcamentoGateway.save(orcamento);
    }
}
