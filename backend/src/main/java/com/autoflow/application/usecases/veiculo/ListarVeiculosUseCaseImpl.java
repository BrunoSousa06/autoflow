package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.input.veiculo.*;
import com.autoflow.application.output.veiculo.*;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.port.in.veiculo.ListarVeiculosUseCase;
import com.autoflow.application.security.ClienteAutenticadoService;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ListarVeiculosUseCaseImpl implements ListarVeiculosUseCase {

    private final VeiculoGateway veiculoGateway;
    private final ClienteAutenticadoService clienteAutenticadoService;

    @Override
    public PageOutput<VeiculoOutput> execute(VeiculoInput filtro, PageInput page) {
        Long clienteId = clienteAutenticadoService.getClienteId().orElse(null);

        VeiculoFiltro filtroEfetivo = new VeiculoFiltro(
                filtro.placa(),
                filtro.marca(),
                filtro.modelo(),
                filtro.ano(),
                clienteId);

        return veiculoGateway.findAll(filtroEfetivo, page);
    }
}
