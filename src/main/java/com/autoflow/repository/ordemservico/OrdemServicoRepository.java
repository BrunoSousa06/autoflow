package com.autoflow.repository.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface OrdemServicoRepository extends JpaRepository<OrdemServicoEntity, Long> {

    List<OrdemServicoEntity> findByCliente_IdOrderByDataAberturaDesc(Long clienteId);

    List<OrdemServicoEntity> findAllByOrderByDataAberturaDesc();

    Optional<OrdemServicoEntity> findByNumeroOs(String numeroOs);
}
