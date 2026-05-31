package com.autoflow.controller.ordemServico;

import com.autoflow.controller.ordemServico.request.ItensNecessariosRequest;
import com.autoflow.controller.ordemServico.request.CriarOrdemServicoRequest;
import com.autoflow.controller.ordemServico.request.IncluirMecanicoRequest;
import com.autoflow.controller.ordemServico.request.ServicoSolicitadoRequest;
import com.autoflow.controller.ordemServico.response.OrdemServicoResponse;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.mapper.ItensNecessariosMapper;
import com.autoflow.mapper.ServicoSolicitadoMapper;
import com.autoflow.service.ordemServico.OrdemServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens-servico")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;
    private final ServicoSolicitadoMapper servicoSolicitadoMapper;
    private final ItensNecessariosMapper itensNecessariosMapper;

    public OrdemServicoController(OrdemServicoService ordemServicoService, ServicoSolicitadoMapper servicoSolicitadoMapper, ItensNecessariosMapper itensNecessariosMapper) {
        this.ordemServicoService = ordemServicoService;
        this.servicoSolicitadoMapper = servicoSolicitadoMapper;
        this.itensNecessariosMapper = itensNecessariosMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public OrdemServicoResponse criar(@Valid @RequestBody CriarOrdemServicoRequest request) {
        List<ServicoSolicitadoEntity> servicos = servicoSolicitadoMapper.mapToEntities(request.servicosSolicitados());
        return OrdemServicoResponse.fromDomain(ordemServicoService.criar(
                request.clienteId(),
                request.veiculoId(),
                servicos
        ));
    }

    @PostMapping("/{ordemServicoId}/servicos")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public OrdemServicoResponse incluirServico(
            @PathVariable Long ordemServicoId,
            @Valid @RequestBody List<ServicoSolicitadoRequest> request
    ) {
        List<ServicoSolicitadoEntity> servicos = servicoSolicitadoMapper.mapToEntities(request);
        return OrdemServicoResponse.fromDomain(ordemServicoService.incluirServicos(ordemServicoId, servicos));
    }

    @PatchMapping("/{ordemServicoId}/mecanico")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public OrdemServicoResponse atribuirMecanico(
            @PathVariable Long ordemServicoId,
            @Valid @RequestBody IncluirMecanicoRequest request){
        return OrdemServicoResponse.fromDomain(ordemServicoService.atribuirMecanico(
                ordemServicoId,
                request.mecanicoId()));
    }

    @PatchMapping("/{ordemServicoId}/diagnostico/iniciar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse iniciarDiagnostico(@PathVariable Long ordemServicoId, @AuthenticationPrincipal UserDetails userDetails){
        return OrdemServicoResponse.fromDomain(ordemServicoService.iniciarDiagnostico(
                ordemServicoId,
                userDetails.getUsername()));
    }

    @PatchMapping("/{ordemServicoId}/itens-necessarios")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse atualizarDiagnostico(
            @PathVariable Long ordemServicoId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody List<ItensNecessariosRequest> itensNecessariosRequests
    ){
        return OrdemServicoResponse.fromDomain(ordemServicoService.registrarItemNecessario(
                ordemServicoId,
                userDetails.getUsername(),
                itensNecessariosMapper.mapToEntities(itensNecessariosRequests)
        ));
    }

}
