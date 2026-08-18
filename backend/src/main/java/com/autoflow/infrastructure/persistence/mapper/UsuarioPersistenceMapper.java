package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.domain.usuario.Usuario;
import com.autoflow.infrastructure.persistence.entity.usuario.UsuarioEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioPersistenceMapper {

    Usuario toDomain(UsuarioEntity entity);

    UsuarioEntity toEntity(Usuario usuario);
}
