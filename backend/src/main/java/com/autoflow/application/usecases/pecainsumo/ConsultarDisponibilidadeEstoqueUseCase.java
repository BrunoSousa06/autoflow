package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.SituacaoEstoque;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultarDisponibilidadeEstoqueUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    public List<ItemNecessarioEntity> execute(List<ItemNecessarioEntity> itensNecessarios) {
        if (itensNecessarios == null || itensNecessarios.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = itensNecessarios.stream()
                .map(ItemNecessarioEntity::getPecaInsumoId)
                .distinct()
                .toList();
        List<PecaInsumoEntity> itensEstoque = pecaInsumoGateway.findAllById(ids);

        Map<Long, PecaInsumoEntity> estoquePorId = itensEstoque.stream()
                .collect(Collectors.toMap(PecaInsumoEntity::getId,
                        Function.identity()));

        return itensNecessarios.stream()
                .map(itemSolicitado -> {
                    PecaInsumoEntity estoque = estoquePorId.get(itemSolicitado.getPecaInsumoId());
                    if (estoque == null) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Peça/Insumo não encontrado com o ID: "
                                        + itemSolicitado.getPecaInsumoId()
                        );
                    }
                    boolean disponivel = estoque.getQuantidade() >= itemSolicitado.getQuantidade();

                    StatusItemNecessario status = disponivel
                            ? StatusItemNecessario.DISPONIVEL
                            : StatusItemNecessario.PENDENTE;

                    MotivoPendenciaItem motivoPendencia = disponivel
                            ? null
                            : MotivoPendenciaItem.ESTOQUE_INSUFICIENTE;

                    return ItemNecessarioEntity.criar(
                            estoque.getId(),
                            estoque.getNome(),
                            estoque.getTipo(),
                            estoque.getValor(),
                            itemSolicitado.getQuantidade(),
                            status,
                            new SituacaoEstoque(
                                    estoque.getQuantidade(),
                                    motivoPendencia
                            )
                    );
                }).toList();
    }
}
