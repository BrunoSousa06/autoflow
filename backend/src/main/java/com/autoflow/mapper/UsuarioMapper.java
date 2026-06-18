package com.autoflow.mapper;


import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.controller.usuario.response.UsuarioResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = PasswordMapper.class)
public interface UsuarioMapper {

    @Mapping(target = "senha", source = "senha", qualifiedByName = "encodePassword")
    UsuarioEntity mapToEntity(RegistroRequest request);

    ClienteEntity mapToClienteEntity(RegistroRequest request);

    UsuarioResponse mapToResponse(UsuarioEntity usuario);

    List<UsuarioResponse> mapToResponse(List<UsuarioEntity> usuarios);
}
