package com.autoflow.service.ordemServico;

import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.repository.ordemServico.OrdemServicoRepository;
import org.junit.jupiter.api.Test;

import java.lang.Long;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrdemServicoEntityServiceTest {

    @Test
    void deveCriarESalvarOrdemServico() {
        OrdemServicoRepository repository = mock(OrdemServicoRepository.class);
        OrdemServicoService service = new OrdemServicoService(repository);
        Long clienteId = 1L;
        Long veiculoId = 1L;
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Revisao");
        OrdemServicoEntity ordemServicoEntitySalva = OrdemServicoEntity.criar(clienteId, veiculoId, List.of(servico));
        when(repository.save(any(OrdemServicoEntity.class))).thenReturn(ordemServicoEntitySalva);

        OrdemServicoEntity ordemServicoEntity = service.criar(clienteId, veiculoId, List.of(servico));

        assertEquals(clienteId, ordemServicoEntity.getClienteId());
        assertEquals(veiculoId, ordemServicoEntity.getVeiculoId());
        assertEquals(List.of(servico), ordemServicoEntity.getServicosSolicitados());
        verify(repository).save(any(OrdemServicoEntity.class));
    }
}
