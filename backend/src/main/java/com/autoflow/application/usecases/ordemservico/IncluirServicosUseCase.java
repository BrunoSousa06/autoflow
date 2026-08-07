package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IncluirServicosUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final ServicoGateway servicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final OrdemServicoAccessPolicy accessPolicy;

    @Transactional
    public OrdemServicoEntity execute(String numeroOs, List<ServicoSolicitadoEntity> servicos,
                                      String emailUsuarioLogado) {
        OrdemServicoEntity os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
        if (StatusOrdemServico.EM_DIAGNOSTICO.equals(os.getStatus())) {
            UsuarioEntity usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário autenticado não encontrado."));
            if (!RoleEnum.ADMIN.equals(usuario.getRole())) accessPolicy.validarPodeAlterarDiagnostico(os, usuario);
        }
        if (servicos == null || servicos.isEmpty()) {
            throw new IllegalArgumentException("A ordem de servico deve ter ao menos um servico solicitado.");
        }
        List<ServicoSolicitadoEntity> preenchidos = servicos.stream().map(servico -> {
            ServicoEntity catalogo = servicoGateway.findById(servico.getServicoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Serviço não encontrado com o ID: " + servico.getServicoId()));
            ServicoSolicitadoEntity resultado = new ServicoSolicitadoEntity();
            resultado.setServicoId(catalogo.getId());
            resultado.setNome(catalogo.getNome());
            resultado.setValor(catalogo.getValor());
            resultado.setStatus(com.autoflow.domain.ordemservico.StatusServicoOs.AGUARDANDO);
            resultado.setOrdemServico(os);
            return resultado;
        }).toList();
        os.adicionarServicosSolicitados(preenchidos);
        return ordemServicoGateway.save(os);
    }
}
