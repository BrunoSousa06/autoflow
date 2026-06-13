package com.autoflow.repository.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface OrcamentoRepository extends JpaRepository<OrcamentoEntity, Long> {

    Boolean existsByOrdemServicoIdAndStatus(Long ordemServicoId, StatusOrcamento status);

    Optional<OrcamentoEntity> findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(Long ordemServicoId, TipoOrcamento tipoOrcamento);
    Optional<OrcamentoEntity> findTopByNumeroOsAndTipoOrderByVersaoDesc(String numeroOs, TipoOrcamento tipoOrcamento);

    Boolean existsByOrdemServicoIdAndTipoAndStatus(Long ordemServicoId, TipoOrcamento tipo, StatusOrcamento status);

    Optional<OrcamentoEntity> findByOrdemServicoIdAndStatus(Long ordemServicoId, StatusOrcamento status);
    Optional<OrcamentoEntity> findByNumeroOsAndStatus(String numeroOs, StatusOrcamento status);

    Optional<OrcamentoEntity> findTopByOrdemServicoIdOrderByVersaoDesc(Long ordemServicoId);
    Optional<OrcamentoEntity> findTopByNumeroOsOrderByVersaoDesc(String numeroOs);

    List<OrcamentoEntity> findByStatus(StatusOrcamento status);
}
