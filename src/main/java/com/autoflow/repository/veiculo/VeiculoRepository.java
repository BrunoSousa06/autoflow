package com.autoflow.repository.veiculo;

import com.autoflow.domain.veiculo.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VeiculoRepository extends JpaRepository<VeiculoEntity, Long> {
    boolean existsByPlaca(String placa);
}
