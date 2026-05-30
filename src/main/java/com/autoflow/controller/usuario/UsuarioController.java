package com.autoflow.controller.usuario;

import com.autoflow.controller.usuario.request.LoginRequest;
import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.controller.usuario.response.LoginResponse;
import com.autoflow.controller.usuario.response.UsuarioResponse;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.service.usuario.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

}
