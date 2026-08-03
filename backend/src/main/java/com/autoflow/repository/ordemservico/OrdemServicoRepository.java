package com.autoflow.repository.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface OrdemServicoRepository extends JpaRepository<OrdemServicoEntity, Long>, JpaSpecificationExecutor<OrdemServicoEntity> {

    List<OrdemServicoEntity> findByCliente_IdOrderByDataAberturaDesc(Long clienteId);

    List<OrdemServicoEntity> findAllByOrderByDataAberturaDesc();

    Optional<OrdemServicoEntity> findByNumeroOs(String numeroOs);

    @Query(value = """
        SELECT
            COUNT(*) AS quantidadeOrdensFinalizadas,
            AVG(EXTRACT(EPOCH FROM (finalizada_em - execucao_iniciada_em))) AS tempoMedioSegundos
        FROM ordem_servico
        WHERE status IN ('FINALIZADA', 'ENTREGUE')
          AND execucao_iniciada_em IS NOT NULL
          AND finalizada_em IS NOT NULL
        """, nativeQuery = true)
    TempoMedioOrdemServicoProjection calcularTempoMedioFinalizacao();

    Optional<OrdemServicoEntity> findByAcompanhamentoTokenHash(
            String acompanhamentoTokenHash
    );

}
