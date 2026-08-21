package com.autoflow.infrastructure.persistence.mapper.ordemservico;

import com.autoflow.domain.ordemservico.ItemNecessario;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.infrastructure.persistence.entity.ordemservico.ItemNecessarioEntity;
import com.autoflow.infrastructure.persistence.entity.ordemservico.ServicoSolicitadoEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ServicoSolicitadoPersistenceMapper {

    public ServicoSolicitado toDomain(ServicoSolicitadoEntity entity) {
        ServicoSolicitado domain = new ServicoSolicitado();
        domain.setId(entity.getId());
        domain.setServicoId(entity.getServicoId());
        domain.setNome(entity.getNome());
        domain.setValor(entity.getValor());
        domain.setStatus(entity.getStatus());
        domain.setIniciadoEm(entity.getIniciadoEm());
        domain.setFinalizadoEm(entity.getFinalizadoEm());
        domain.setItensNecessarios(entity.getItensNecessarios().stream().map(this::toDomain).toList());
        return domain;
    }

    public ServicoSolicitadoEntity toEntity(ServicoSolicitado domain) {
        ServicoSolicitadoEntity entity = new ServicoSolicitadoEntity();
        entity.setId(domain.getId());
        entity.setServicoId(domain.getServicoId());
        entity.setNome(domain.getNome());
        entity.setValor(domain.getValor());
        entity.setStatus(domain.getStatus());
        entity.setIniciadoEm(domain.getIniciadoEm());
        entity.setFinalizadoEm(domain.getFinalizadoEm());
        entity.setItensNecessarios(new ArrayList<>(domain.getItensNecessarios().stream().map(this::toEntity).toList()));
        return entity;
    }

    private ItemNecessario toDomain(ItemNecessarioEntity entity) {
        ItemNecessario item = new ItemNecessario();
        item.setPecaInsumoId(entity.getPecaInsumoId());
        item.setNome(entity.getNome());
        item.setTipo(entity.getTipo());
        item.setValorUnitario(entity.getValorUnitario());
        item.setQuantidade(entity.getQuantidade());
        item.setValorTotal(entity.getValorTotal());
        item.setStatus(entity.getStatus());
        item.setMotivoPendencia(entity.getMotivoPendencia());
        item.setQuantidadeDisponivel(entity.getQuantidadeDisponivel());
        item.setMensagemStatus(entity.getMensagemStatus());
        return item;
    }

    private ItemNecessarioEntity toEntity(ItemNecessario domain) {
        ItemNecessarioEntity entity = new ItemNecessarioEntity();
        entity.setPecaInsumoId(domain.getPecaInsumoId());
        entity.setNome(domain.getNome());
        entity.setTipo(domain.getTipo());
        entity.setValorUnitario(domain.getValorUnitario());
        entity.setQuantidade(domain.getQuantidade());
        entity.setValorTotal(domain.getValorTotal());
        entity.setStatus(domain.getStatus());
        entity.setMotivoPendencia(domain.getMotivoPendencia());
        entity.setQuantidadeDisponivel(domain.getQuantidadeDisponivel());
        entity.setMensagemStatus(domain.getMensagemStatus());
        return entity;
    }
}
