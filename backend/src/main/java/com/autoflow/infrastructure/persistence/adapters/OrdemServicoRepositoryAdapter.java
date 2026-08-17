package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.input.PageQuery;
import com.autoflow.application.output.PageResult;
import com.autoflow.application.input.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.infrastructure.persistence.entity.ordemservico.OrdemServicoEntity;
import com.autoflow.infrastructure.persistence.mapper.ordemservico.OrdemServicoPersistenceMapper;
import com.autoflow.infrastructure.persistence.repository.OrdemServicoRepository;
import com.autoflow.infrastructure.persistence.repository.OrdemServicoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrdemServicoRepositoryAdapter implements OrdemServicoGateway {

    private final OrdemServicoRepository repository;
    private final OrdemServicoPersistenceMapper mapper;

    @Override
    public OrdemServico save(OrdemServico ordemServico) {
        return mapper.toDomain(repository.save(mapper.toEntity(ordemServico)));
    }

    @Override
    public Optional<OrdemServico> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<OrdemServico> findByNumeroOs(String numeroOs) {
        return repository.findByNumeroOs(numeroOs).map(mapper::toDomain);
    }

    @Override
    public Optional<OrdemServico> findByNumeroOsForUpdate(String numeroOs) {
        return repository.findByNumeroOsForUpdate(numeroOs).map(mapper::toDomain);
    }

    @Override
    public List<OrdemServico> findByClienteIdOrderByDataAberturaDesc(Long clienteId) {
        return repository.findByCliente_IdOrderByDataAberturaDesc(clienteId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<OrdemServico> findAllByOrderByDataAberturaDesc() {
        return repository.findAllByOrderByDataAberturaDesc().stream().map(mapper::toDomain).toList();
    }

    @Override
    public PageResult<OrdemServico> findAll(
            OrdemServicoFiltroInput filtro,
            String emailMecanico,
            PageQuery pageQuery) {
        Specification<OrdemServicoEntity> specification =
                OrdemServicoSpecifications.comFiltros(filtro, emailMecanico);
        Page<OrdemServicoEntity> page = repository.findAll(
                specification,
                PageRequest.of(pageQuery.page(), pageQuery.size()));
        return new PageResult<>(page.getContent().stream().map(mapper::toDomain).toList(), page.getTotalElements(),
                pageQuery.page(), pageQuery.size());
    }
}
