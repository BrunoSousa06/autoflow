package com.autoflow.application.gateway;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.repository.ordemservico.TempoMedioOrdemServicoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface OrdemServicoGateway {

    OrdemServicoEntity save(OrdemServicoEntity ordemServico);

    Optional<OrdemServicoEntity> findById(Long id);

    Optional<OrdemServicoEntity> findByNumeroOs(String numeroOs);

    List<OrdemServicoEntity> findByClienteIdOrderByDataAberturaDesc(Long clienteId);

    List<OrdemServicoEntity> findAllByOrderByDataAberturaDesc();

    Page<OrdemServicoEntity> findAll(
            Specification<OrdemServicoEntity> specification,
            Pageable pageable);



    TempoMedioOrdemServicoProjection calcularTempoMedioFinalizacao();
}
