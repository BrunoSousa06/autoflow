package com.autoflow.controller.ordemservico.reparoadicional;

import com.autoflow.controller.ordemservico.reparoadicional.request.CriarReparoAdicionalRequest;
import com.autoflow.controller.ordemservico.reparoadicional.response.CriarReparoAdicionalResponse;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.mapper.ItensNecessariosMapper;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens-servico/{numeroOs}/reparos-adicionais")
@RequiredArgsConstructor
@Tag(name = "reparos adicionais", description = "Endpoints para gerenciamento dos reparos adicionais da ordem de serviço")
public class ReparoAdicionalController {

    private final ReparoAdicionalService reparoAdicionalService;
    private final ItensNecessariosMapper itensNecessariosMapper;


    @Operation(summary = "Criar os reparos adicionais", description = "Cria os reparos adicionais da ordem de serviço")
    @ApiResponse(responseCode = "200", description = "Reparo adicional criado com sucesso")
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
        List<ServicoSolicitadoEntity> servicos = request.servicos()
                .stream()
                .map(servicoRequest -> {
                    ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity();
                    servico.setServicoId(servicoRequest.servicoId());
                    servico.registrarItensNecessarios(
                            itensNecessariosMapper.mapToEntities(servicoRequest.itensNecessarios())
                    );
                    return servico;
                })
                .toList();

        return CriarReparoAdicionalResponse.from(reparoAdicionalService.criar(
                numeroOs,
                userDetails.getUsername(),
                servicos
        ));
    }
}
