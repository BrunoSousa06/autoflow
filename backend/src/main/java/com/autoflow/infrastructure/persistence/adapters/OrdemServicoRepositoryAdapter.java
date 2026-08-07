package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrdemServicoRepositoryAdapter implements OrdemServicoGateway {

    private final OrdemServicoRepository repository;

    @Override
    public OrdemServicoEntity save(OrdemServicoEntity ordemServico) {
        return repository.save(ordemServico);
    }

    @Override
    public Optional<OrdemServicoEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<OrdemServicoEntity> findByNumeroOs(String numeroOs) {
        return repository.findByNumeroOs(numeroOs);
    }

    @Override
    public Optional<OrdemServicoEntity> findByNumeroOsForUpdate(String numeroOs) {
        return repository.findByNumeroOsForUpdate(numeroOs);
    }

    @Override
    public List<OrdemServicoEntity> findByClienteIdOrderByDataAberturaDesc(Long clienteId) {
        return repository.findByCliente_IdOrderByDataAberturaDesc(clienteId);
    }

    @Override
    public List<OrdemServicoEntity> findAllByOrderByDataAberturaDesc() {
        return repository.findAllByOrderByDataAberturaDesc();
    }

    @Override
    public Page<OrdemServicoEntity> findAll(
            Specification<OrdemServicoEntity> specification,
            Pageable pageable) {
        return repository.findAll(specification, pageable);
    }
}
