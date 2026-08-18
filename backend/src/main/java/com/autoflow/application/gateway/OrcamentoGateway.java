package com.autoflow.application.gateway;

import com.autoflow.application.input.orcamento.OrcamentoFiltro;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;

import java.util.List;
import java.util.Optional;

public interface OrcamentoGateway {

    Orcamento save(Orcamento orcamento);

    Optional<Orcamento> findById(Long id);

    default Optional<Orcamento> findByIdForUpdate(Long id) {
        return findById(id);
    }

    Optional<Orcamento> findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(
            Long ordemServicoId,
            TipoOrcamento tipoOrcamento);

    Optional<Orcamento> findTopByNumeroOsAndTipoOrderByVersaoDesc(
            String numeroOs,
            TipoOrcamento tipoOrcamento);

    Optional<Orcamento> findByOrdemServicoIdAndStatus(
            Long ordemServicoId,
            StatusOrcamento status);

    Optional<Orcamento> findByNumeroOsAndStatus(
            String numeroOs,
            StatusOrcamento status);

    Optional<Orcamento> findTopByNumeroOsOrderByVersaoDesc(
            String numeroOs);

    List<Orcamento> findAll(OrcamentoFiltro filtro);
}
