package com.autoflow.controller.usuario;

import com.autoflow.controller.usuario.request.RegistroRequest;
import com.autoflow.controller.usuario.response.UsuarioResponse;
import com.autoflow.domain.usuario.RoleEnum;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "usuarios", description = "Endpoints para gerenciamento de usuários do sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioAdminController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Listar todos os usuários", description = "Retorna a lista de todos os usuários cadastrados no sistema")
    @ApiResponse(responseCode = "200", description = "Usuários retornados com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @Operation(summary = "Cadastrar usuário pelo staff", description = "Cria um novo usuário respeitando as restrições de role do criador. ATENDENTE só pode criar ATENDENTE ou CLIENTE.")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso")
    @ApiResponse(responseCode = "403", description = "Atendente não pode criar ADMIN ou MECANICO")
    @ApiResponse(responseCode = "409", description = "Email ou CPF/CNPJ já cadastrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ATENDENTE')")
    public ResponseEntity<UsuarioResponse> cadastrarComoStaff(
            @Valid @RequestBody RegistroRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String roleName = userDetails.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");
        RoleEnum callerRole = RoleEnum.valueOf(roleName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.cadastrarComoStaff(request, callerRole));
    }
}
