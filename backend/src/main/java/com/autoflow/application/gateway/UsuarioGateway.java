package com.autoflow.application.gateway;

import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;

import java.util.List;
import java.util.Optional;

public interface UsuarioGateway {

    List<UsuarioEntity> findAll();

    List<UsuarioEntity> findByRole(RoleEnum roleEnum);
    Optional<UsuarioEntity> findById(Long mecanicoId);

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    UsuarioEntity save(UsuarioEntity usuario);

}
