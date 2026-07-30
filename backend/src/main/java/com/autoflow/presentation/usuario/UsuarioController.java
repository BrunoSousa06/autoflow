package com.autoflow.presentation.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.usecases.usuario.*;
import com.autoflow.infrastructure.persistence.mapper.UsuarioMapper;
import com.autoflow.presentation.usuario.request.LoginRequest;
import com.autoflow.presentation.usuario.request.RegistroRequest;
import com.autoflow.presentation.usuario.response.LoginResponse;
import com.autoflow.presentation.usuario.response.UsuarioCadastroResponse;
import com.autoflow.presentation.usuario.response.UsuarioResponse;
import com.autoflow.domain.usuario.RoleEnum;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "autenticação", description = "Endpoints para gerenciamento de autenticação dos usuarios")
public class UsuarioController {

    private final CadastrarUsuarioUseCase cadastrarUsuarioUseCase;
    private final LoginUsuarioUseCase loginUsuarioUseCase;
    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final BuscarMecanicosUseCase buscarMecanicosUseCase;
    private final UsuarioMapper usuarioMapper;



    @Operation(summary = "Cadastrar um usuario", description = "Retorna as informações do usuario cadastrado")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
    @ApiResponse(responseCode = "409", description = "CPF/CNPJ ou email ja foram cadastrados")
    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioCadastroResponse> cadastrar(@Valid @RequestBody RegistroRequest request) {
        if (!RoleEnum.CLIENTE.equals(request.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cadastro público permite apenas a role CLIENTE");
        }
        RegistroInput input = usuarioMapper.mapToInput(request);
        UsuarioOutput execute = cadastrarUsuarioUseCase.execute(input);

        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioMapper.mapToCadastroResponse(execute));
    }

    @Operation(summary = "Autenticar o usuario", description = "Autentica o usuario cadastrado")
    @ApiResponse(responseCode = "200", description = "Token retornado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
       return new LoginResponse(loginUsuarioUseCase.execute(request));

    }

    @Operation(summary = "Listar todos usuarios cadastrados no sistema", description = "Retorna a lista dos usuarios cadastrados")
    @ApiResponse(responseCode = "200", description = "Usuarios retornados com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        List<UsuarioOutput> todosUsuarios = listarUsuariosUseCase.execute();
        return ResponseEntity.ok(usuarioMapper.mapToResponse(todosUsuarios));
    }

    @Operation(summary = "Listar todos mecanicos cadastrados no sistema", description = "Retorna a lista dos mecanicos cadastrados")
    @ApiResponse(responseCode = "200", description = "Mecanicos retornados com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/mecanicos")
    @PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
    public ResponseEntity<List<UsuarioResponse>> listarMecanicos() {
        List<UsuarioOutput> todosMecanicos = buscarMecanicosUseCase.execute();
        return ResponseEntity.ok(usuarioMapper.mapToResponse(todosMecanicos));
        }
}
