package com.autoflow.repository.notificacao;

import com.autoflow.domain.notificacao.NotificacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<NotificacaoEntity, Long> {
}