package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.output.cliente.ClienteOutput;
import com.autoflow.application.output.ordemservico.OrdemServicoCriadaOutput;
import com.autoflow.application.output.ordemservico.acompanhamento.TokenAcompanhamentoOutput;
import com.autoflow.application.output.servico.ServicoOutput;
import com.autoflow.application.input.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.output.veiculo.VeiculoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.mapper.ServicoApplicationMapper;
import com.autoflow.application.port.in.ordemservico.acompanhamento.EnviarLinkAcompanhamentoUseCase;
import com.autoflow.application.port.in.ordemservico.acompanhamento.GerarTokenAcompanhamentoUseCase;
import com.autoflow.application.port.in.ordemservico.BuscarOuCadastrarVeiculoForOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.CriarOrdemServicoUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.port.in.cliente.BuscarClientePorCpfCnpjUseCase;
import com.autoflow.domain.cliente.Cliente;
import com.autoflow.domain.ordemservico.HistoricoStatusOs;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.StatusServicoOs;
import com.autoflow.domain.ordemservico.Veiculo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j

@RequiredArgsConstructor
public class CriarOrdemServicoUseCaseImpl implements CriarOrdemServicoUseCase {

    private final BuscarClientePorCpfCnpjUseCase buscarCliente;
    private final BuscarOuCadastrarVeiculoForOrdemServicoUseCase buscarOuCadastrarVeiculo;
    private final ServicoGateway servicoGateway;
    private final OrdemServicoGateway ordemServicoGateway;
    private final HistoricoStatusOsGateway historicoGateway;
    private final GerarTokenAcompanhamentoUseCase gerarToken;
    private final EnviarLinkAcompanhamentoUseCase enviarLink;

    @TransactionalUseCase
    @Override
    public OrdemServicoCriadaOutput execute(
            String cpfCnpj,
            VeiculoOrdemServicoInput veiculoRequest,
            List<ServicoSolicitado> servicosSolicitados) {
        validarServicos(servicosSolicitados);
        ClienteOutput cliente = buscarCliente.execute(cpfCnpj);
        VeiculoOutput veiculo = buscarOuCadastrarVeiculo.execute(cliente, veiculoRequest);
        OrdemServico os = OrdemServico.criar(
                Cliente.reconstituir(cliente.id(), cliente.nome(), cliente.cpfCnpj(), cliente.telefone(), cliente.email()),
                new Veiculo(veiculo.id(), veiculo.placa(), veiculo.marca(), veiculo.modelo(), veiculo.ano()));
        os.adicionarServicosSolicitados(servicosSolicitados.stream()
                .map(servico -> preencherServico(os, servico)).toList());
        OrdemServico salva = ordemServicoGateway.save(os);
        historicoGateway.save(HistoricoStatusOs.criar(
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

    private ServicoSolicitado preencherServico(
            OrdemServico os,
            ServicoSolicitado solicitado) {
        ServicoOutput servico = servicoGateway.findById(solicitado.getServicoId())
                .map(ServicoApplicationMapper::toOutput)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Serviço não encontrado com o ID: " + solicitado.getServicoId()));
        ServicoSolicitado resultado = new ServicoSolicitado();
        resultado.setServicoId(servico.getId());
        resultado.setNome(servico.getNome());
        resultado.setValor(servico.getValor());
        resultado.setStatus(StatusServicoOs.AGUARDANDO);
        resultado.setOrdemServico(os);
        return resultado;
    }

    private void validarServicos(List<ServicoSolicitado> servicos) {
        if (servicos == null || servicos.isEmpty()) {
            throw new IllegalArgumentException("A ordem de servico deve ter ao menos um servico solicitado.");
        }
    }
}
