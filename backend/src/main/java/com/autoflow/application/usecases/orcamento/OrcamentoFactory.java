package com.autoflow.application.usecases.orcamento;

import com.autoflow.domain.orcamento.*;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class OrcamentoFactory {

    public OrcamentoEntity criarPrincipalDisponivel(OrdemServico ordemServico, int versao, LocalDateTime now) {
        List<OrcamentoServicoEntity> orcamentoServicoEntities = ordemServico.getServicosSolicitados().stream().map(servicoSolicitadoEntity -> OrcamentoServicoEntity.builder().valor(servicoSolicitadoEntity.getValor()).nome(servicoSolicitadoEntity.getNome()).servicoId(servicoSolicitadoEntity.getServicoId()).build()).collect(Collectors.toCollection(ArrayList::new));

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
                        .collect(Collectors.toCollection(ArrayList::new));

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
                .numeroOs(ordemServico.getNumeroOs())
                .cliente(ClienteOrcamentoSnapshot.from(ordemServico.getCliente()))
                .veiculo(VeiculoOrcamentoSnapshot.from(ordemServico.getVeiculo()))
                .build();
    }

    public OrcamentoEntity criarAdicionalDisponivel(OrdemServico ordemServico, ReparoAdicional reparoSalvo, int versao, LocalDateTime now) {
        List<OrcamentoServicoEntity> orcamentoServicoEntities = reparoSalvo.getServicos().stream().map(servicoSolicitadoEntity -> OrcamentoServicoEntity.builder().valor(servicoSolicitadoEntity.getValor()).nome(servicoSolicitadoEntity.getNome()).servicoId(servicoSolicitadoEntity.getServicoId()).build()).collect(Collectors.toCollection(ArrayList::new));

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
                        .collect(Collectors.toCollection(ArrayList::new));

        BigDecimal totalServicos = totalServicos(orcamentoServicoEntities);
        BigDecimal totalItens = totalItens(itemNecessarioEntities);
        BigDecimal totalGeral = totalGeral(totalServicos, totalItens);

        return OrcamentoEntity.builder()
                .ordemServicoId(ordemServico.getId())
                .numeroOs(ordemServico.getNumeroOs())
                .tipo(TipoOrcamento.COMPLEMENTAR).versao(versao)
                .status(StatusOrcamento.DISPONIVEL).criadoEm(now)
                .disponibilizadoEm(now)
                .servicos(orcamentoServicoEntities)
                .itens(itemNecessarioEntities)
                .totalServicos(totalServicos)
                .totalItens(totalItens)
                .totalGeral(totalGeral)
                .numeroOs(ordemServico.getNumeroOs())
                .cliente(ClienteOrcamentoSnapshot.from(ordemServico.getCliente()))
                .veiculo(VeiculoOrcamentoSnapshot.from(ordemServico.getVeiculo()))
                .build();
    }

    public OrcamentoEntity criarPrincipalConsolidadoDisponivel(
            OrdemServico ordemServico,
            ReparoAdicional reparo,
            int versao,
            LocalDateTime now
    ) {
        List<ServicoSolicitado> servicosConsolidados = new ArrayList<>();
        servicosConsolidados.addAll(ordemServico.getServicosSolicitados());
        servicosConsolidados.addAll(reparo.getServicos());

        List<OrcamentoServicoEntity> servicos = servicosConsolidados.stream()
                .map(servico -> OrcamentoServicoEntity.builder()
                        .servicoId(servico.getServicoId())
                        .nome(servico.getNome())
                        .valor(servico.getValor())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        List<OrcamentoItemNecessarioEntity> itens = servicosConsolidados.stream()
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
                .collect(Collectors.toCollection(ArrayList::new));

        BigDecimal totalServicos = totalServicos(servicos);
        BigDecimal totalItens = totalItens(itens);

        return OrcamentoEntity.builder()
                .ordemServicoId(ordemServico.getId())
                .tipo(TipoOrcamento.PRINCIPAL)
                .versao(versao)
                .status(StatusOrcamento.DISPONIVEL)
                .criadoEm(now)
                .disponibilizadoEm(now)
                .servicos(servicos)
                .itens(itens)
                .totalServicos(totalServicos)
                .totalItens(totalItens)
                .totalGeral(totalServicos.add(totalItens))
                .numeroOs(ordemServico.getNumeroOs())
                .cliente(ClienteOrcamentoSnapshot.from(ordemServico.getCliente()))
                .veiculo(VeiculoOrcamentoSnapshot.from(ordemServico.getVeiculo()))
                .build();
    }

    private BigDecimal totalGeral(BigDecimal totalServicos, BigDecimal totalItens) {
        return totalServicos.add(totalItens);
    }

    private BigDecimal totalServicos(List<OrcamentoServicoEntity> servicos) {
        if (servicos == null || servicos.isEmpty()) return BigDecimal.ZERO;
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
