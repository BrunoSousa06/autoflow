package com.autoflow.application.gateway;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoGateway {

    OrdemServicoEntity save(OrdemServicoEntity ordemServico);

    Optional<OrdemServicoEntity> findById(Long id);

    Optional<OrdemServicoEntity> findByNumeroOs(String numeroOs);

    Optional<OrdemServicoEntity> findByNumeroOsForUpdate(String numeroOs);

    List<OrdemServicoEntity> findByClienteIdOrderByDataAberturaDesc(Long clienteId);

    List<OrdemServicoEntity> findAllByOrderByDataAberturaDesc();

    PageResult<OrdemServicoEntity> findAll(
            OrdemServicoFiltroInput filtro,
            String emailMecanico,
            PageQuery pageQuery);

}
