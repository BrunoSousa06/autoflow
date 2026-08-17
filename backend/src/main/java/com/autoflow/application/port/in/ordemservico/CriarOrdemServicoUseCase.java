package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.dto.ordemservico.OrdemServicoCriadaOutput;
import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.domain.ordemservico.ServicoSolicitado;

import java.util.List;

public interface CriarOrdemServicoUseCase {
    OrdemServicoCriadaOutput execute(
            String cpfCnpj,
            VeiculoOrdemServicoInput veiculoRequest,
            List<ServicoSolicitado> servicosSolicitados);
}
