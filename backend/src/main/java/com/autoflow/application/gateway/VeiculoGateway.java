package com.autoflow.application.gateway;

import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface VeiculoGateway {


    VeiculoEntity save(VeiculoEntity veiculo);

    Optional<VeiculoEntity> findById(Long id);

    Optional<VeiculoEntity> findByPlaca(String placa);

    boolean existsByPlaca(String placa);

    boolean existsById(Long id);

    Page<VeiculoEntity> findAllByClienteId(Long clienteId, Pageable pageable);

    void deleteById(Long id);

}
