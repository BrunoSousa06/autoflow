package com.autoflow.controller.ordemservico;

import com.autoflow.controller.ordemservico.request.*;
import com.autoflow.controller.ordemservico.response.OrdemServicoResponse;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.mapper.ItensNecessariosMapper;
import com.autoflow.mapper.ServicoSolicitadoMapper;
import com.autoflow.service.ordemservico.OrdemServicoService;
import com.autoflow.service.ordemservico.impl.OrdemServicoServiceImpl;
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

    public OrdemServicoController(OrdemServicoServiceImpl ordemServicoService,
                                  ServicoSolicitadoMapper servicoSolicitadoMapper,
                                  ItensNecessariosMapper itensNecessariosMapper) {
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

    @PatchMapping("/{ordemServicoId}/servicos/{servicoOsId}/itens-necessarios")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse registrarItensDoServico(
            @PathVariable Long ordemServicoId,
            @PathVariable Long servicoOsId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody List<ItensNecessariosRequest> request
    ) {
        return OrdemServicoResponse.fromDomain(
                ordemServicoService.registrarItemNecessario(
                        ordemServicoId,
                        servicoOsId,
                        userDetails.getUsername(),
                        itensNecessariosMapper.mapToEntities(request)
                )
        );
    }

    @PatchMapping("/{ordemServicoId}/diagnostico/laudo")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse atualizarDiagnostico(
            @PathVariable Long ordemServicoId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegistrarLaudoRequest request
    ){
        return OrdemServicoResponse.fromDomain(ordemServicoService.registrarLaudo(
                ordemServicoId,
                userDetails.getUsername(),
                request.laudo()
        ));
    }

    @PatchMapping("/{ordemServicoId}/diagnostico/finalizar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public FinalizarDiagnosticoResponse finalizarDiagnostico(
            @PathVariable Long ordemServicoId,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        return FinalizarDiagnosticoResponse.from(ordemServicoService.finalizarDiagnostico(
                ordemServicoId,
                userDetails.getUsername()
        ));
    }

    @PatchMapping("/{ordemServicoId}/servicos/{servicoOsId}/iniciar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO', 'ATENDENTE')")
    public OrdemServicoResponse iniciarServico(
            @PathVariable Long ordemServicoId,
            @PathVariable Long servicoOsId
    ) {
        return OrdemServicoResponse.fromDomain(
                ordemServicoService.iniciarServico(ordemServicoId, servicoOsId)
        );
    }

    @PatchMapping("/{ordemServicoId}/servicos/{servicoOsId}/finalizar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse finalizarServico(
            @PathVariable Long ordemServicoId,
            @PathVariable Long servicoOsId
    ) {
        return OrdemServicoResponse.fromDomain(
                ordemServicoService.finalizarServico(ordemServicoId, servicoOsId)
        );
    }

}
