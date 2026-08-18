package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.output.ordemservico.OrdemServicoCriadaOutput;
import com.autoflow.application.input.veiculo.VeiculoInput;
import com.autoflow.domain.ordemservico.ServicoSolicitado;

import java.util.List;

public interface CriarOrdemServicoUseCase {
    OrdemServicoCriadaOutput execute(
            String cpfCnpj,
            VeiculoInput veiculoRequest,
            List<ServicoSolicitado> servicosSolicitados);
}
