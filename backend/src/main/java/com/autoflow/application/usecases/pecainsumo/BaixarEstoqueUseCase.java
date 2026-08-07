package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.SituacaoEstoque;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BaixarEstoqueUseCase {
    private final PecaInsumoGateway pecaInsumoGateway;

    public List<ItemNecessarioEntity> execute(List<ItemNecessarioEntity> itens) {
        if (itens == null || itens.isEmpty()) return Collections.emptyList();
        List<Long> ids = itens.stream().map(ItemNecessarioEntity::getPecaInsumoId).distinct().toList();
        Map<Long, PecaInsumoEntity> estoque = pecaInsumoGateway.findAllById(ids).stream()
                .collect(Collectors.toMap(PecaInsumoEntity::getId, Function.identity()));
        List<PecaInsumoEntity> alteradas = new ArrayList<>();
        List<ItemNecessarioEntity> atualizados = itens.stream().map(item -> {
            PecaInsumoEntity peca = estoque.get(item.getPecaInsumoId());
            if (peca == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Peça/Insumo não encontrado com o ID: " + item.getPecaInsumoId());
            boolean disponivel = peca.getQuantidade() >= item.getQuantidade();
            if (disponivel) { peca.setQuantidade(peca.getQuantidade() - item.getQuantidade()); alteradas.add(peca); }
            return ItemNecessarioEntity.criar(peca.getId(), peca.getNome(), peca.getTipo(), peca.getValor(),
                    item.getQuantidade(), disponivel ? StatusItemNecessario.UTILIZADO : StatusItemNecessario.PENDENTE,
                    new SituacaoEstoque(peca.getQuantidade(), disponivel ? null : MotivoPendenciaItem.ESTOQUE_INSUFICIENTE));
        }).toList();
        pecaInsumoGateway.saveAll(alteradas);
        return atualizados;
    }
}
