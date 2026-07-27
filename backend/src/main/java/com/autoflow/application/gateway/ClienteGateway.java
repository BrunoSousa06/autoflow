package com.autoflow.application.gateway;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;

import java.util.Optional;
import java.util.List;


public interface ClienteGateway {

    ClienteEntity save(ClienteEntity cliente);

    Optional<ClienteEntity> findById(Long id);


    Optional<ClienteEntity> findByCpfCnpj(String cpfCnpj);

    boolean existsByCpfCnpj(String cpfCnpj);


    Optional<ClienteEntity> findByUsuarioEmail(String usuarioEmail);


    List<ClienteEntity> findAll();


    void deleteById(Long id);


    boolean existsById(Long id);
}