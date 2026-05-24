package com.autoflow.ordemServico.web;

import com.autoflow.ordemServico.application.OrdemServicoService;
import com.autoflow.ordemServico.domain.OrdemServico;
import com.autoflow.ordemServico.domain.ServicoSolicitado;
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
        List<ServicoSolicitado> servicos = request.servicosSolicitados()
                .stream()
                .map(servico -> new ServicoSolicitado(servico.servicoId(), servico.nome()))
                .toList();

        OrdemServico ordemServico = ordemServicoService.criar(
                request.clienteId(),
                request.veiculoId(),
                servicos
        );

        return OrdemServicoResponse.fromDomain(ordemServico);
    }
}
