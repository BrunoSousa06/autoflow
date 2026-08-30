package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.application.input.pecainsumo.PecaInsumoInput;
import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;
import com.autoflow.infrastructure.persistence.entity.pecainsumo.PecaInsumoEntity;
import org.springframework.stereotype.Component;

@Component
public class PecaInsumoPersistenceMapper {

    public PecaInsumoEntity toEntity(PecaInsumoInput input) {
        PecaInsumoEntity entity = new PecaInsumoEntity();
        updateEntity(input, entity);
        return entity;
    }

    public void updateEntity(PecaInsumoInput input, PecaInsumoEntity entity) {
        entity.setNome(input.nome());
        entity.setValor(input.valor());
        entity.setQuantidade(input.quantidade());
        entity.setTipo(input.tipo());
    }

    public PecaInsumoOutput toOutput(PecaInsumoEntity entity) {
        return new PecaInsumoOutput(entity.getId(), entity.getNome(), entity.getValor(),
                entity.getQuantidade(), entity.getTipo());
    }
}
