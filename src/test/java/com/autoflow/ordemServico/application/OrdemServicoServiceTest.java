package com.autoflow.ordemServico.application;

import com.autoflow.ordemServico.domain.OrdemServico;
import com.autoflow.ordemServico.domain.ServicoSolicitado;
import com.autoflow.ordemServico.infrastructure.persistence.OrdemServicoEntity;
import com.autoflow.ordemServico.infrastructure.persistence.OrdemServicoJpaRepository;
import com.autoflow.ordemServico.infrastructure.persistence.OrdemServicoMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrdemServicoServiceTest {

    @Test
    void deveCriarESalvarOrdemServico() {
        OrdemServicoJpaRepository repository = mock(OrdemServicoJpaRepository.class);
        OrdemServicoMapper mapper = mock(OrdemServicoMapper.class);
        OrdemServicoService service = new OrdemServicoService(repository, mapper);
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        ServicoSolicitado servico = new ServicoSolicitado(UUID.randomUUID(), "Revisao");
        OrdemServicoEntity entity = mock(OrdemServicoEntity.class);
        OrdemServico ordemServicoSalva = OrdemServico.criar(clienteId, veiculoId, List.of(servico));
        when(mapper.toEntity(any(OrdemServico.class))).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(ordemServicoSalva);

        OrdemServico ordemServico = service.criar(clienteId, veiculoId, List.of(servico));

        assertEquals(clienteId, ordemServico.getClienteId());
        assertEquals(veiculoId, ordemServico.getVeiculoId());
        assertEquals(List.of(servico), ordemServico.getServicosSolicitados());
        verify(mapper).toEntity(any(OrdemServico.class));
        verify(repository).save(entity);
        verify(mapper).toDomain(entity);
    }
}
