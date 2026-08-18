package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.output.pecainsumo.EstoqueItemOutput;
import com.autoflow.application.exception.EstoqueItemNaoEncontradoException;
import com.autoflow.application.gateway.EstoqueGateway;
import com.autoflow.application.port.in.pecainsumo.BaixarEstoqueUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.ItemNecessario;
import com.autoflow.domain.ordemservico.SituacaoEstoque;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.EstoquePolicy;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class BaixarEstoqueUseCaseImpl implements BaixarEstoqueUseCase {
    private final EstoqueGateway estoqueGateway;

    @TransactionalUseCase
    @Override
    public List<ItemNecessario> execute(List<ItemNecessario> itens) {
        if (itens == null || itens.isEmpty()) return Collections.emptyList();

        validarItens(itens);
        List<Long> ids = itens.stream().map(ItemNecessario::getPecaInsumoId).distinct().toList();
        Map<Long, EstoqueItemOutput> estoque = estoqueGateway.findAllByIdForUpdate(ids).stream()
                .collect(Collectors.toMap(EstoqueItemOutput::id, Function.identity()));
        Map<Long, Integer> quantidadesRestantes = new HashMap<>();
        Map<Long, Integer> quantidadesSolicitadas = itens.stream()
                .collect(Collectors.toMap(
                        ItemNecessario::getPecaInsumoId,
                        ItemNecessario::getQuantidade,
                        Integer::sum
                ));
        estoque.forEach((id, item) -> quantidadesRestantes.put(id, item.quantidade()));

        quantidadesSolicitadas.forEach((id, quantidade) -> {
            EstoqueItemOutput peca = estoque.get(id);
            if (peca == null) {
                throw new EstoqueItemNaoEncontradoException(
                        "Peça/Insumo não encontrado com o ID: " + id);
            }
            if (!EstoquePolicy.classificar(peca.quantidade(), quantidade).disponivel()) {
                throw new IllegalStateException(
                        "Estoque insuficiente para iniciar o serviço. Peça/Insumo: " + id
                );
            }
            quantidadesRestantes.put(id,
                    EstoquePolicy.calcularQuantidadeRestante(peca.quantidade(), quantidade));
        });

        List<ItemNecessario> atualizados = new ArrayList<>();
        for (ItemNecessario item : itens) {
            EstoqueItemOutput peca = estoque.get(item.getPecaInsumoId());

            atualizados.add(ItemNecessario.criar(
                    peca.id(),
                    peca.nome(),
                    peca.tipo(),
                    peca.valor(),
                    item.getQuantidade(),
                    StatusItemNecessario.UTILIZADO,
                    new SituacaoEstoque(
                            quantidadesRestantes.get(peca.id()),
                            null
                    )
            ));
        }

        List<EstoqueItemOutput> alteradas = new ArrayList<>();
        quantidadesSolicitadas.forEach((id, ignored) -> {
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

    private void validarItens(List<ItemNecessario> itens) {
        Set<Long> ids = new HashSet<>();
        itens.forEach(item -> {
            if (item == null || item.getPecaInsumoId() == null || item.getQuantidade() == null) {
                throw new IllegalArgumentException("Item necessario e obrigatorio.");
            }
            if (!ids.add(item.getPecaInsumoId())) {
                throw new IllegalArgumentException(
                        "Peça/Insumo duplicado no mesmo serviço: ID " + item.getPecaInsumoId()
                );
            }
        });
    }
}
