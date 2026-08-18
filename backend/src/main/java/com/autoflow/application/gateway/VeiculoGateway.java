package com.autoflow.application.gateway;

import com.autoflow.application.input.veiculo.CadastrarVeiculoCommand;
import com.autoflow.application.input.veiculo.PageInput;
import com.autoflow.application.input.veiculo.VeiculoFiltro;
import com.autoflow.application.input.veiculo.VeiculoInput;
import com.autoflow.application.output.veiculo.PageOutput;
import com.autoflow.application.output.veiculo.VeiculoOutput;

import java.util.Optional;

public interface VeiculoGateway {

    VeiculoOutput save(CadastrarVeiculoCommand input, Long clienteId);

    VeiculoOutput update(Long id, VeiculoInput input);

    Optional<VeiculoOutput> findById(Long id);

    Optional<VeiculoOutput> findByPlaca(String placa);

    boolean existsByPlaca(String placa);

    PageOutput<VeiculoOutput> findAll(VeiculoFiltro filtro, PageInput page);

    boolean existsById(Long id);

    void deleteById(Long id);
}
