package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.usecases.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegistrarItensNecessariosUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final OrdemServicoAccessPolicy accessPolicy;
    private final ConsultarDisponibilidadeEstoqueUseCase disponibilidadeEstoque;

    @Transactional
    public OrdemServicoEntity execute(String numeroOs, Long servicoId, String emailUsuarioLogado,
                                      List<ItemNecessarioEntity> itensNecessarios) {
        OrdemServicoEntity ordemServico = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
        UsuarioEntity usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário autenticado não encontrado."));
        if (!RoleEnum.ADMIN.equals(usuario.getRole())) {
            accessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuario);
        }
        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoId);
        if (!StatusOrdemServico.EM_DIAGNOSTICO.equals(ordemServico.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Só é possível incluir peças e insumos enquanto o serviço está em diagnóstico.");
        }
        servico.registrarItensNecessarios(disponibilidadeEstoque.execute(itensNecessarios));
        return ordemServicoGateway.save(ordemServico);
    }
}
