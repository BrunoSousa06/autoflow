package com.autoflow.ordemServico.infrastructure.persistence;

import com.autoflow.ordemServico.domain.OrdemServico;
import com.autoflow.ordemServico.domain.ServicoSolicitado;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrdemServicoMapper {

    OrdemServicoEntity toEntity(OrdemServico ordemServico);

    ServicoSolicitadoEmbeddable toEmbeddable(ServicoSolicitado servicoSolicitado);

    default OrdemServico toDomain(OrdemServicoEntity entity) {
        if (entity == null) {
            return null;
        }

        return OrdemServico.restaurar(
                entity.getId(),
                entity.getNumeroOs(),
                entity.getClienteId(),
                entity.getVeiculoId(),
                entity.getStatus(),
                entity.getDataAbertura(),
                toDomain(entity.getServicosSolicitados())
        );
    }

    default ServicoSolicitado toDomain(ServicoSolicitadoEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }

        return new ServicoSolicitado(
                embeddable.getServicoId(),
                embeddable.getNome()
        );
    }

    List<ServicoSolicitado> toDomain(List<ServicoSolicitadoEmbeddable> servicosSolicitados);
}
