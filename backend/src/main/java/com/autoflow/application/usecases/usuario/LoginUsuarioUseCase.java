package com.autoflow.application.usecases.usuario;

import com.autoflow.presentation.usuario.request.LoginRequest;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.security.service.JwtService;
import com.autoflow.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUsuarioUseCase {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public String execute(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.senha()
                )
        );

        UsuarioEntity usuario = usuarioRepository
                .findByEmail(request.email())
                .orElseThrow();

        return jwtService.gerarToken(
                usuario.getEmail(),
                usuario.getRole().name()
        );
    }
}
