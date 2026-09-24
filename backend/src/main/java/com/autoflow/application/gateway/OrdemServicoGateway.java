package com.autoflow.application.gateway;

import com.autoflow.application.input.PageQuery;
import com.autoflow.application.input.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.application.output.PageResult;
import com.autoflow.domain.ordemservico.OrdemServico;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoGateway {

    OrdemServico save(OrdemServico ordemServico);

    Optional<OrdemServico> findById(Long id);

    Optional<OrdemServico> findByNumeroOs(String numeroOs);

    Optional<OrdemServico> findByNumeroOsForUpdate(String numeroOs);

    List<OrdemServico> findByClienteIdOrderByDataAberturaDesc(Long clienteId);

    List<OrdemServico> findAllByOrderByDataAberturaDesc();

    PageResult<OrdemServico> findAll(
            OrdemServicoFiltroInput filtro,
            String emailMecanico,
            PageQuery pageQuery);

}
