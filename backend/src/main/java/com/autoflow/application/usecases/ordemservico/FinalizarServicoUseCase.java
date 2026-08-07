package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class FinalizarServicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final HistoricoStatusOsGateway historicoStatusOsGateway;

    @Transactional
    public OrdemServicoEntity execute(String numeroOs, Long servicoId) {
        OrdemServicoEntity os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
        os.buscarServicoSolicitado(servicoId).finalizar();
        os.atualizarUltimaAtualizacao();
        os.finalizarSeTodosServicosFinalizados();
        OrdemServicoEntity salva = ordemServicoGateway.save(os);
        if (StatusOrdemServico.FINALIZADA.equals(salva.getStatus())) {
            historicoStatusOsGateway.save(HistoricoStatusOsEntity.criar(salva.getId(), salva.getStatus(),
                    StatusOrdemServicoMensagemPolicy.mensagem(salva.getStatus()), salva.getNumeroOs()));
        }
        return salva;
    }
}
