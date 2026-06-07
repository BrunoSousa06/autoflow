package com.autoflow.service.orcamento.impl;

import com.autoflow.domain.orcamento.*;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.service.orcamento.OrcamentoFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class OrcamentoFactoryImpl implements OrcamentoFactory {

    @Override
    public OrcamentoEntity criarPrincipalDisponivel(OrdemServicoEntity ordemServico, int versao, LocalDateTime now) {
        List<OrcamentoServicoEntity> orcamentoServicoEntities = ordemServico.getServicosSolicitados().stream().map(servicoSolicitadoEntity -> OrcamentoServicoEntity.builder().valor(servicoSolicitadoEntity.getValor()).nome(servicoSolicitadoEntity.getNome()).servicoId(servicoSolicitadoEntity.getServicoId()).build()).toList();

        List<OrcamentoItemNecessarioEntity> itemNecessarioEntities =
                ordemServico.getServicosSolicitados().stream()
                        .flatMap(servico -> servico.getItensNecessarios().stream()
                                .map(item -> OrcamentoItemNecessarioEntity.builder()
                                        .servicoOsId(servico.getId())
                                        .tipo(item.getTipo())
                                        .pecaInsumoId(item.getPecaInsumoId())
                                        .quantidade(item.getQuantidade())
                                        .valorUnitario(item.getValorUnitario())
                                        .valorTotal(item.getValorTotal())
                                        .nome(item.getNome())
                                        .build()))
                        .toList();

        BigDecimal totalServicos = totalServicos(orcamentoServicoEntities);
        BigDecimal totalItens = totalItens(itemNecessarioEntities);
        BigDecimal totalGeral = totalGeral(totalServicos, totalItens);

        return OrcamentoEntity.builder()
                .ordemServicoId(ordemServico.getId())
                .numeroOs(ordemServico.getNumeroOs())
                .tipo(TipoOrcamento.PRINCIPAL).versao(versao)
                .status(StatusOrcamento.DISPONIVEL).criadoEm(now)
                .disponibilizadoEm(now)
                .servicos(orcamentoServicoEntities)
                .itens(itemNecessarioEntities)
                .totalServicos(totalServicos)
                .totalItens(totalItens)
                .totalGeral(totalGeral)
                .build();
    }

    @Override
    public OrcamentoEntity criarAdicionalDisponivel(OrdemServicoEntity ordemServico, ReparoAdicionalEntity reparoSalvo, int versao, LocalDateTime now) {
        List<OrcamentoServicoEntity> orcamentoServicoEntities = reparoSalvo.getServicos().stream().map(servicoSolicitadoEntity -> OrcamentoServicoEntity.builder().valor(servicoSolicitadoEntity.getValor()).nome(servicoSolicitadoEntity.getNome()).servicoId(servicoSolicitadoEntity.getServicoId()).build()).toList();

        List<OrcamentoItemNecessarioEntity> itemNecessarioEntities =
                reparoSalvo.getServicos().stream()
                        .flatMap(servico -> servico.getItensNecessarios().stream()
                                .map(item -> OrcamentoItemNecessarioEntity.builder()
                                        .servicoOsId(servico.getId())
                                        .tipo(item.getTipo())
                                        .pecaInsumoId(item.getPecaInsumoId())
                                        .quantidade(item.getQuantidade())
                                        .valorUnitario(item.getValorUnitario())
                                        .valorTotal(item.getValorTotal())
                                        .nome(item.getNome())
                                        .build()))
                        .toList();

        BigDecimal totalServicos = totalServicos(orcamentoServicoEntities);
        BigDecimal totalItens = totalItens(itemNecessarioEntities);
        BigDecimal totalGeral = totalGeral(totalServicos, totalItens);

        return OrcamentoEntity.builder()
                .ordemServicoId(ordemServico.getId())
                .numeroOs(ordemServico.getNumeroOs())
                .tipo(TipoOrcamento.ADICIONAL).versao(versao)
                .status(StatusOrcamento.DISPONIVEL).criadoEm(now)
                .disponibilizadoEm(now)
                .servicos(orcamentoServicoEntities)
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
