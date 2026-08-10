package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.dto.notificacao.OrcamentoNotificacao;
import com.autoflow.application.dto.orcamento.OrcamentoPublicacao;
import com.autoflow.application.dto.ordemservico.reparoadicional.CriarReparoAdicionalCommand;
import com.autoflow.application.dto.ordemservico.reparoadicional.CriarReparoAdicionalOutput;
import com.autoflow.application.dto.ordemservico.reparoadicional.ItemReparoAdicionalCommand;
import com.autoflow.application.dto.ordemservico.reparoadicional.ServicoReparoAdicionalCommand;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.*;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.usecases.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
public class CriarReparoAdicionalUseCase {

    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;
    private final ServicoGateway servicoGateway;
    private final ConsultarDisponibilidadeEstoqueUseCase consultarDisponibilidadeEstoqueUseCase;
    private final ReparoAdicionalGateway reparoAdicionalGateway;
    private final OrcamentoComplementarGateway orcamentoComplementarGateway;
    private final OrcamentoPublicacaoGateway orcamentoPublicacaoGateway;
    private final OrcamentoNotificacaoGateway orcamentoNotificacaoGateway;
    private final Clock clock;

    @TransactionalUseCase
    public CriarReparoAdicionalOutput execute(CriarReparoAdicionalCommand command) {
        validarCommand(command);

        OrdemServicoEntity ordemServico = ordemServicoGateway.findByNumeroOsForUpdate(command.numeroOs())
                .orElseThrow(() -> ApplicationException.notFound(
                        "Ordem de serviço não encontrada."
                ));

        validarStatus(ordemServico);
        validarServicosDuplicados(command.servicos(), ordemServico);

        var usuario = usuarioGateway.findByEmail(command.emailMecanico())
                .orElseThrow(() -> ApplicationException.notFound(
                        "Usuário autenticado não encontrado."
                ));
        validarAutorizacao(ordemServico, usuario);
        Long mecanicoId = usuario.getId();

        List<ServicoSolicitadoEntity> servicos = command.servicos().stream()
                .map(this::enriquecerServico)
                .toList();

        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar(
                ordemServico.getNumeroOs(),
                mecanicoId,
                servicos
        );
        reparo.setOrdemServicoId(ordemServico.getId());

        ReparoAdicionalEntity reparoSalvo = reparoAdicionalGateway.save(reparo);
        OrcamentoEntity orcamento = orcamentoComplementarGateway.criarESalvar(
                ordemServico,
                reparoSalvo,
                LocalDateTime.ofInstant(clock.instant(), ZoneId.systemDefault())
        );

        reparoSalvo.setOrcamentoId(orcamento.getId());
        OrcamentoPublicacao publicacao = orcamentoPublicacaoGateway.publicarComLinks(orcamento.getId());
        String urlPublica = publicacao.urlPdf();

        tentarNotificar(orcamento, publicacao);
        reparoAdicionalGateway.save(reparoSalvo);

        return new CriarReparoAdicionalOutput(
                reparoSalvo.getId(),
                orcamento.getId(),
                urlPublica
        );
    }

