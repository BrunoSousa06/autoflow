package com.autoflow.repository.ordemservico.historico;

import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoStatusOsRepository extends JpaRepository<HistoricoStatusOsEntity, Long> {

    List<HistoricoStatusOsEntity> findByOrdemServicoIdOrderByRegistradoEmAsc(Long ordemServicoId);
    List<HistoricoStatusOsEntity> findByNumeroOsOrderByRegistradoEmAsc(String numeroOs);
}
