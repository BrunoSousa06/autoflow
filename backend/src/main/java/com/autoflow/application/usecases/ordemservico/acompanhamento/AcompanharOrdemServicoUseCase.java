package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.HistoricoStatusOsOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.OrcamentoResumoOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.ServicoSolicitadoOutput;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.application.usecases.ordemservico.StatusOrdemServicoMensagemPolicy;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcompanharOrdemServicoUseCase {

    private final VeiculoClienteGateway clienteGateway;
    private final OrdemServicoGateway ordemServicoGateway;
    private final OrcamentoGateway orcamentoGateway;
    private final HistoricoStatusOsGateway historicoStatusOsGateway;

    public List<AcompanhamentoOrdemServicoOutput> execute(String emailCliente) {
        Long clienteId = clienteGateway.findIdByUsuarioEmail(emailCliente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente autenticado não encontrado."));

        return ordemServicoGateway
                .findByClienteIdOrderByDataAberturaDesc(clienteId)
                .stream()
                .map(this::montarAcompanhamento)
                .toList();
    }

    private AcompanhamentoOrdemServicoOutput montarAcompanhamento(
            OrdemServicoEntity ordemServico) {
        OrcamentoEntity orcamentoAtual = buscarOrcamentoAtual(ordemServico.getNumeroOs());
        List<HistoricoStatusOsEntity> historico = historicoStatusOsGateway
                .findByNumeroOsOrderByRegistradoEmAsc(ordemServico.getNumeroOs());

        return new AcompanhamentoOrdemServicoOutput(
                ordemServico.getNumeroOs(),
                ordemServico.getVeiculoPlaca(),
                ordemServico.getStatus(),
                ordemServico.getDataAbertura(),
                ordemServico.getUltimaAtualizacao(),
                ordemServico.getServicosSolicitados().stream()
                        .map(ServicoSolicitadoOutput::from)
                        .toList(),
                orcamentoAtual == null ? null : OrcamentoResumoOutput.from(orcamentoAtual),
                orcamentoAtual == null ? null : orcamentoAtual.getStatus(),
                StatusOrdemServicoMensagemPolicy.mensagem(ordemServico.getStatus()),
                historico.stream().map(HistoricoStatusOsOutput::from).toList());
    }

    private OrcamentoEntity buscarOrcamentoAtual(String numeroOs) {
        return orcamentoGateway.findByNumeroOsAndStatus(numeroOs, StatusOrcamento.DISPONIVEL)
                .or(() -> orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc(numeroOs))
                .orElse(null);
    }
}
