package com.autoflow.presentation.ordemservico;

import com.autoflow.application.port.in.ordemservico.EntregarOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.FinalizarServicoUseCase;
import com.autoflow.application.port.in.ordemservico.IniciarServicoUseCase;
import com.autoflow.presentation.ordemservico.response.OrdemServicoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ordens-servico")
public class OrdemServicoExecucaoController {

    private final IniciarServicoUseCase iniciarServico;
    private final FinalizarServicoUseCase finalizarServico;
    private final EntregarOrdemServicoUseCase entregar;

    public OrdemServicoExecucaoController(
            IniciarServicoUseCase iniciarServico,
            FinalizarServicoUseCase finalizarServico,
            EntregarOrdemServicoUseCase entregar) {
        this.iniciarServico = iniciarServico;
        this.finalizarServico = finalizarServico;
        this.entregar = entregar;
    }

    @PatchMapping("/{numeroOs}/servicos/{servicoId}/iniciar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse iniciar(@PathVariable String numeroOs, @PathVariable Long servicoId) {
        return OrdemServicoResponse.fromDomain(iniciarServico.execute(numeroOs, servicoId));
    }

    @PatchMapping("/{numeroOs}/servicos/{servicoId}/finalizar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse finalizar(@PathVariable String numeroOs, @PathVariable Long servicoId) {
        return OrdemServicoResponse.fromDomain(finalizarServico.execute(numeroOs, servicoId));
    }

    @PatchMapping("/{numeroOs}/entregar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public OrdemServicoResponse entregar(@PathVariable String numeroOs) {
        return OrdemServicoResponse.fromDomain(entregar.execute(numeroOs));
    }
}
