package com.autoflow.repository.ordemServico;

import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface OrdemServicoRepository extends JpaRepository<OrdemServicoEntity, Long> {

    Optional<OrdemServicoEntity> findByNumeroOs(String numeroOs);
}
