package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.application.port.in.ordemservico.reparoadicional.AprovarReparoAdicionalUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.ItemNecessario;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.SituacaoEstoque;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class AprovarReparoAdicionalUseCaseImpl implements AprovarReparoAdicionalUseCase {

    private final ReparoAdicionalGateway reparoAdicionalGateway;
    private final OrdemServicoGateway ordemServicoGateway;

    @TransactionalUseCase
    @Override
    public OrdemServico execute(Long reparoAdicionalId) {
        ReparoAdicional reparo = reparoAdicionalGateway.findByIdForUpdate(reparoAdicionalId)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));

        OrdemServico ordemServico = ordemServicoGateway.findByNumeroOsForUpdate(reparo.getNumeroOs())
                .orElseThrow(() -> new IllegalArgumentException("Ordem de serviço não encontrada."));

        reparo.aprovar();

        List<ServicoSolicitado> servicosParaOs = reparo.getServicos().stream()
                .map(servico -> copiarServico(servico, ordemServico))
                .toList();
        ordemServico.adicionarServicosSolicitados(servicosParaOs);

        reparoAdicionalGateway.save(reparo);
        return ordemServicoGateway.save(ordemServico);
    }

    private ServicoSolicitado copiarServico(
            ServicoSolicitado origem,
            OrdemServico ordemServico
    ) {
        ServicoSolicitado copia = new ServicoSolicitado();
        copia.setServicoId(origem.getServicoId());
        copia.setNome(origem.getNome());
        copia.setValor(origem.getValor());
        copia.setStatus(origem.getStatus());
        copia.registrarItensNecessarios(origem.getItensNecessarios().stream()
                .map(this::copiarItem)
                .toList());
        return copia;
    }

    private ItemNecessario copiarItem(ItemNecessario origem) {
        return ItemNecessario.criar(
                origem.getPecaInsumoId(),
                origem.getNome(),
                origem.getTipo(),
                origem.getValorUnitario(),
                origem.getQuantidade(),
                origem.getStatus(),
                new SituacaoEstoque(
                        origem.getQuantidadeDisponivel(),
                        origem.getMotivoPendencia()
                )
        );
    }
}
