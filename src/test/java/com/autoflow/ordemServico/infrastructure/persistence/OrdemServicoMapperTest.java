package com.autoflow.ordemServico.infrastructure.persistence;

import com.autoflow.ordemServico.domain.OrdemServico;
import com.autoflow.ordemServico.domain.ServicoSolicitado;
import com.autoflow.ordemServico.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrdemServicoMapperTest {

    private final OrdemServicoMapper mapper = Mappers.getMapper(OrdemServicoMapper.class);

    @Test
    void deveConverterDominioParaEntityEVoltar() {
        ServicoSolicitado servico = new ServicoSolicitado(UUID.randomUUID(), "Revisao");
        OrdemServico ordemServico = OrdemServico.criar(UUID.randomUUID(), UUID.randomUUID(), List.of(servico));

        OrdemServicoEntity entity = mapper.toEntity(ordemServico);
        OrdemServico dominioRestaurado = mapper.toDomain(entity);

        assertEquals(ordemServico.getId(), entity.getId());
        assertEquals(ordemServico.getNumeroOs(), entity.getNumeroOs());
        assertEquals(StatusOrdemServico.RECEBIDA, entity.getStatus());
        assertEquals(servico.servicoId(), entity.getServicosSolicitados().getFirst().getServicoId());
        assertEquals(ordemServico.getId(), dominioRestaurado.getId());
        assertEquals(ordemServico.getNumeroOs(), dominioRestaurado.getNumeroOs());
        assertEquals(servico, dominioRestaurado.getServicosSolicitados().getFirst());
    }
}
