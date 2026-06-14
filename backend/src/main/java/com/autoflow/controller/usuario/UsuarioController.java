package com.autoflow.controller.usuario;

import com.autoflow.controller.usuario.request.LoginRequest;
import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.controller.usuario.response.LoginResponse;
import com.autoflow.controller.usuario.response.UsuarioResponse;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.service.usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "autenticação", description = "Endpoints para gerenciamento de autenticação dos usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Cadastrar um usuario", description = "Retorna as informações do usuario cadastrado")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
    @ApiResponse(responseCode = "409", description = "CPF/CNPJ ou email ja foram cadastrados")
    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioEntity> cadastrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrar(request));
    }

    @Operation(summary = "Autenticar o usuario", description = "Autentica o usuario cadastrado")
    @ApiResponse(responseCode = "200", description = "Token retornado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
       return new LoginResponse(usuarioService.login(request));

    }

    @Operation(summary = "Listar todos usuarios cadastrados no sistema", description = "Retorna a lista dos usuarios cadastrados")
    @ApiResponse(responseCode = "200", description = "Usuarios retornados com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @Operation(summary = "Listar todos mecanicos cadastrados no sistema", description = "Retorna a lista dos mecanicos cadastrados")
    @ApiResponse(responseCode = "200", description = "Mecanicos retornados com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/mecanicos")
    @PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
    public ResponseEntity<List<UsuarioResponse>> listarMecanicos() {
        return ResponseEntity.ok(usuarioService.buscarMecanicos());
    }


}
