package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.dto.orcamento.OrcamentoFiltro;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.infrastructure.persistence.mapper.OrcamentoPersistenceMapper;
import com.autoflow.infrastructure.persistence.repository.OrcamentoRepository;
import com.autoflow.infrastructure.persistence.repository.OrcamentoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrcamentoRepositoryAdapter implements OrcamentoGateway {

    private final OrcamentoRepository repository;
    private final OrcamentoPersistenceMapper mapper;

    @Override
    public OrcamentoEntity save(OrcamentoEntity orcamento) {
        return mapper.toDomain(repository.save(mapper.toEntity(orcamento)));
    }

    @Override
    public Optional<OrcamentoEntity> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<OrcamentoEntity> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public Optional<OrcamentoEntity> findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(
            Long ordemServicoId,
            TipoOrcamento tipoOrcamento) {

        return repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(
                ordemServicoId, tipoOrcamento).map(mapper::toDomain);
    }

    @Override
    public Optional<OrcamentoEntity> findTopByNumeroOsAndTipoOrderByVersaoDesc(
            String numeroOs,
            TipoOrcamento tipoOrcamento) {

        return repository.findTopByNumeroOsAndTipoOrderByVersaoDesc(
                numeroOs, tipoOrcamento).map(mapper::toDomain);
    }

    @Override
    public Optional<OrcamentoEntity> findByOrdemServicoIdAndStatus(
            Long ordemServicoId,
            StatusOrcamento status) {

        return repository.findByOrdemServicoIdAndStatus(
                ordemServicoId, status).map(mapper::toDomain);
    }

    @Override
    public Optional<OrcamentoEntity> findByNumeroOsAndStatus(
            String numeroOs,
            StatusOrcamento status) {

        return repository.findByNumeroOsAndStatus(numeroOs, status).map(mapper::toDomain);
    }

    @Override
    public Optional<OrcamentoEntity> findTopByNumeroOsOrderByVersaoDesc(
            String numeroOs) {

        return repository.findTopByNumeroOsOrderByVersaoDesc(numeroOs).map(mapper::toDomain);
    }

    @Override
    public List<OrcamentoEntity> findAll(OrcamentoFiltro filtro) {
        return repository.findAll(OrcamentoSpecifications.comFiltros(filtro)).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
