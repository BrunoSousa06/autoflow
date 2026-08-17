package com.autoflow.presentation.usuario;

import com.autoflow.application.input.usuario.RegistroInput;
import com.autoflow.application.output.usuario.UsuarioOutput;
import com.autoflow.presentation.usuario.request.RegistroRequest;
import com.autoflow.presentation.usuario.response.UsuarioCadastroResponse;
import com.autoflow.presentation.usuario.response.UsuarioResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioControllerMapper {
    RegistroInput toInput(RegistroRequest request);

    UsuarioResponse toResponse(UsuarioOutput output);

    List<UsuarioResponse> toResponse(List<UsuarioOutput> outputs);

    UsuarioCadastroResponse toCadastroResponse(UsuarioOutput output);
}
