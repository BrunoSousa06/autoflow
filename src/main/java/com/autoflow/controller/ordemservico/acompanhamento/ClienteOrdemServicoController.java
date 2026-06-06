package com.autoflow.controller.ordemservico.acompanhamento;

import com.autoflow.controller.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import com.autoflow.service.ordemservico.OrdemServicoService;
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
public class ClienteOrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE')")
    public List<AcompanhamentoOrdemServicoResponse> listarMinhasOS(
            @AuthenticationPrincipal UserDetails userDetails
            ){
        return ordemServicoService.listarAcompanhamentoCliente(userDetails.getUsername());
    }
}
