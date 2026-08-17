package com.autoflow.application.port.in.veiculo;

import com.autoflow.application.dto.veiculo.PageInput;
import com.autoflow.application.dto.veiculo.PageOutput;
import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;

public interface ListarVeiculosUseCase {
    PageOutput<VeiculoOutput> execute(VeiculoInput filtro, PageInput page);
}
