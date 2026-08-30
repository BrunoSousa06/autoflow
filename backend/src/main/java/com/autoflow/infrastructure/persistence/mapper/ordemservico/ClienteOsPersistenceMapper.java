package com.autoflow.infrastructure.persistence.mapper.ordemservico;

import com.autoflow.domain.ordemservico.ClienteOs;
import com.autoflow.infrastructure.persistence.entity.ordemservico.ClienteOsEntity;
import org.springframework.stereotype.Component;

@Component
public class ClienteOsPersistenceMapper {

    public ClienteOs toDomain(ClienteOsEntity entity) {
        return ClienteOs.fromFields(entity.getId(), entity.getNome(), entity.getCpfCnpj(), entity.getEmail(), entity.getTelefone());
    }

    public ClienteOsEntity toEntity(ClienteOs domain) {
        ClienteOsEntity entity = new ClienteOsEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setCpfCnpj(domain.getCpfCnpj());
        entity.setEmail(domain.getEmail());
        entity.setTelefone(domain.getTelefone());
        return entity;
    }
}
