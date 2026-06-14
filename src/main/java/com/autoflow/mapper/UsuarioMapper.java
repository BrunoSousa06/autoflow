package com.autoflow.mapper;


import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.controller.usuario.response.UsuarioResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class UsuarioMapper {

    @Autowired
    PasswordEncoder passwordEncoder;


    @Mapping(target = "senha", expression = "java(passwordEncoder.encode(request.senha()))")
    public abstract UsuarioEntity mapToEntity(RegistroRequest request);

    public abstract ClienteEntity mapToClienteEntity(RegistroRequest request);

    public abstract List<UsuarioResponse> mapToResponse(List<UsuarioEntity> usuarios);
}
