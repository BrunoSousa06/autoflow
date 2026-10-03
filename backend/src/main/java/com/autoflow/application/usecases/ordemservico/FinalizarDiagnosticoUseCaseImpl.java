package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.*;
import com.autoflow.application.output.ordemservico.FinalizarDiagnosticoOutput;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import com.autoflow.application.port.in.ordemservico.FinalizarDiagnosticoUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class FinalizarDiagnosticoUseCaseImpl implements FinalizarDiagnosticoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final OrdemServicoAccessPolicy accessPolicy;
    private final FinalizarDiagnosticoService finalizador;
    private final CriarOrcamentoDiagnosticoService criadorOrcamento;
    private final PublicarOrcamentoDiagnosticoService publicadorOrcamento;
    private final NotificarOrcamentoDiagnosticoService notificadorOrcamento;
    private final RegistrarHistoricoStatusOsService registrarHistoricoStatusOs;
    private final Clock clock;

    @Autowired
    public FinalizarDiagnosticoUseCaseImpl(
            OrdemServicoGateway ordemServicoGateway,
            UsuarioGateway usuarioGateway,
            OrdemServicoAccessPolicy accessPolicy,
            FinalizarDiagnosticoService finalizador,
            CriarOrcamentoDiagnosticoService criadorOrcamento,
            PublicarOrcamentoDiagnosticoService publicadorOrcamento,
            NotificarOrcamentoDiagnosticoService notificadorOrcamento,
            RegistrarHistoricoStatusOsService registrarHistoricoStatusOs,
            Clock clock) {
        this.ordemServicoGateway = ordemServicoGateway;
        this.usuarioGateway = usuarioGateway;
        this.accessPolicy = accessPolicy;
        this.finalizador = finalizador;
        this.criadorOrcamento = criadorOrcamento;
        this.publicadorOrcamento = publicadorOrcamento;
        this.notificadorOrcamento = notificadorOrcamento;
        this.registrarHistoricoStatusOs = registrarHistoricoStatusOs;
        this.clock = clock;
    }

    FinalizarDiagnosticoUseCaseImpl(
            OrdemServicoGateway ordemServicoGateway,
            UsuarioGateway usuarioGateway,
            OrdemServicoAccessPolicy accessPolicy,
            OrcamentoVersioningGateway versioningGateway,
            com.autoflow.application.usecases.orcamento.OrcamentoFactory orcamentoFactory,
            OrcamentoGateway orcamentoGateway,
            OrcamentoPublicacaoGateway publicacaoGateway,
            OrcamentoNotificacaoGateway notificacaoGateway,
            RegistrarHistoricoStatusOsService registrarHistoricoStatusOs,
            Clock clock) {
        this(ordemServicoGateway, usuarioGateway, accessPolicy,
                new FinalizarDiagnosticoService(),
                new CriarOrcamentoDiagnosticoService(versioningGateway, orcamentoFactory, orcamentoGateway),
                new PublicarOrcamentoDiagnosticoService(publicacaoGateway),
                new NotificarOrcamentoDiagnosticoService(notificacaoGateway),
                registrarHistoricoStatusOs, clock);
    }

    @TransactionalUseCase
    @Override
    public FinalizarDiagnosticoOutput execute(String numeroOs, String emailUsuarioLogado) {
        OrdemServico os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        Usuario usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> ApplicationException.notFound("Usuário autenticado não encontrado."));
        if (!RoleEnum.ADMIN.equals(usuario.getRole())) accessPolicy.validarPodeAlterarDiagnostico(os, usuario);

        LocalDateTime agora = LocalDateTime.now(clock);
        finalizador.finalizar(os, agora);
        Orcamento salvo = criadorOrcamento.criar(os, numeroOs, agora);
        var publicacao = publicadorOrcamento.publicar(salvo.getId());
        String publicUrl = publicacao.urlPdf();
        notificadorOrcamento.tentarNotificar(salvo, publicacao);
        OrdemServico osSalva = ordemServicoGateway.save(os);
        registrarHistoricoStatusOs.registrar(osSalva);
        return new FinalizarDiagnosticoOutput(osSalva, salvo.getId(), publicUrl);
    }
}
