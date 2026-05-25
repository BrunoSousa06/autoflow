package com.autoflow.service.ordemServico;

import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.repository.ordemServico.OrdemServicoRepository;
import org.springframework.stereotype.Service;

import java.lang.Long;
import java.util.List;


@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
    }

    public OrdemServicoEntity criar(
            Long clienteId,
            Long veiculoId,
            List<ServicoSolicitadoEntity> servicosSolicitados
    ) {
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(clienteId, veiculoId, servicosSolicitados);
        return ordemServicoRepository.save(ordemServicoEntity);
    }
}
