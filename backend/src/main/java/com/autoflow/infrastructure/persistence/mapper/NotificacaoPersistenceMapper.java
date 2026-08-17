package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.domain.notificacao.NotificacaoEntity;
import com.autoflow.infrastructure.persistence.entity.notificacao.NotificacaoPersistenceEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoPersistenceMapper {
    public NotificacaoEntity toDomain(
            NotificacaoPersistenceEntity entity) {
        var domain = new NotificacaoEntity();
        domain.setId(entity.getId());
        domain.setOrcamentoId(entity.getOrcamentoId());
        domain.setClienteId(entity.getClienteId());
        domain.setCanal(entity.getCanal());
        domain.setDestinatario(entity.getDestinatario());
        domain.setStatus(entity.getStatus());
        domain.setEnviadaEm(entity.getEnviadaEm());
        domain.setMensagemErro(entity.getMensagemErro());
        domain.setCriadaEm(entity.getCriadaEm());
        return domain;
    }

    public NotificacaoPersistenceEntity toEntity(
            NotificacaoEntity domain) {
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
