package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrcamentoVersioningService {
    private final OrcamentoRepository repository;

    public int proximaVersaoPrincipal(Long ordemServicId){
        return repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(ordemServicId, TipoOrcamento.PRINCIPAL)
                .map(o -> o.getVersao() + 1)
                .orElse(1);
    }
}
