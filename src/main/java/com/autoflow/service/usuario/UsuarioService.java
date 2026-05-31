package com.autoflow.service.usuario;

import com.autoflow.controller.usuario.request.LoginRequest;
import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.config.security.service.JwtService;
import com.autoflow.controller.usuario.response.UsuarioResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.mapper.UsuarioMapper;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;


    public UsuarioEntity cadastrar(RegistroRequest request) {

        UsuarioEntity usuarioEntity = usuarioRepository.save(usuarioMapper.mapToEntity(request));

        if (RoleEnum.CLIENTE.equals(request.role())){

            ClienteEntity clienteEntity = usuarioMapper.mapToClienteEntity(request);
            clienteEntity.setUsuario(usuarioEntity);
            clienteRepository.save(clienteEntity);
        }
        return usuarioEntity;
    }


    public String login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.senha()
                )
        );

        UsuarioEntity usuarioEntity = usuarioRepository
                .findByEmail(request.email())
                .orElseThrow();

        return  jwtService.gerarToken(
                usuarioEntity.getEmail(),
                usuarioEntity.getRole().name()
        );
    }

    public UsuarioEntity buscarMecanicoPorId(Long mecanicoId){
        UsuarioEntity usuario = usuarioRepository.findById(mecanicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mecânico não encontrado."
                ));
        if (!RoleEnum.MECANICO.equals(usuario.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Usuário informado não é um mecânico."
            );
        }
        return usuario;
    }

    public UsuarioEntity buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário autenticado não encontrado."
                ));
    }

    public List<UsuarioResponse> listarUsuarios() {
        List<UsuarioEntity> usuarios = usuarioRepository.findAll();
        usuarioMapper.mapToResponse(usuarios);

        return usuarioMapper.mapToResponse(usuarios);
    }
}
