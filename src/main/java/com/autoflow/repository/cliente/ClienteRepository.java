package com.autoflow.repository.cliente;


import com.autoflow.domain.cliente.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long>{
    Optional<ClienteEntity> findByCpfCnpj(Long cpfCnpj);
}