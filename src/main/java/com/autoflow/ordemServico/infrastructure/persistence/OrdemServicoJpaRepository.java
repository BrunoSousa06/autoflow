package com.autoflow.ordemServico.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, UUID> {

    Optional<OrdemServicoEntity> findByNumeroOs(String numeroOs);
}
