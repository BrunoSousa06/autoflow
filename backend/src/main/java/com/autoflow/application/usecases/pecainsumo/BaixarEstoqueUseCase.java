package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.gateway.EstoqueGateway;
import com.autoflow.application.dto.pecainsumo.EstoqueItemOutput;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.SituacaoEstoque;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.EstoqueDisponibilidade;
import com.autoflow.domain.pecainsumo.EstoquePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BaixarEstoqueUseCase {
    private final EstoqueGateway estoqueGateway;

    @Transactional
    public List<ItemNecessarioEntity> execute(List<ItemNecessarioEntity> itens) {
        if (itens == null || itens.isEmpty()) return Collections.emptyList();

        validarItens(itens);
        List<Long> ids = itens.stream().map(ItemNecessarioEntity::getPecaInsumoId).distinct().toList();
        Map<Long, EstoqueItemOutput> estoque = estoqueGateway.findAllByIdForUpdate(ids).stream()
                .collect(Collectors.toMap(EstoqueItemOutput::id, Function.identity()));
        Map<Long, Integer> quantidadesRestantes = new HashMap<>();
        Map<Long, Integer> quantidadesBaixadas = new HashMap<>();
        estoque.forEach((id, item) -> quantidadesRestantes.put(id, item.quantidade()));

        List<ItemNecessarioEntity> atualizados = new ArrayList<>();
        for (ItemNecessarioEntity item : itens) {
            EstoqueItemOutput peca = estoque.get(item.getPecaInsumoId());
            if (peca == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Peça/Insumo não encontrado com o ID: " + item.getPecaInsumoId());
            }

            int quantidadeAtual = quantidadesRestantes.get(peca.id());
            EstoqueDisponibilidade disponibilidade = EstoquePolicy.classificar(
                    quantidadeAtual,
                    item.getQuantidade()
            );
            if (disponibilidade.disponivel()) {
                quantidadesRestantes.put(
                        peca.id(),
                        quantidadeAtual - item.getQuantidade()
                );
                quantidadesBaixadas.merge(peca.id(), item.getQuantidade(), Integer::sum);
            }

            atualizados.add(ItemNecessarioEntity.criar(
                    peca.id(),
                    peca.nome(),
                    peca.tipo(),
                    peca.valor(),
                    item.getQuantidade(),
                    disponibilidade.disponivel()
                            ? StatusItemNecessario.UTILIZADO
                            : StatusItemNecessario.PENDENTE,
                    new SituacaoEstoque(
                            quantidadesRestantes.get(peca.id()),
                            disponibilidade.disponivel()
                                    ? null
                                    : MotivoPendenciaItem.ESTOQUE_INSUFICIENTE
                    )
            ));
        }

        List<EstoqueItemOutput> alteradas = new ArrayList<>();
        quantidadesBaixadas.forEach((id, ignored) -> {
            EstoqueItemOutput peca = estoque.get(id);
            alteradas.add(new EstoqueItemOutput(
                    peca.id(),
                    peca.nome(),
                    peca.tipo(),
                    peca.valor(),
                    quantidadesRestantes.get(id)
            ));
        });
        if (!alteradas.isEmpty()) {
            estoqueGateway.saveAll(alteradas);
        }
        return atualizados;
    }

    private void validarItens(List<ItemNecessarioEntity> itens) {
        itens.forEach(item -> {
            if (item == null || item.getPecaInsumoId() == null || item.getQuantidade() == null) {
                throw new IllegalArgumentException("Item necessario e obrigatorio.");
            }
        });
    }
}
