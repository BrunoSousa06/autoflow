package com.autoflow.controller.usuario;

import com.autoflow.controller.usuario.request.LoginRequest;
import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.controller.usuario.response.LoginResponse;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.service.usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioEntity> cadastrar(@RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrar(request));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
       return new LoginResponse(usuarioService.login(request));

    }
}
