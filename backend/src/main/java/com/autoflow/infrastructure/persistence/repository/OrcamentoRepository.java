package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;


public interface OrcamentoRepository extends JpaRepository<OrcamentoEntity, Long>,
        JpaSpecificationExecutor<OrcamentoEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrcamentoEntity> findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(Long ordemServicoId, TipoOrcamento tipoOrcamento);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrcamentoEntity> findTopByNumeroOsAndTipoOrderByVersaoDesc(String numeroOs, TipoOrcamento tipoOrcamento);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrcamentoEntity> findByOrdemServicoIdAndTipoAndStatus(
            Long ordemServicoId, TipoOrcamento tipo, StatusOrcamento status);

    Optional<OrcamentoEntity> findByOrdemServicoIdAndStatus(Long ordemServicoId, StatusOrcamento status);

    Optional<OrcamentoEntity> findByNumeroOsAndStatus(String numeroOs, StatusOrcamento status);

    Optional<OrcamentoEntity> findTopByNumeroOsOrderByVersaoDesc(String numeroOs);
}
