package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.input.orcamento.OrcamentoFiltro;
import com.autoflow.domain.orcamento.Orcamento;
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
    public Orcamento save(Orcamento orcamento) {
        return mapper.toDomain(repository.save(mapper.toEntity(orcamento)));
    }

    @Override
    public Optional<Orcamento> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Orcamento> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Orcamento> findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(
            Long ordemServicoId,
            TipoOrcamento tipoOrcamento) {

        return repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(
                ordemServicoId, tipoOrcamento).map(mapper::toDomain);
    }

    @Override
    public Optional<Orcamento> findTopByNumeroOsAndTipoOrderByVersaoDesc(
            String numeroOs,
            TipoOrcamento tipoOrcamento) {

        return repository.findTopByNumeroOsAndTipoOrderByVersaoDesc(
                numeroOs, tipoOrcamento).map(mapper::toDomain);
    }

    @Override
    public Optional<Orcamento> findByOrdemServicoIdAndStatus(
            Long ordemServicoId,
            StatusOrcamento status) {

        return repository.findByOrdemServicoIdAndStatus(
                ordemServicoId, status).map(mapper::toDomain);
    }

    @Override
    public Optional<Orcamento> findByNumeroOsAndStatus(
            String numeroOs,
            StatusOrcamento status) {

        return repository.findByNumeroOsAndStatus(numeroOs, status).map(mapper::toDomain);
    }

    @Override
    public Optional<Orcamento> findTopByNumeroOsOrderByVersaoDesc(
            String numeroOs) {

        return repository.findTopByNumeroOsOrderByVersaoDesc(numeroOs).map(mapper::toDomain);
    }

    @Override
    public List<Orcamento> findAll(OrcamentoFiltro filtro) {
        return repository.findAll(OrcamentoSpecifications.comFiltros(filtro)).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
