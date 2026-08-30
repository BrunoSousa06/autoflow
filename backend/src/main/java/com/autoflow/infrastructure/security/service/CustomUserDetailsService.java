package com.autoflow.infrastructure.security.service;

import com.autoflow.infrastructure.persistence.entity.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        UsuarioEntity usuarioEntity = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado"));

        return new User(
                usuarioEntity.getEmail(),
                usuarioEntity.getSenha(),
                List.of(new SimpleGrantedAuthority("ROLE_" + usuarioEntity.getRole().name()))
        );
    }
}
