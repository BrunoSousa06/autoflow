package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.exception.OrdemServicoNaoEncontradaException;
import com.autoflow.application.gateway.AcompanhamentoPublicoGateway;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.acompanhamento.AcessoAcompanhamento;
import com.autoflow.infrastructure.persistence.entity.orcamento.OrcamentoPersistenceEntity;
import com.autoflow.infrastructure.persistence.entity.ordemservico.OrdemServicoEntity;
import com.autoflow.infrastructure.persistence.mapper.ordemservico.OrdemServicoPersistenceMapper;
import com.autoflow.infrastructure.persistence.repository.OrcamentoRepository;
import com.autoflow.infrastructure.persistence.repository.OrdemServicoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AcompanhamentoPublicoRepositoryAdapter
        implements AcompanhamentoPublicoGateway {

    private final OrdemServicoRepository repository;
    private final OrcamentoRepository orcamentoRepository;
    private final OrdemServicoPersistenceMapper ordemServicoMapper;

    @Override
    @Transactional
    public void salvar(
            Long ordemServicoId,
            AcessoAcompanhamento acesso
    ) {
        OrdemServicoEntity ordemServicoEntity = repository
                .findById(ordemServicoId)
                .orElseThrow(
                        () -> new OrdemServicoNaoEncontradaException(
                                ordemServicoId
                        )
                );
        OrdemServico ordemServico = ordemServicoMapper.toDomain(ordemServicoEntity);
        ordemServico.configurarAcompanhamentoPublico(
                acesso.tokenHash(),
                acesso.criadoEm(),
                acesso.expiraEm()
        );

        repository.save(ordemServicoMapper.toEntity(ordemServico));
    }

    @Override
    @Transactional
    public Optional<DadosAcompanhamentoPublico>
    buscarPorTokenHash(String tokenHash) {
        return repository
                .findByAcompanhamentoTokenHash(tokenHash)
                .map(this::toDados);
    }

    private DadosAcompanhamentoPublico toDados(
            OrdemServicoEntity ordemServicoEntity
    ) {
        OrdemServico ordemServico = ordemServicoMapper.toDomain(ordemServicoEntity);
        AcessoAcompanhamento acesso =
                new AcessoAcompanhamento(
                        ordemServico.getAcompanhamentoTokenHash(),
                        ordemServico.getAcompanhamentoTokenCriadoEm(),
                        ordemServico.getAcompanhamentoTokenExpiraEm(),
                        ordemServico.getAcompanhamentoTokenRevogadoEm()
                );

        return new DadosAcompanhamentoPublico(
                ordemServico.getNumeroOs(),
                ordemServico.getStatus(),
                ordemServico.getDataAbertura(),
                ordemServico.getExecucaoIniciadaEm(),
                ordemServico.getFinalizadaEm(),
                ordemServico.getEntregueEm(),
                orcamentoRepository.findTopByNumeroOsOrderByVersaoDesc(ordemServico.getNumeroOs())
                        .filter(orcamento -> orcamento.getStatus() == StatusOrcamento.DISPONIVEL)
                        .map(OrcamentoPersistenceEntity::getId)
                        .orElse(null),
                acesso
        );
    }
}
