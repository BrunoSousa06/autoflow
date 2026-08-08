package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.infrastructure.persistence.repository.reparoadicional.ReparoAdicionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReparoAdicionalPersistenceAdapter implements ReparoAdicionalGateway {

    private final ReparoAdicionalRepository repository;

    @Override
    public ReparoAdicionalEntity save(ReparoAdicionalEntity reparoAdicional) {
        return repository.save(reparoAdicional);
    }

    @Override
    public Optional<ReparoAdicionalEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<ReparoAdicionalEntity> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id);
    }

    @Override
    public Optional<ReparoAdicionalEntity> findByOrcamentoId(Long orcamentoId) {
        return repository.findByOrcamentoId(orcamentoId);
    }
}
