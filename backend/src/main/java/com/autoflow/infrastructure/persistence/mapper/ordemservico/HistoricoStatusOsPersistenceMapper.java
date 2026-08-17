package com.autoflow.infrastructure.persistence.mapper.ordemservico;

import com.autoflow.domain.ordemservico.HistoricoStatusOs;
import com.autoflow.infrastructure.persistence.entity.ordemservico.HistoricoStatusOsEntity;
import org.springframework.stereotype.Component;

@Component
public class HistoricoStatusOsPersistenceMapper {
    public HistoricoStatusOs toDomain(HistoricoStatusOsEntity entity) {
        HistoricoStatusOs domain = new HistoricoStatusOs();
        domain.setId(entity.getId()); domain.setOrdemServicoId(entity.getOrdemServicoId()); domain.setNumeroOs(entity.getNumeroOs());
        domain.setStatus(entity.getStatus()); domain.setRegistradoEm(entity.getRegistradoEm()); domain.setMensagemCliente(entity.getMensagemCliente());
        return domain;
    }

    public HistoricoStatusOsEntity toEntity(HistoricoStatusOs domain) {
        HistoricoStatusOsEntity entity = new HistoricoStatusOsEntity();
        entity.setId(domain.getId()); entity.setOrdemServicoId(domain.getOrdemServicoId()); entity.setNumeroOs(domain.getNumeroOs());
        entity.setStatus(domain.getStatus()); entity.setRegistradoEm(domain.getRegistradoEm()); entity.setMensagemCliente(domain.getMensagemCliente());
        return entity;
    }
}
