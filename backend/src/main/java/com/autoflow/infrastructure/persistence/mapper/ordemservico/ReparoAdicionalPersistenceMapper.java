package com.autoflow.infrastructure.persistence.mapper.ordemservico;

import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import com.autoflow.infrastructure.persistence.entity.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.infrastructure.persistence.entity.ordemservico.reparoadicional.ReparoAdicionalEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ReparoAdicionalPersistenceMapper {

    private final OrdemServicoPersistenceMapper ordemServicoMapper;

    public ReparoAdicionalPersistenceMapper(OrdemServicoPersistenceMapper ordemServicoMapper) {
        this.ordemServicoMapper = ordemServicoMapper;
    }

    public ReparoAdicional toDomain(ReparoAdicionalEntity entity) {
        ReparoAdicional domain = new ReparoAdicional();
        domain.setId(entity.getId()); domain.setOrdemServicoId(entity.getOrdemServicoId()); domain.setNumeroOs(entity.getNumeroOs());
        domain.setMecanicoId(entity.getMecanicoId()); domain.setOrcamentoId(entity.getOrcamentoId()); domain.setStatus(entity.getStatus());
        domain.setCriadoEm(entity.getCriadoEm()); domain.setAprovadoEm(entity.getAprovadoEm()); domain.setRecusadoEm(entity.getRecusadoEm());
        domain.setMotivoRecusa(entity.getMotivoRecusa());
        var servicos = new ArrayList<ServicoSolicitado>();
        for (ServicoSolicitadoEntity servico : entity.getServicos()) {
            servicos.add(ordemServicoMapper.toDomainServico(servico));
        }
        domain.setServicos(servicos);
        servicos.forEach(servico -> servico.setReparoAdicional(domain));
        return domain;
    }

    public ReparoAdicionalEntity toEntity(ReparoAdicional domain) {
        ReparoAdicionalEntity entity = new ReparoAdicionalEntity();
        entity.setId(domain.getId()); entity.setOrdemServicoId(domain.getOrdemServicoId()); entity.setNumeroOs(domain.getNumeroOs());
        entity.setMecanicoId(domain.getMecanicoId()); entity.setOrcamentoId(domain.getOrcamentoId()); entity.setStatus(domain.getStatus());
        entity.setCriadoEm(domain.getCriadoEm()); entity.setAprovadoEm(domain.getAprovadoEm()); entity.setRecusadoEm(domain.getRecusadoEm());
        entity.setMotivoRecusa(domain.getMotivoRecusa());
        var servicos = new ArrayList<ServicoSolicitadoEntity>();
        for (ServicoSolicitado servico : domain.getServicos()) {
            ServicoSolicitadoEntity servicoEntity = ordemServicoMapper.toEntityServico(servico);
            servicoEntity.setReparoAdicional(entity);
            servicos.add(servicoEntity);
        }
        entity.setServicos(servicos);
        return entity;
    }
}
