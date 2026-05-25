package com.autoflow.controller.ordemServico;

import com.autoflow.service.ordemServico.OrdemServicoService;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.controller.ordemServico.request.CriarOrdemServicoRequest;
import com.autoflow.controller.ordemServico.response.OrdemServicoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ordens-servico")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdemServicoResponse criar(@Valid @RequestBody CriarOrdemServicoRequest request) {
        List<ServicoSolicitadoEntity> servicos = request.servicosSolicitados()
                .stream()
                .map(servico -> new ServicoSolicitadoEntity(
                        servico.servicoId(), servico.nome()
                ))
                .toList();

        OrdemServicoEntity ordemServicoEntity = ordemServicoService.criar(
                request.clienteId(),
                request.veiculoId(),
                servicos
        );

        return OrdemServicoResponse.fromDomain(ordemServicoEntity);
    }
}
