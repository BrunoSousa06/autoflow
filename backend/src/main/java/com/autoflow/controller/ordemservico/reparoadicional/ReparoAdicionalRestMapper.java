package com.autoflow.controller.ordemservico.reparoadicional;

import com.autoflow.application.dto.ordemservico.reparoadicional.CriarReparoAdicionalCommand;
import com.autoflow.application.dto.ordemservico.reparoadicional.CriarReparoAdicionalOutput;
import com.autoflow.application.dto.ordemservico.reparoadicional.ItemReparoAdicionalCommand;
import com.autoflow.application.dto.ordemservico.reparoadicional.ServicoReparoAdicionalCommand;
import com.autoflow.controller.ordemservico.reparoadicional.request.CriarReparoAdicionalRequest;
import com.autoflow.controller.ordemservico.reparoadicional.request.ServicoReparoAdicionalRequest;
import com.autoflow.controller.ordemservico.reparoadicional.response.CriarReparoAdicionalResponse;
import org.springframework.stereotype.Component;

@Component
public class ReparoAdicionalRestMapper {

    public CriarReparoAdicionalCommand toCommand(
            String numeroOs,
            String emailMecanico,
            CriarReparoAdicionalRequest request
    ) {
        return new CriarReparoAdicionalCommand(
                numeroOs,
                emailMecanico,
                request.servicos().stream().map(this::toCommand).toList()
        );
    }

    public CriarReparoAdicionalResponse toResponse(CriarReparoAdicionalOutput output) {
        return new CriarReparoAdicionalResponse(
                output.reparoAdicionalId(),
                output.orcamentoId(),
                output.publicUrl()
        );
    }

    private ServicoReparoAdicionalCommand toCommand(ServicoReparoAdicionalRequest request) {
        return new ServicoReparoAdicionalCommand(
                request.servicoId(),
                request.itensNecessarios().stream()
                        .map(item -> new ItemReparoAdicionalCommand(item.pecaInsumoId(), item.quantidade()))
                        .toList()
        );
    }
}
