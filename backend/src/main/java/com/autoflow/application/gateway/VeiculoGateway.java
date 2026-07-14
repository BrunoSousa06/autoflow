package com.autoflow.application.gateway;

import com.autoflow.domain.veiculo.VeiculoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Gateway interface defining the repository contract for Veiculo domain.
 * This ensures the domain layer is independent from infrastructure details.
 * All database operations must go through this gateway.
 */
public interface VeiculoGateway {

    /**
     * Save a vehicle (create or update)
     */
    VeiculoEntity save(VeiculoEntity veiculo);

    /**
     * Find a vehicle by ID
     */
    Optional<VeiculoEntity> findById(Long id);

    /**
     * Find a vehicle by plate number
     */
    Optional<VeiculoEntity> findByPlaca(String placa);

    /**
     * Check if a plate already exists
     */
    boolean existsByPlaca(String placa);

    /**
     * Check if a vehicle exists by ID
     */
    boolean existsById(Long id);

    /**
     * Find all vehicles for a specific client with pagination
     */
    Page<VeiculoEntity> findAllByClienteId(Long clienteId, Pageable pageable);

    /**
     * Delete a vehicle by ID
     */
    void deleteById(Long id);

}
