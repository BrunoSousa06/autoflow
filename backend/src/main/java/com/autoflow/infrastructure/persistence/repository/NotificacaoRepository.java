package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.infrastructure.persistence.entity.notificacao.NotificacaoPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<NotificacaoPersistenceEntity, Long> {
}
