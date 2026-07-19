package com.autoflow.infrastructure.persistence.mapper;


import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.presentation.usuario.response.UsuarioCadastroResponse;
import com.autoflow.presentation.usuario.request.RegistroRequest;
import com.autoflow.presentation.usuario.response.UsuarioResponse;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.domain.usuario.UsuarioEntity;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = PasswordMapper.class)
public interface UsuarioMapper {

    @Mapping(target = "senha", source = "senha", qualifiedByName = "encodePassword")
    UsuarioEntity mapToEntity(RegistroInput request);

    ClienteEntity mapToClienteEntity(RegistroInput request);

    UsuarioResponse mapToResponse(UsuarioEntity usuario);

    List<UsuarioOutput> mapToOutput(List<UsuarioEntity> usuarios);

    UsuarioOutput mapToOutput(UsuarioEntity usuario);

    UsuarioResponse mapToResponse(UsuarioOutput execute);

    UsuarioCadastroResponse mapToCadastroResponse(UsuarioOutput execute);

    List<UsuarioResponse> mapEntityToResponse(List<UsuarioEntity> todosUsuarios);

    List<UsuarioResponse> mapToResponse(List<UsuarioOutput> todosUsuarios);

    RegistroInput mapToInput(RegistroRequest request);
}
