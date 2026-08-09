package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.SituacaoEstoque;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class AprovarReparoAdicionalUseCase {

    private final ReparoAdicionalGateway reparoAdicionalGateway;
    private final OrdemServicoGateway ordemServicoGateway;

    @TransactionalUseCase
    public OrdemServicoEntity execute(Long reparoAdicionalId) {
        ReparoAdicionalEntity reparo = reparoAdicionalGateway.findByIdForUpdate(reparoAdicionalId)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));

        OrdemServicoEntity ordemServico = ordemServicoGateway.findByNumeroOsForUpdate(reparo.getNumeroOs())
                .orElseThrow(() -> new IllegalArgumentException("Ordem de serviço não encontrada."));

        reparo.aprovar();

        List<ServicoSolicitadoEntity> servicosParaOs = reparo.getServicos().stream()
                .map(servico -> copiarServico(servico, ordemServico))
                .toList();
        ordemServico.adicionarServicosSolicitados(servicosParaOs);

        reparoAdicionalGateway.save(reparo);
        return ordemServicoGateway.save(ordemServico);
    }

    private ServicoSolicitadoEntity copiarServico(
            ServicoSolicitadoEntity origem,
            OrdemServicoEntity ordemServico
    ) {
        ServicoSolicitadoEntity copia = new ServicoSolicitadoEntity();
        copia.setServicoId(origem.getServicoId());
        copia.setNome(origem.getNome());
        copia.setValor(origem.getValor());
        copia.setStatus(origem.getStatus());
        copia.setOrdemServico(ordemServico);
        copia.registrarItensNecessarios(origem.getItensNecessarios().stream()
                .map(this::copiarItem)
                .toList());
        return copia;
    }

    private ItemNecessarioEntity copiarItem(ItemNecessarioEntity origem) {
        return ItemNecessarioEntity.criar(
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
