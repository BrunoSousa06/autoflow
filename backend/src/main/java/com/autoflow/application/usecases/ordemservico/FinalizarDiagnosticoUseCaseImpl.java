package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.output.orcamento.OrcamentoPublicacao;
import com.autoflow.application.input.notificacao.OrcamentoNotificacao;
import com.autoflow.application.output.ordemservico.FinalizarDiagnosticoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.*;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import com.autoflow.application.port.in.ordemservico.FinalizarDiagnosticoUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j

@RequiredArgsConstructor
public class FinalizarDiagnosticoUseCaseImpl implements FinalizarDiagnosticoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final OrdemServicoAccessPolicy accessPolicy;
    private final OrcamentoVersioningGateway versioningGateway;
    private final OrcamentoFactory orcamentoFactory;
    private final OrcamentoGateway orcamentoGateway;
    private final OrcamentoPublicacaoGateway publicacaoGateway;
    private final OrcamentoNotificacaoGateway notificacaoGateway;
    private final RegistrarHistoricoStatusOsService registrarHistoricoStatusOs;

    @TransactionalUseCase
    @Override
    public FinalizarDiagnosticoOutput execute(String numeroOs, String emailUsuarioLogado) {
        OrdemServico os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        Usuario usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> ApplicationException.notFound("Usuário autenticado não encontrado."));
        if (!RoleEnum.ADMIN.equals(usuario.getRole())) accessPolicy.validarPodeAlterarDiagnostico(os, usuario);

        os.finalizarDiagnostico();
        int versao = versioningGateway.proximaVersaoPorNumeroOs(numeroOs, TipoOrcamento.PRINCIPAL);
        LocalDateTime agora = LocalDateTime.now(ZoneId.systemDefault());
        Orcamento orcamento = orcamentoFactory.criarPrincipalDisponivel(os, versao, agora);
        os.aguardarAprovacao();
        Orcamento salvo = orcamentoGateway.save(orcamento);
        OrcamentoPublicacao publicacao = publicacaoGateway.publicarComLinks(salvo.getId());
        String publicUrl = publicacao.urlPdf();
        try {
            var cliente = salvo.getCliente();
            notificacaoGateway.notificar(new OrcamentoNotificacao(
                    salvo.getId(), salvo.getTipo(), salvo.getNumeroOs(),
                    cliente.getNome(), cliente.getEmail(), publicUrl, publicacao.urlDecisao()));
        } catch (Exception exception) {
            log.error("Falha ao notificar cliente sobre orçamento da OS {}. orcamentoId={}", numeroOs, salvo.getId(), exception);
        }
        OrdemServico osSalva = ordemServicoGateway.save(os);
        registrarHistoricoStatusOs.registrar(osSalva);
        return new FinalizarDiagnosticoOutput(osSalva, salvo.getId(), publicUrl);
    }
}
