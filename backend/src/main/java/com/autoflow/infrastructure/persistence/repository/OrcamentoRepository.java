package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.infrastructure.persistence.entity.orcamento.OrcamentoPersistenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface OrcamentoRepository extends JpaRepository<OrcamentoPersistenceEntity, Long>,
        JpaSpecificationExecutor<OrcamentoPersistenceEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrcamentoPersistenceEntity o where o.id = :id")
    Optional<OrcamentoPersistenceEntity> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrcamentoPersistenceEntity> findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(Long ordemServicoId, TipoOrcamento tipoOrcamento);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrcamentoPersistenceEntity> findTopByNumeroOsAndTipoOrderByVersaoDesc(String numeroOs, TipoOrcamento tipoOrcamento);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrcamentoPersistenceEntity> findByOrdemServicoIdAndTipoAndStatus(
            Long ordemServicoId, TipoOrcamento tipo, StatusOrcamento status);

    Optional<OrcamentoPersistenceEntity> findByOrdemServicoIdAndStatus(Long ordemServicoId, StatusOrcamento status);

    Optional<OrcamentoPersistenceEntity> findByNumeroOsAndStatus(String numeroOs, StatusOrcamento status);

    Optional<OrcamentoPersistenceEntity> findTopByNumeroOsOrderByVersaoDesc(String numeroOs);
}
