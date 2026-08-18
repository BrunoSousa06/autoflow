package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.input.ordemservico.reparoadicional.CriarReparoAdicionalCommand;
import com.autoflow.application.output.ordemservico.reparoadicional.CriarReparoAdicionalOutput;
import com.autoflow.application.port.in.ordemservico.reparoadicional.CriarReparoAdicionalUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import com.autoflow.domain.usuario.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class CriarReparoAdicionalUseCaseImpl implements CriarReparoAdicionalUseCase {

    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final ReparoAdicionalGateway reparoAdicionalGateway;
    private final ReparoAdicionalValidationService validator;
    private final EnriquecerServicoReparoAdicionalService servicoEnricher;
    private final CriarOrcamentoReparoAdicionalService orcamentoCreator;
    private final PublicarOrcamentoReparoAdicionalService publisher;
    private final NotificarOrcamentoReparoAdicionalService notifier;
    private final Clock clock;

    @Autowired
    public CriarReparoAdicionalUseCaseImpl(
            OrdemServicoGateway ordemServicoGateway,
            UsuarioGateway usuarioGateway,
            ReparoAdicionalGateway reparoAdicionalGateway,
            ReparoAdicionalValidationService validator,
            EnriquecerServicoReparoAdicionalService servicoEnricher,
            CriarOrcamentoReparoAdicionalService orcamentoCreator,
            PublicarOrcamentoReparoAdicionalService publisher,
            NotificarOrcamentoReparoAdicionalService notifier,
            Clock clock) {
        this.ordemServicoGateway = ordemServicoGateway;
        this.usuarioGateway = usuarioGateway;
        this.reparoAdicionalGateway = reparoAdicionalGateway;
        this.validator = validator;
        this.servicoEnricher = servicoEnricher;
        this.orcamentoCreator = orcamentoCreator;
        this.publisher = publisher;
        this.notifier = notifier;
        this.clock = clock;
    }

    CriarReparoAdicionalUseCaseImpl(
            OrdemServicoGateway ordemServicoGateway,
            UsuarioGateway usuarioGateway,
            com.autoflow.application.gateway.ServicoGateway servicoGateway,
            com.autoflow.application.port.in.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase estoque,
            ReparoAdicionalGateway reparoAdicionalGateway,
            com.autoflow.application.gateway.OrcamentoComplementarGateway orcamentoGateway,
            com.autoflow.application.gateway.OrcamentoPublicacaoGateway publicacaoGateway,
            com.autoflow.application.gateway.OrcamentoNotificacaoGateway notificacaoGateway,
            Clock clock) {
        this(ordemServicoGateway, usuarioGateway, reparoAdicionalGateway,
                new ReparoAdicionalValidationService(),
                new EnriquecerServicoReparoAdicionalService(servicoGateway, estoque),
                new CriarOrcamentoReparoAdicionalService(orcamentoGateway),
                new PublicarOrcamentoReparoAdicionalService(publicacaoGateway),
                new NotificarOrcamentoReparoAdicionalService(notificacaoGateway), clock);
    }

    @TransactionalUseCase
    @Override
    public CriarReparoAdicionalOutput execute(CriarReparoAdicionalCommand command) {
        validator.validarComando(command);
        OrdemServico ordemServico = ordemServicoGateway.findByNumeroOsForUpdate(command.numeroOs())
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        validator.validarOrdem(command, ordemServico);
        Usuario usuario = usuarioGateway.findByEmail(command.emailMecanico())
                .orElseThrow(() -> ApplicationException.notFound("Usuário autenticado não encontrado."));
        validator.validarAutorizacaoPara(ordemServico, usuario);

        List<ServicoSolicitado> servicos = command.servicos().stream().map(servicoEnricher::enriquecer).toList();
        LocalDateTime agora = LocalDateTime.ofInstant(clock.instant(), ZoneId.systemDefault());
        ReparoAdicional reparo = ReparoAdicional.criar(ordemServico.getNumeroOs(), usuario.getId(), servicos, agora);
        reparo.setOrdemServicoId(ordemServico.getId());
        ReparoAdicional salvo = reparoAdicionalGateway.save(reparo);

        Orcamento orcamento = orcamentoCreator.criar(ordemServico, salvo, agora);
        salvo.setOrcamentoId(orcamento.getId());
        var publicacao = publisher.publicar(orcamento.getId());
        notifier.tentarNotificar(orcamento, publicacao);
        reparoAdicionalGateway.save(salvo);

        return new CriarReparoAdicionalOutput(salvo.getId(), orcamento.getId(), publicacao.urlPdf());
    }
}
