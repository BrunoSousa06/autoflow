package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<VeiculoEntity, Long>, JpaSpecificationExecutor<VeiculoEntity> {
    boolean existsByPlaca(String placa);

    Optional<VeiculoEntity> findByPlaca(String placa);
}
