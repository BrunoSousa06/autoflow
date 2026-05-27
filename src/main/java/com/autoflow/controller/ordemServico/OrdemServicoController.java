package com.autoflow.controller.ordemServico;

import com.autoflow.controller.ordemServico.request.CriarOrdemServicoRequest;
import com.autoflow.controller.ordemServico.request.IncluirOrdemServicoRequest;
import com.autoflow.controller.ordemServico.response.OrdemServicoResponse;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.mapper.ServicoSolicitadoMapper;
import com.autoflow.service.ordemServico.OrdemServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens-servico")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;
    private final ServicoSolicitadoMapper servicoSolicitadoMapper;

    public OrdemServicoController(OrdemServicoService ordemServicoService, ServicoSolicitadoMapper servicoSolicitadoMapper) {
        this.ordemServicoService = ordemServicoService;
        this.servicoSolicitadoMapper = servicoSolicitadoMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdemServicoResponse criar(@Valid @RequestBody CriarOrdemServicoRequest request) {
        List<ServicoSolicitadoEntity> servicos = servicoSolicitadoMapper.mapToEntities(request.servicosSolicitados());

        OrdemServicoEntity ordemServicoEntity = ordemServicoService.criar(
                request.clienteId(),
                request.veiculoId(),
                servicos
        );

        return OrdemServicoResponse.fromDomain(ordemServicoEntity);
    }

    @PostMapping("/{ordemServicoId}/servicos")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrdemServicoResponse incluirServico(
            @PathVariable Long ordemServicoId,
            @Valid @RequestBody IncluirOrdemServicoRequest request
    ) {
        List<ServicoSolicitadoEntity> servicos = servicoSolicitadoMapper.mapToEntities(request.servicosSolicitados());
        OrdemServicoEntity ordemServicoEntity = ordemServicoService.incluirServicos(ordemServicoId, servicos);
        return OrdemServicoResponse.fromDomain(ordemServicoEntity);
    }

}
