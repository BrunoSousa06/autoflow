package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.PageInput;
import com.autoflow.application.dto.veiculo.PageOutput;
import com.autoflow.application.dto.veiculo.VeiculoFiltro;
import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.security.ClienteAutenticadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarVeiculosUseCase {

    private final VeiculoGateway veiculoGateway;
    private final ClienteAutenticadoService clienteAutenticadoService;

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
