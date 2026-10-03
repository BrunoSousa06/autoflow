package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import com.autoflow.application.port.in.ordemservico.RegistrarLaudoUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;


@RequiredArgsConstructor
public class RegistrarLaudoUseCaseImpl implements RegistrarLaudoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final OrdemServicoAccessPolicy accessPolicy;
    private final Clock clock;

    @TransactionalUseCase
    @Override
    public OrdemServico execute(String numeroOs, String emailUsuarioLogado, String laudo) {
        OrdemServico ordemServico = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        Usuario usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> ApplicationException.notFound("Usuário autenticado não encontrado."));
        accessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuario);
        ordemServico.registrarLaudo(laudo, LocalDateTime.now(clock));
        return ordemServicoGateway.save(ordemServico);
    }
}
