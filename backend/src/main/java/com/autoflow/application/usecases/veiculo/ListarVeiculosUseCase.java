package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.security.ClienteAutenticadoService;
import com.autoflow.infrastructure.persistence.mapper.VeiculoMapper;
import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import com.autoflow.infrastructure.persistence.repository.VeiculoSpecifications;
import com.autoflow.application.dto.veiculo.VeiculoFiltro;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarVeiculosUseCase {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoMapper veiculoMapper;
    private final ClienteAutenticadoService clienteAutenticadoService;

    public Page<VeiculoOutput> execute(VeiculoInput filtro,
                                       Pageable pageable) {

        Long clienteId = clienteAutenticadoService.getClienteId();

        VeiculoFiltro filtroEfetivo = new VeiculoFiltro(
                filtro.placa(),
                filtro.marca(),
                filtro.modelo(),
                filtro.ano(),
                clienteId
        );

        return veiculoRepository
                .findAll(
                        VeiculoSpecifications.comFiltros(filtroEfetivo),
                        pageable)
                .map(veiculoMapper::mapToOutput);
    }
}