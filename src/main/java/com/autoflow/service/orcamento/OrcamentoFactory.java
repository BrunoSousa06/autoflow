package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.*;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class OrcamentoFactory {

    public OrcamentoEntity criarPrincipalDisponivel(OrdemServicoEntity ordemServico, int versao, LocalDateTime now){
        List<OrcamentoServicoEntity> orcamentoServicoEntities = ordemServico.getServicosSolicitados().stream().map(
                servicoSolicitadoEntity -> OrcamentoServicoEntity.builder().valor(servicoSolicitadoEntity.getValor())
                        .nome(servicoSolicitadoEntity.getNome())
                        .servicoId(servicoSolicitadoEntity.getServicoId())
                        .build()
        ).toList();

        List<OrcamentoItemNecessarioEntity> itemNecessarioEntities = ordemServico.getItemNecessario().stream().map(itemNecessario ->
                OrcamentoItemNecessarioEntity.builder().tipo(itemNecessario.getTipo())
                        .pecaInsumoId(itemNecessario.getPecaInsumoId())
                        .quantidade(itemNecessario.getQuantidade())
                        .valorUnitario(itemNecessario.getValorUnitario())
                        .valorTotal(itemNecessario.getValorTotal())
                        .nome(itemNecessario.getNome()).build()
        ).toList();
        BigDecimal totalServicos = totalServicos(orcamentoServicoEntities);
        BigDecimal totalItens = totalItens(itemNecessarioEntities);
        BigDecimal totalGeral = totalGeral(totalServicos, totalItens);
        return OrcamentoEntity.builder()
                .tipo(TipoOrcamento.PRINCIPAL)
                .servicos(orcamentoServicoEntities)
                .criadoEm(now)
                .versao(versao)
                .status(StatusOrcamento.DISPONIVEL)
                .itens(itemNecessarioEntities)
                .totalServicos(totalServicos)
                .totalItens(totalItens)
                .totalGeral(totalGeral)
                .build();
    }

    private BigDecimal totalGeral(BigDecimal totalServicos, BigDecimal totalItens) {
        return totalServicos.add(totalItens);
    }

    private BigDecimal totalServicos(List<OrcamentoServicoEntity> servicos) {
        if(servicos == null || servicos.isEmpty())return BigDecimal.ZERO;
        return servicos.stream()
                .map(OrcamentoServicoEntity::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalItens(List<OrcamentoItemNecessarioEntity> itens) {
        if (itens == null || itens.isEmpty()) return BigDecimal.ZERO;
        return itens.stream()
                .map(OrcamentoItemNecessarioEntity::getValorTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
