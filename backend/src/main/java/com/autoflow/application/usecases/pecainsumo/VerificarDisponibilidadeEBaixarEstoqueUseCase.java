package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.SituacaoEstoque;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.service.pecainsumo.BaixaEstoqueResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VerificarDisponibilidadeEBaixarEstoqueUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    public BaixaEstoqueResult execute(
            List<ItemNecessarioEntity> itensNecessarios) {

        if (itensNecessarios == null || itensNecessarios.isEmpty()) {
            return new BaixaEstoqueResult(Collections.emptyList());
        }

        List<Long> ids = itensNecessarios.stream()
                .map(ItemNecessarioEntity::getPecaInsumoId)
                .distinct()
                .toList();

        Map<Long, PecaInsumoEntity> estoquePorId =
                pecaInsumoGateway.findAllById(ids)
                        .stream()
                        .collect(Collectors.toMap(
                                PecaInsumoEntity::getId,
                                Function.identity()));

        List<PecaInsumoEntity> alterados = new ArrayList<>();

        List<ItemNecessarioEntity> atualizados =
                itensNecessarios.stream()
                        .map(item -> {

                            PecaInsumoEntity estoque =
                                    estoquePorId.get(item.getPecaInsumoId());

                            if (estoque == null) {
                                throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Peça/Insumo não encontrado com o ID: "
                                                + item.getPecaInsumoId());
                            }

                            boolean disponivel =
                                    estoque.getQuantidade() >= item.getQuantidade();

                            if (disponivel) {
                                estoque.setQuantidade(
                                        estoque.getQuantidade() - item.getQuantidade());

                                alterados.add(estoque);
                            }

                            return ItemNecessarioEntity.criar(
                                    estoque.getId(),
                                    estoque.getNome(),
                                    estoque.getTipo(),
                                    estoque.getValor(),
                                    item.getQuantidade(),
                                    disponivel
                                            ? StatusItemNecessario.UTILIZADO
                                            : StatusItemNecessario.PENDENTE,
                                    new SituacaoEstoque(
                                            estoque.getQuantidade(),
                                            disponivel
                                                    ? null
                                                    : MotivoPendenciaItem.ESTOQUE_INSUFICIENTE
                                    )
                            );
                        })
                        .toList();

        pecaInsumoGateway.saveAll(alterados);

        return new BaixaEstoqueResult(atualizados);
    }
}
