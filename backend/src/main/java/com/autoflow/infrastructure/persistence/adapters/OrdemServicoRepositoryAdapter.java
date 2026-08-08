package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.infrastructure.persistence.repository.OrdemServicoRepository;
import com.autoflow.infrastructure.persistence.repository.OrdemServicoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public PageResult<OrdemServicoEntity> findAll(
            OrdemServicoFiltroInput filtro,
            String emailMecanico,
            PageQuery pageQuery) {
        Specification<OrdemServicoEntity> specification =
                OrdemServicoSpecifications.comFiltros(filtro, emailMecanico);
        Page<OrdemServicoEntity> page = repository.findAll(
                specification,
                PageRequest.of(pageQuery.page(), pageQuery.size(),
                        Sort.by(Sort.Direction.DESC, "dataAbertura")));
        return new PageResult<>(page.getContent(), page.getTotalElements(),
                pageQuery.page(), pageQuery.size());
    }
}
