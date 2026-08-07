package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.dto.ordemservico.FinalizarDiagnosticoOutput;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoNotificacaoGateway;
import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.application.gateway.OrcamentoVersioningGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinalizarDiagnosticoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final OrdemServicoAccessPolicy accessPolicy;
    private final OrcamentoVersioningGateway versioningGateway;
    private final OrcamentoFactory orcamentoFactory;
    private final OrcamentoGateway orcamentoGateway;
    private final OrcamentoPublicacaoGateway publicacaoGateway;
    private final OrcamentoNotificacaoGateway notificacaoGateway;
    private final HistoricoStatusOsGateway historicoGateway;

    @Transactional
    public FinalizarDiagnosticoOutput execute(String numeroOs, String emailUsuarioLogado) {
        OrdemServicoEntity os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
        UsuarioEntity usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário autenticado não encontrado."));
        if (!RoleEnum.ADMIN.equals(usuario.getRole())) accessPolicy.validarPodeAlterarDiagnostico(os, usuario);

        os.finalizarDiagnostico();
        int versao = versioningGateway.proximaVersaoPrincipalNumeroOs(numeroOs);
        LocalDateTime agora = LocalDateTime.now(ZoneId.systemDefault());
        OrcamentoEntity orcamento = orcamentoFactory.criarPrincipalDisponivel(os, versao, agora);
        os.aguardarAprovacao();
        OrcamentoEntity salvo = orcamentoGateway.save(orcamento);
        String publicUrl = publicacaoGateway.publicar(salvo.getId());
        try {
            notificacaoGateway.notificar(salvo, os, publicUrl);
        } catch (Exception exception) {
            log.error("Falha ao notificar cliente sobre orçamento da OS {}. orcamentoId={}", numeroOs, salvo.getId(), exception);
        }
        OrdemServicoEntity osSalva = ordemServicoGateway.save(os);
        historicoGateway.save(HistoricoStatusOsEntity.criar(osSalva.getId(), osSalva.getStatus(),
                StatusOrdemServicoMensagemPolicy.mensagem(osSalva.getStatus()), osSalva.getNumeroOs()));
        return new FinalizarDiagnosticoOutput(osSalva, salvo.getId(), publicUrl);
    }
}
