package com.autoflow.controller.ordemservico.reparoadicional;

import com.autoflow.controller.ordemservico.reparoadicional.request.CriarReparoAdicionalRequest;
import com.autoflow.controller.ordemservico.reparoadicional.response.CriarReparoAdicionalResponse;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.mapper.ItensNecessariosMapper;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens-servico/{ordemServicoId}/reparos-adicionais")
@RequiredArgsConstructor
public class ReparoAdicionalController {

    private final ReparoAdicionalService reparoAdicionalService;
    private final ItensNecessariosMapper itensNecessariosMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MECANICO', 'ADMIN')")
    public CriarReparoAdicionalResponse criar(
            @PathVariable Long ordemServicoId,
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
                ordemServicoId,
                userDetails.getUsername(),
                servicos
        ));
    }
}
