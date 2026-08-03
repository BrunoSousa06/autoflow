package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.mapper.UsuarioMapper;
import com.autoflow.infrastructure.persistence.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CadastrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final CadastrarClienteUseCase cadastrarClienteUseCase;

    @Transactional
    public UsuarioOutput execute(RegistroInput request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email já cadastrado");
        }

        UsuarioEntity usuario = usuarioRepository.save(
                usuarioMapper.mapToEntity(request)
        );

        if (RoleEnum.CLIENTE.equals(request.role())) {
            cadastrarClienteUseCase.execute(request, usuario);
        }

        return usuarioMapper.mapToOutput(usuario);
    }
}