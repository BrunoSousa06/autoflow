package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import com.autoflow.infrastructure.persistence.repository.reparoadicional.ReparoAdicionalRepository;
import com.autoflow.infrastructure.persistence.mapper.ordemservico.ReparoAdicionalPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReparoAdicionalPersistenceAdapter implements ReparoAdicionalGateway {

    private final ReparoAdicionalRepository repository;
    private final ReparoAdicionalPersistenceMapper mapper;

    @Override
    public ReparoAdicional save(ReparoAdicional reparoAdicional) {
        return mapper.toDomain(repository.save(mapper.toEntity(reparoAdicional)));
    }

    @Override
    public Optional<ReparoAdicional> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ReparoAdicional> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ReparoAdicional> findByOrcamentoId(Long orcamentoId) {
        return repository.findByOrcamentoId(orcamentoId).map(mapper::toDomain);
    }
}