    private void validarCommand(CriarReparoAdicionalCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Comando de criação do reparo adicional é obrigatório.");
        }
        if (command.numeroOs() == null || command.numeroOs().isBlank()) {
            throw new IllegalArgumentException("Número da ordem de serviço é obrigatório.");
        }
        if (command.emailMecanico() == null || command.emailMecanico().isBlank()) {
            throw new IllegalArgumentException("E-mail do mecânico é obrigatório.");
        }
        if (command.servicos() == null || command.servicos().isEmpty()) {
            throw new IllegalArgumentException("Reparo adicional deve ter ao menos um servico.");
        }
    }

    private void validarStatus(OrdemServicoEntity ordemServico) {
        if (StatusOrdemServico.FINALIZADA.equals(ordemServico.getStatus())
                || StatusOrdemServico.ENTREGUE.equals(ordemServico.getStatus())) {
            throw new IllegalStateException("Não é possível registrar reparo adicional em uma OS finalizada.");
        }
    }

    private void validarAutorizacao(OrdemServicoEntity ordemServico, UsuarioEntity usuario) {
        if (RoleEnum.ADMIN.equals(usuario.getRole())) {
            return;
        }
        if (!RoleEnum.MECANICO.equals(usuario.getRole())) {
            throw ApplicationException.forbidden(
                    "Somente mecânico atribuído ou administrador pode criar reparo adicional.");
        }
        if (ordemServico.getDiagnostico() == null || ordemServico.getDiagnostico().getMecanico() == null) {
            throw ApplicationException.badRequest(
                    "A ordem de serviço ainda não possui mecânico atribuído.");
        }
        if (!java.util.Objects.equals(ordemServico.getDiagnostico().getMecanico().getId(), usuario.getId())) {
            throw ApplicationException.forbidden(
                    "Somente o mecânico atribuído pode criar reparo adicional.");
        }
    }

    private void validarServicosDuplicados(
            List<ServicoReparoAdicionalCommand> servicos,
            OrdemServicoEntity ordemServico
    ) {
        Set<Long> idsInformados = new HashSet<>();
        Set<Long> pecasInformadas = new HashSet<>();
        for (ServicoReparoAdicionalCommand servico : servicos) {
            if (servico == null || servico.servicoId() == null) {
                throw new IllegalArgumentException("Servico e obrigatorio.");
            }
            if (!idsInformados.add(servico.servicoId())) {
                throw new IllegalArgumentException(
                        "Serviço duplicado no reparo adicional: ID " + servico.servicoId()
                );
            }
            if (servico.itensNecessarios() == null || servico.itensNecessarios().isEmpty()) {
                continue;
            }
            for (ItemReparoAdicionalCommand item : servico.itensNecessarios()) {
                if (item != null && item.pecaInsumoId() != null && !pecasInformadas.add(item.pecaInsumoId())) {
                    throw new IllegalArgumentException(
                            "Peça/Insumo duplicado no reparo adicional: ID " + item.pecaInsumoId()
                    );
                }
            }
        }

        Set<Long> servicosJaNaOs = ordemServico.getServicosSolicitados().stream()
                .map(ServicoSolicitadoEntity::getServicoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        servicos.stream()
                .map(ServicoReparoAdicionalCommand::servicoId)
                .filter(servicosJaNaOs::contains)
                .findFirst()
                .ifPresent(id -> {
                    throw new IllegalArgumentException(
                            "Serviço já incluído na ordem de serviço e não pode ser adicionado novamente: ID " + id
                    );
                });
    }

    private ServicoSolicitadoEntity enriquecerServico(ServicoReparoAdicionalCommand command) {
        if (command.itensNecessarios() == null || command.itensNecessarios().isEmpty()) {
            throw new IllegalArgumentException("Servico do reparo adicional deve ter ao menos um item necessario.");
        }

        ServicoOutput servicoCatalogo = servicoGateway.findById(command.servicoId())
                .orElseThrow(() -> ApplicationException.notFound(
                        "Serviço não encontrado com o ID: " + command.servicoId()
                ));

        List<ItemNecessarioEntity> itensSolicitados = command.itensNecessarios().stream()
                .map(this::mapearItemSolicitado)
                .toList();

        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity();
        servico.setServicoId(servicoCatalogo.getId());
        servico.setNome(servicoCatalogo.getNome());
        servico.setValor(servicoCatalogo.getValor());
        servico.setStatus(StatusServicoOs.AGUARDANDO);
        servico.registrarItensNecessarios(
                consultarDisponibilidadeEstoqueUseCase.execute(itensSolicitados)
        );
        return servico;
    }

    private ItemNecessarioEntity mapearItemSolicitado(ItemReparoAdicionalCommand command) {
        if (command == null || command.pecaInsumoId() == null || command.quantidade() == null) {
            throw new IllegalArgumentException("Item necessario e obrigatorio.");
        }
        if (command.quantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
        }

        ItemNecessarioEntity item = new ItemNecessarioEntity();
        item.setPecaInsumoId(command.pecaInsumoId());
        item.setQuantidade(command.quantidade());
        return item;
    }

    private void tentarNotificar(
            OrcamentoEntity orcamento,
            OrcamentoPublicacao publicacao
    ) {
        try {
            var cliente = orcamento.getCliente();
            orcamentoNotificacaoGateway.notificar(new OrcamentoNotificacao(
                    orcamento.getId(), orcamento.getTipo(), orcamento.getNumeroOs(),
                    cliente.getNome(), cliente.getEmail(), publicacao.urlPdf(), publicacao.urlDecisao()));
        } catch (Exception exception) {
            log.error(
                    "Falha ao notificar cliente sobre orçamento complementar. orcamentoId={}",
                    orcamento.getId(),
                    exception
            );
        }
    }
}
