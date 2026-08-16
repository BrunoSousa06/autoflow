package com.autoflow.application.mapper;

import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.domain.usuario.Usuario;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface UsuarioApplicationMapper {
    List<UsuarioOutput> toOutput(List<Usuario> usuarios);

    UsuarioOutput toOutput(Usuario usuario);
}
