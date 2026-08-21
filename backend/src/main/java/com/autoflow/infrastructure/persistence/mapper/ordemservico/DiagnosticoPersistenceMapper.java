package com.autoflow.infrastructure.persistence.mapper.ordemservico;

import com.autoflow.domain.ordemservico.Diagnostico;
import com.autoflow.infrastructure.persistence.entity.ordemservico.DiagnosticoEntity;
import com.autoflow.infrastructure.persistence.mapper.UsuarioPersistenceMapper;
import org.springframework.stereotype.Component;

@Component
public class DiagnosticoPersistenceMapper {

    private final UsuarioPersistenceMapper usuarioMapper;

    public DiagnosticoPersistenceMapper(UsuarioPersistenceMapper usuarioMapper) {
        this.usuarioMapper = usuarioMapper;
    }

    public Diagnostico toDomain(DiagnosticoEntity entity) {
        if (entity == null) return null;
        Diagnostico domain = new Diagnostico();
        domain.setMecanico(entity.getMecanico() == null ? null : usuarioMapper.toDomain(entity.getMecanico()));
        domain.setIniciadoEm(entity.getIniciadoEm());
        domain.setConcluidoEm(entity.getConcluidoEm());
        domain.setLaudo(entity.getLaudo());
        return domain;
    }

    public DiagnosticoEntity toEntity(Diagnostico domain) {
        if (domain == null) return null;
        DiagnosticoEntity entity = new DiagnosticoEntity();
        entity.setMecanico(domain.getMecanico() == null ? null : usuarioMapper.toEntity(domain.getMecanico()));
        entity.setIniciadoEm(domain.getIniciadoEm());
        entity.setConcluidoEm(domain.getConcluidoEm());
        entity.setLaudo(domain.getLaudo());
        return entity;
    }
}
