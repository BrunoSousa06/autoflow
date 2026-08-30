package com.autoflow.application.usecases.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FinalizarDiagnosticoService {

    public void finalizar(OrdemServico ordemServico, LocalDateTime dataHora) {
        ordemServico.finalizarDiagnostico(dataHora);
        ordemServico.aguardarAprovacao(dataHora);
    }
}
