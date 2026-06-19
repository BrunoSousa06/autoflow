package com.autoflow.service.orcamento.impl;

import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrcamentoVersioningServiceImpl implements OrcamentoVersioningService {
    private final OrcamentoRepository repository;

    @Override
    public int proximaVersaoPrincipal(Long ordemServicId){
        return repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(ordemServicId, TipoOrcamento.PRINCIPAL)
                .map(o -> o.getVersao() + 1)
                .orElse(1);
    }

    @Override
    public int proximaVersaoPrincipalNumeroOs(String numeroOs){
        return repository.findTopByNumeroOsAndTipoOrderByVersaoDesc(numeroOs, TipoOrcamento.PRINCIPAL)
                .map(o -> o.getVersao() + 1)
                .orElse(1);
    }

    @Override
    @Transactional
    public void substituirDisponivelAtual(Long ordemServicoId) {
        repository.findByOrdemServicoIdAndStatus(
                ordemServicoId,
                StatusOrcamento.DISPONIVEL
        ).ifPresent(orcamentoAtual -> {
            orcamentoAtual.setStatus(StatusOrcamento.SUBSTITUIDO);
            repository.saveAndFlush(orcamentoAtual);
        });
    }

    @Override
    public int proximaVersaoAdicional(Long ordemServicoId) {
        return repository
                .findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(
                        ordemServicoId,
                        TipoOrcamento.COMPLEMENTAR
                )
                .map(orcamento -> orcamento.getVersao() + 1)
                .orElse(1);
    }
}
