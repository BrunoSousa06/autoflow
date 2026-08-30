package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.infrastructure.persistence.entity.ordemservico.ServicoSolicitadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ServicoSolicitadoRepository extends JpaRepository<ServicoSolicitadoEntity, Long> {

    @Query(value = """
            SELECT
                oss.servico_id AS servicoId,
                COALESCE(s.nome, oss.nome) AS nomeServico,
                COUNT(*) AS quantidadeExecucoes,
                AVG(EXTRACT(EPOCH FROM (oss.finalizado_em - oss.iniciado_em))) AS tempoMedioSegundos
            FROM ordem_servico_servico_solicitado oss
            LEFT JOIN servicos s ON s.id = oss.servico_id
            WHERE oss.status = 'FINALIZADO'
              AND oss.iniciado_em IS NOT NULL
              AND oss.finalizado_em IS NOT NULL
            GROUP BY oss.servico_id, COALESCE(s.nome, oss.nome)
            ORDER BY nomeServico
            """, nativeQuery = true)
    List<TempoMedioServicoProjection> calcularTempoMedioPorServico();
}
