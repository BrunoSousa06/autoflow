package com.autoflow.presentation.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.application.port.in.ordemservico.acompanhamento.AcompanharOrdemServicoUseCase;
import com.autoflow.presentation.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clientes/me/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "ordens de serviço do cliente", description = "Endpoints para gerenciamento das ordens de serviço do cliente autenticado")
@SecurityRequirement(name = "bearerAuth")
public class ClienteOrdemServicoController {

    private final AcompanharOrdemServicoUseCase acompanharOrdemServicoUseCase;
    private final AcompanhamentoControllerMapper acompanhamentoMapper;

    @Operation(summary = "Listar ordem de serviço do cliente", description = "Retorna as ordens de serviço do cliente autenticado")
    @ApiResponse(responseCode = "200", description = "Ordens de serviço encontradas com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente autenticado nao encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public List<AcompanhamentoOrdemServicoResponse> listarMinhasOS(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<AcompanhamentoOrdemServicoOutput> output =
                acompanharOrdemServicoUseCase.execute(userDetails.getUsername());
        return acompanhamentoMapper.toResponse(output);
    }
}
