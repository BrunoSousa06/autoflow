package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.EstoqueItemOutput;
import com.autoflow.application.exception.EstoqueItemNaoEncontradoException;
import com.autoflow.application.gateway.EstoqueGateway;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.SituacaoEstoque;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.EstoqueDisponibilidade;
import com.autoflow.domain.pecainsumo.EstoquePolicy;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class ConsultarDisponibilidadeEstoqueUseCase {

    private final EstoqueGateway estoqueGateway;

    public List<ItemNecessarioEntity> execute(List<ItemNecessarioEntity> itensNecessarios) {
        if (itensNecessarios == null || itensNecessarios.isEmpty()) {
            return Collections.emptyList();
        }

        validarItens(itensNecessarios);
        List<Long> ids = itensNecessarios.stream()
                .map(ItemNecessarioEntity::getPecaInsumoId)
                .distinct()
                .toList();
        List<EstoqueItemOutput> itensEstoque = estoqueGateway.findAllById(ids);

        Map<Long, EstoqueItemOutput> estoquePorId = itensEstoque.stream()
                .collect(Collectors.toMap(EstoqueItemOutput::id,
                        Function.identity()));

        return itensNecessarios.stream()
                .map(itemSolicitado -> {
                    EstoqueItemOutput estoque = estoquePorId.get(itemSolicitado.getPecaInsumoId());
                    if (estoque == null) {
                        throw new EstoqueItemNaoEncontradoException(
                                "Peça/Insumo não encontrado com o ID: "
                                        + itemSolicitado.getPecaInsumoId()
                        );
                    }
                    validarQuantidadeSolicitada(itemSolicitado);
                    EstoqueDisponibilidade disponibilidade = EstoquePolicy.classificar(
                            estoque.quantidade(),
                            itemSolicitado.getQuantidade()
                    );

                    StatusItemNecessario status = disponibilidade.disponivel()
                            ? StatusItemNecessario.DISPONIVEL
                            : StatusItemNecessario.PENDENTE;

                    MotivoPendenciaItem motivoPendencia = disponibilidade.disponivel()
                            ? null
                            : MotivoPendenciaItem.ESTOQUE_INSUFICIENTE;

                    return ItemNecessarioEntity.criar(
                            estoque.id(),
                            estoque.nome(),
                            estoque.tipo(),
                            estoque.valor(),
                            itemSolicitado.getQuantidade(),
                            status,
                            new SituacaoEstoque(
                                    disponibilidade.quantidadeDisponivel(),
                                    motivoPendencia
                            )
                    );
                }).toList();
    }

    private void validarQuantidadeSolicitada(ItemNecessarioEntity item) {
        if (item.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
        }
    }

    private void validarItens(List<ItemNecessarioEntity> itens) {
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
