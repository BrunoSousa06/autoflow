package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.domain.notificacao.Notificacao;
import com.autoflow.infrastructure.persistence.entity.notificacao.NotificacaoPersistenceEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoPersistenceMapper {
    public Notificacao toDomain(
            NotificacaoPersistenceEntity entity) {
        return Notificacao.reconstituir(
                entity.getId(),
                entity.getOrcamentoId(),
                entity.getClienteId(),
                entity.getCanal(),
                entity.getDestinatario(),
                entity.getStatus(),
                entity.getEnviadaEm(),
                entity.getMensagemErro(),
                entity.getCriadaEm());
    }

    public NotificacaoPersistenceEntity toEntity(
            Notificacao domain) {
        var entity = new NotificacaoPersistenceEntity();
        entity.setId(domain.getId());
        entity.setOrcamentoId(domain.getOrcamentoId());
        entity.setClienteId(domain.getClienteId());
        entity.setCanal(domain.getCanal());
        entity.setDestinatario(domain.getDestinatario());
        entity.setStatus(domain.getStatus());
        entity.setEnviadaEm(domain.getEnviadaEm());
        entity.setMensagemErro(domain.getMensagemErro());
        entity.setCriadaEm(domain.getCriadaEm());
        return entity;
    }
}
