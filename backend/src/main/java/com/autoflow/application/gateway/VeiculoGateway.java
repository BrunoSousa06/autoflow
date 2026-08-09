package com.autoflow.application.gateway;

import com.autoflow.application.dto.veiculo.*;

import java.util.Optional;

public interface VeiculoGateway {

    VeiculoOutput save(CadastrarVeiculoInput input, Long clienteId);

    VeiculoOutput save(VeiculoOrdemServicoInput input, Long clienteId);

    VeiculoOutput update(Long id, VeiculoInput input);

    Optional<VeiculoOutput> findById(Long id);

    Optional<VeiculoOutput> findByPlaca(String placa);

    boolean existsByPlaca(String placa);

    PageOutput<VeiculoOutput> findAll(VeiculoFiltro filtro, PageInput page);

    boolean existsById(Long id);

    void deleteById(Long id);
}
