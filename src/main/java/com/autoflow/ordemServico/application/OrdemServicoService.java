package com.autoflow.ordemServico.application;

import com.autoflow.ordemServico.domain.OrdemServico;
import com.autoflow.ordemServico.domain.ServicoSolicitado;
import com.autoflow.ordemServico.infrastructure.persistence.OrdemServicoJpaRepository;
import com.autoflow.ordemServico.infrastructure.persistence.OrdemServicoMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrdemServicoService {

    private final OrdemServicoJpaRepository ordemServicoJpaRepository;
    private final OrdemServicoMapper ordemServicoMapper;

    public OrdemServicoService(
            OrdemServicoJpaRepository ordemServicoJpaRepository,
            OrdemServicoMapper ordemServicoMapper
    ) {
        this.ordemServicoJpaRepository = ordemServicoJpaRepository;
        this.ordemServicoMapper = ordemServicoMapper;
    }

    public OrdemServico criar(
            UUID clienteId,
            UUID veiculoId,
            List<ServicoSolicitado> servicosSolicitados
    ) {
        OrdemServico ordemServico = OrdemServico.criar(clienteId, veiculoId, servicosSolicitados);
        return ordemServicoMapper.toDomain(
                ordemServicoJpaRepository.save(ordemServicoMapper.toEntity(ordemServico))
        );
    }
}
