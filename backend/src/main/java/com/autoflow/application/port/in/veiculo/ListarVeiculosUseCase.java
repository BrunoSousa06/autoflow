package com.autoflow.application.port.in.veiculo;

import com.autoflow.application.input.veiculo.PageInput;
import com.autoflow.application.input.veiculo.VeiculoInput;
import com.autoflow.application.output.veiculo.PageOutput;
import com.autoflow.application.output.veiculo.VeiculoOutput;

public interface ListarVeiculosUseCase {
    PageOutput<VeiculoOutput> execute(VeiculoInput filtro, PageInput page);
}
