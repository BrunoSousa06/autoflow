package com.autoflow.controller.ordemservico.reparoadicional;

import com.autoflow.application.usecases.ordemservico.reparoadicional.CriarReparoAdicionalUseCase;
import com.autoflow.controller.ordemservico.reparoadicional.request.CriarReparoAdicionalRequest;
import com.autoflow.controller.ordemservico.reparoadicional.response.CriarReparoAdicionalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ordens-servico/{numeroOs}/reparos-adicionais")
@RequiredArgsConstructor
@Tag(name = "reparos adicionais", description = "Endpoints para gerenciamento dos reparos adicionais da ordem de serviço")
@SecurityRequirement(name = "bearerAuth")
public class ReparoAdicionalController {

    private final CriarReparoAdicionalUseCase criarReparoAdicionalUseCase;
    private final ReparoAdicionalRestMapper reparoAdicionalRestMapper;

    @Operation(summary = "Criar os reparos adicionais", description = "Cria os reparos adicionais da ordem de serviço")
    @ApiResponse(responseCode = "201", description = "Reparo adicional criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados obrigatórios não informados ou inválidos")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MECANICO', 'ADMIN')")
    public CriarReparoAdicionalResponse criar(
            @PathVariable String numeroOs,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CriarReparoAdicionalRequest request
    ) {
        var command = reparoAdicionalRestMapper.toCommand(
                numeroOs,
                userDetails.getUsername(),
                request
        );

        return reparoAdicionalRestMapper.toResponse(criarReparoAdicionalUseCase.execute(command));
    }
}
