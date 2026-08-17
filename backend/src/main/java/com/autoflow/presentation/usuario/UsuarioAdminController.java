package com.autoflow.presentation.usuario;

import com.autoflow.application.input.usuario.RegistroInput;
import com.autoflow.application.output.usuario.UsuarioOutput;
import com.autoflow.application.port.in.usuario.CadastrarStaffUseCase;
import com.autoflow.application.port.in.usuario.ListarUsuariosUseCase;
import com.autoflow.presentation.usuario.request.RegistroRequest;
import com.autoflow.presentation.usuario.response.UsuarioResponse;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "usuarios", description = "Endpoints para gerenciamento de usuários do sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioAdminController {

    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final CadastrarStaffUseCase cadastrarComoStaff;
    private final UsuarioControllerMapper usuarioMapper;

    @Operation(summary = "Listar todos os usuários", description = "Retorna a lista de todos os usuários cadastrados no sistema")
    @ApiResponse(responseCode = "200", description = "Usuários retornados com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
    public ResponseEntity<List<UsuarioResponse>> listar() {

        List<UsuarioOutput> todosUsuarios = listarUsuariosUseCase.execute();

        return ResponseEntity.ok(usuarioMapper.toResponse(todosUsuarios));
    }

    @Operation(summary = "Cadastrar usuário pelo staff", description = "Cria um novo usuário respeitando as restrições de role do criador. ATENDENTE só pode criar ATENDENTE ou CLIENTE.")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
    @ApiResponse(responseCode = "403", description = "Atendente não pode criar ADMIN ou MECANICO")
    @ApiResponse(responseCode = "409", description = "Email ou CPF/CNPJ já cadastrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
    public ResponseEntity<UsuarioResponse> cadastrarComoStaff(
            @Valid @RequestBody RegistroRequest request) {
        RegistroInput input = usuarioMapper.toInput(request);
        UsuarioOutput execute = cadastrarComoStaff.execute(input);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioMapper.toResponse(execute));
    }
}
