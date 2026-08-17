package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.port.in.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase;
import com.autoflow.domain.ordemservico.ItemNecessario;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class RegistrarItensNecessariosUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final OrdemServicoAccessPolicy accessPolicy;
    private final ConsultarDisponibilidadeEstoqueUseCase disponibilidadeEstoque;

    @TransactionalUseCase
    public OrdemServico execute(String numeroOs, Long servicoId, String emailUsuarioLogado,
                                List<ItemNecessario> itensNecessarios) {
        OrdemServico ordemServico = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        Usuario usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> ApplicationException.notFound("Usuário autenticado não encontrado."));
        if (!RoleEnum.ADMIN.equals(usuario.getRole())) {
            accessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuario);
        }
        ServicoSolicitado servico = ordemServico.buscarServicoSolicitado(servicoId);
        if (!StatusOrdemServico.EM_DIAGNOSTICO.equals(ordemServico.getStatus())) {
            throw ApplicationException.badRequest(
                    "Só é possível incluir peças e insumos enquanto o serviço está em diagnóstico.");
        }
        servico.registrarItensNecessarios(disponibilidadeEstoque.execute(itensNecessarios));
        return ordemServicoGateway.save(ordemServico);
    }
}
