package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.gateway.OrcamentoVersioningGateway;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.infrastructure.persistence.entity.orcamento.OrcamentoPersistenceEntity;
import com.autoflow.infrastructure.persistence.repository.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrcamentoVersioningAdapter implements OrcamentoVersioningGateway {
    private final OrcamentoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public int proximaVersao(Long ordemServicoId, TipoOrcamento tipo) {
        return repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(ordemServicoId, tipo)
                .map(OrcamentoPersistenceEntity::getVersao)
                .map(versao -> versao + 1)
                .orElse(1);
    }

    @Override
    @Transactional(readOnly = true)
    public int proximaVersaoPorNumeroOs(String numeroOs, TipoOrcamento tipo) {
        return repository.findTopByNumeroOsAndTipoOrderByVersaoDesc(numeroOs, tipo)
                .map(OrcamentoPersistenceEntity::getVersao)
                .map(versao -> versao + 1)
                .orElse(1);
    }

    @Override
    @Transactional
    public void substituirDisponivelAtual(Long ordemServicoId, TipoOrcamento tipo) {
        repository.findByOrdemServicoIdAndTipoAndStatus(
                        ordemServicoId, tipo, StatusOrcamento.DISPONIVEL)
                .ifPresent(orcamento -> {
                    orcamento.setStatus(StatusOrcamento.SUBSTITUIDO);
                    repository.saveAndFlush(orcamento);
                });
    }
}
