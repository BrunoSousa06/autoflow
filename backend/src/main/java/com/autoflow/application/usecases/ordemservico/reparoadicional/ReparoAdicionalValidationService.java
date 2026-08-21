package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.input.ordemservico.reparoadicional.CriarReparoAdicionalCommand;
import com.autoflow.application.input.ordemservico.reparoadicional.ItemReparoAdicionalCommand;
import com.autoflow.application.input.ordemservico.reparoadicional.ServicoReparoAdicionalCommand;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReparoAdicionalValidationService {

    public void validar(CriarReparoAdicionalCommand command, OrdemServico ordemServico, Usuario usuario) {
        validarCommand(command);
        validarStatus(ordemServico);
        validarAutorizacao(ordemServico, usuario);
        validarServicosDuplicados(command.servicos(), ordemServico);
    }

    public void validarComando(CriarReparoAdicionalCommand command) {
        validarCommand(command);
    }

    public void validarOrdem(CriarReparoAdicionalCommand command, OrdemServico ordemServico) {
        validarStatus(ordemServico);
        validarServicosDuplicados(command.servicos(), ordemServico);
    }

    public void validarAutorizacaoPara(OrdemServico ordemServico, Usuario usuario) {
        validarAutorizacao(ordemServico, usuario);
    }

    private void validarCommand(CriarReparoAdicionalCommand command) {
        if (command == null) throw new IllegalArgumentException("Comando de criação do reparo adicional é obrigatório.");
        if (command.numeroOs() == null || command.numeroOs().isBlank()) throw new IllegalArgumentException("Número da ordem de serviço é obrigatório.");
        if (command.emailMecanico() == null || command.emailMecanico().isBlank()) throw new IllegalArgumentException("E-mail do mecânico é obrigatório.");
        if (command.servicos() == null || command.servicos().isEmpty()) throw new IllegalArgumentException("Reparo adicional deve ter ao menos um servico.");
    }

    private void validarStatus(OrdemServico ordemServico) {
        if (StatusOrdemServico.FINALIZADA.equals(ordemServico.getStatus())
                || StatusOrdemServico.ENTREGUE.equals(ordemServico.getStatus())) {
            throw new IllegalStateException("Não é possível registrar reparo adicional em uma OS finalizada.");
        }
    }

    private void validarAutorizacao(OrdemServico ordemServico, Usuario usuario) {
        if (RoleEnum.ADMIN.equals(usuario.getRole())) return;
        if (!RoleEnum.MECANICO.equals(usuario.getRole())) {
            throw ApplicationException.forbidden("Somente mecânico atribuído ou administrador pode criar reparo adicional.");
        }
        if (ordemServico.getDiagnostico() == null || ordemServico.getDiagnostico().getMecanico() == null) {
            throw ApplicationException.badRequest("A ordem de serviço ainda não possui mecânico atribuído.");
        }
        if (!Objects.equals(ordemServico.getDiagnostico().getMecanico().getId(), usuario.getId())) {
            throw ApplicationException.forbidden("Somente o mecânico atribuído pode criar reparo adicional.");
        }
    }

    private void validarServicosDuplicados(List<ServicoReparoAdicionalCommand> servicos, OrdemServico ordemServico) {
        Set<Long> idsInformados = new HashSet<>();
        Set<Long> pecasInformadas = new HashSet<>();
        for (ServicoReparoAdicionalCommand servico : servicos) {
            if (servico == null || servico.servicoId() == null) throw new IllegalArgumentException("Servico e obrigatorio.");
            if (!idsInformados.add(servico.servicoId())) throw new IllegalArgumentException("Serviço duplicado no reparo adicional: ID " + servico.servicoId());
            if (servico.itensNecessarios() == null) continue;
            for (ItemReparoAdicionalCommand item : servico.itensNecessarios()) {
                if (item != null && item.pecaInsumoId() != null && !pecasInformadas.add(item.pecaInsumoId())) {
                    throw new IllegalArgumentException("Peça/Insumo duplicado no reparo adicional: ID " + item.pecaInsumoId());
                }
            }
        }
        Set<Long> servicosJaNaOs = ordemServico.getServicosSolicitados().stream()
                .map(ServicoSolicitado::getServicoId).filter(Objects::nonNull).collect(Collectors.toSet());
        servicos.stream().map(ServicoReparoAdicionalCommand::servicoId).filter(servicosJaNaOs::contains).findFirst()
                .ifPresent(id -> { throw new IllegalArgumentException("Serviço já incluído na ordem de serviço e não pode ser adicionado novamente: ID " + id); });
    }
}
