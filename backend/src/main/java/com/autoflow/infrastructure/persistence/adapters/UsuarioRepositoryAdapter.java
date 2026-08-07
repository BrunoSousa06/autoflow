package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioGateway {

    private final UsuarioRepository usuarioRepository;

    @Override
    public List<UsuarioEntity> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public List<UsuarioEntity> findByRole(RoleEnum roleEnum) {
        return usuarioRepository.findByRole(roleEnum);
    }

    @Override
    public Optional<UsuarioEntity> findById(Long mecanicoId) {
        return usuarioRepository.findById(mecanicoId);
    }

    @Override
    public Optional<UsuarioEntity> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public UsuarioEntity save(UsuarioEntity usuario) {
        return usuarioRepository.save(usuario);
    }
}
