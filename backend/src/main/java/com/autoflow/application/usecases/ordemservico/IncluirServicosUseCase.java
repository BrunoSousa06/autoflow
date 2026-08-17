package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.mapper.ServicoApplicationMapper;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.ordemservico.StatusServicoOs;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class IncluirServicosUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final ServicoGateway servicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final OrdemServicoAccessPolicy accessPolicy;

    @TransactionalUseCase
    public OrdemServico execute(String numeroOs, List<ServicoSolicitado> servicos,
                                      String emailUsuarioLogado) {
        OrdemServico os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        if (StatusOrdemServico.EM_DIAGNOSTICO.equals(os.getStatus())) {
            Usuario usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                    .orElseThrow(() -> ApplicationException.notFound("Usuário autenticado não encontrado."));
            if (!RoleEnum.ADMIN.equals(usuario.getRole())) accessPolicy.validarPodeAlterarDiagnostico(os, usuario);
        }
        if (servicos == null || servicos.isEmpty()) {
            throw new IllegalArgumentException("A ordem de servico deve ter ao menos um servico solicitado.");
        }
        List<ServicoSolicitado> preenchidos = servicos.stream().map(servico -> {
            ServicoOutput catalogo = servicoGateway.findById(servico.getServicoId())
                    .map(ServicoApplicationMapper::toOutput)
                    .orElseThrow(() -> ApplicationException.notFound(
                            "Serviço não encontrado com o ID: " + servico.getServicoId()));
            ServicoSolicitado resultado = new ServicoSolicitado();
            resultado.setServicoId(catalogo.getId());
            resultado.setNome(catalogo.getNome());
            resultado.setValor(catalogo.getValor());
            resultado.setStatus(StatusServicoOs.AGUARDANDO);
            resultado.setOrdemServico(os);
            return resultado;
        }).toList();
        os.adicionarServicosSolicitados(preenchidos);
        return ordemServicoGateway.save(os);
    }
}
