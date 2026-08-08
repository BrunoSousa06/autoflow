package com.autoflow.service.ordemservico;

import com.autoflow.application.dto.ordemservico.OrdemServicoCriadaOutput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.TokenAcompanhamentoOutput;
import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.usecases.cliente.BuscarClientePorCpfCnpjUseCase;
import com.autoflow.application.usecases.ordemservico.StatusOrdemServicoMensagemPolicy;
import com.autoflow.application.usecases.ordemservico.acompanhamento.EnviarLinkAcompanhamentoUseCase;
import com.autoflow.application.usecases.ordemservico.acompanhamento.GerarTokenAcompanhamentoUseCase;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CriarOrdemServicoUseCase {

    private final BuscarClientePorCpfCnpjUseCase buscarCliente;
    private final BuscarOuCadastrarVeiculoForOrdemServicoUseCase buscarOuCadastrarVeiculo;
    private final ServicoGateway servicoGateway;
    private final OrdemServicoGateway ordemServicoGateway;
    private final HistoricoStatusOsGateway historicoGateway;
    private final GerarTokenAcompanhamentoUseCase gerarToken;
    private final EnviarLinkAcompanhamentoUseCase enviarLink;

    @Transactional
    public OrdemServicoCriadaOutput execute(
            String cpfCnpj,
            VeiculoOrdemServicoInput veiculoRequest,
            List<ServicoSolicitadoEntity> servicosSolicitados) {
        validarServicos(servicosSolicitados);
        ClienteOutput cliente = buscarCliente.execute(cpfCnpj);
        VeiculoEntity veiculo = buscarOuCadastrarVeiculo.execute(cliente, veiculoRequest);
        OrdemServicoEntity os = OrdemServicoEntity.criar(
                cliente.id(), cliente.nome(), cliente.cpfCnpj(), cliente.email(), cliente.telefone(), veiculo);
        os.adicionarServicosSolicitados(servicosSolicitados.stream()
                .map(servico -> preencherServico(os, servico)).toList());
        OrdemServicoEntity salva = ordemServicoGateway.save(os);
        historicoGateway.save(HistoricoStatusOsEntity.criar(
                salva.getId(),
                salva.getStatus(),
                StatusOrdemServicoMensagemPolicy.mensagem(salva.getStatus()),
                salva.getNumeroOs()));
        TokenAcompanhamentoOutput token = gerarToken.execute(salva.getId());
        try {
            enviarLink.execute(salva, token.token());
        } catch (RuntimeException exception) {
            log.error("Não foi possível enviar o link de acompanhamento da OS {}", salva.getNumeroOs(), exception);
        }
        return new OrdemServicoCriadaOutput(salva, token.token());
    }

    private ServicoSolicitadoEntity preencherServico(
            OrdemServicoEntity os,
            ServicoSolicitadoEntity solicitado) {
        ServicoOutput servico = servicoGateway.findById(solicitado.getServicoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Serviço não encontrado com o ID: " + solicitado.getServicoId()));
        ServicoSolicitadoEntity resultado = new ServicoSolicitadoEntity();
        resultado.setServicoId(servico.getId());
        resultado.setNome(servico.getNome());
        resultado.setValor(servico.getValor());
        resultado.setStatus(com.autoflow.domain.ordemservico.StatusServicoOs.AGUARDANDO);
        resultado.setOrdemServico(os);
        return resultado;
    }

    private void validarServicos(List<ServicoSolicitadoEntity> servicos) {
        if (servicos == null || servicos.isEmpty()) {
            throw new IllegalArgumentException("A ordem de servico deve ter ao menos um servico solicitado.");
        }
    }
}
