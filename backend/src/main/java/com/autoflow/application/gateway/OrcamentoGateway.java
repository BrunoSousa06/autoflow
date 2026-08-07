package com.autoflow.application.gateway;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;

import java.util.Optional;
import java.util.List;
import com.autoflow.application.dto.orcamento.OrcamentoFiltro;

public interface OrcamentoGateway {

    OrcamentoEntity save(OrcamentoEntity orcamento);

    Optional<OrcamentoEntity> findById(Long id);

    Optional<OrcamentoEntity> findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(
            Long ordemServicoId,
            TipoOrcamento tipoOrcamento);

    Optional<OrcamentoEntity> findTopByNumeroOsAndTipoOrderByVersaoDesc(
            String numeroOs,
            TipoOrcamento tipoOrcamento);

    Optional<OrcamentoEntity> findByOrdemServicoIdAndStatus(
            Long ordemServicoId,
            StatusOrcamento status);

    Optional<OrcamentoEntity> findByNumeroOsAndStatus(
            String numeroOs,
            StatusOrcamento status);

    Optional<OrcamentoEntity> findTopByNumeroOsOrderByVersaoDesc(
            String numeroOs);

    List<OrcamentoEntity> findAll(OrcamentoFiltro filtro);
}
