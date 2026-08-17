package com.autoflow.infrastructure.persistence.mapper;

import com.autoflow.domain.orcamento.ClienteOrcamentoSnapshot;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.OrcamentoItemNecessarioEntity;
import com.autoflow.domain.orcamento.OrcamentoServicoEntity;
import com.autoflow.domain.orcamento.VeiculoOrcamentoSnapshot;
import com.autoflow.infrastructure.persistence.entity.orcamento.ClienteOrcamentoSnapshotPersistenceEntity;
import com.autoflow.infrastructure.persistence.entity.orcamento.OrcamentoItemNecessarioPersistenceEntity;
import com.autoflow.infrastructure.persistence.entity.orcamento.OrcamentoPersistenceEntity;
import com.autoflow.infrastructure.persistence.entity.orcamento.OrcamentoServicoPersistenceEntity;
import com.autoflow.infrastructure.persistence.entity.orcamento.VeiculoOrcamentoSnapshotPersistenceEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class OrcamentoPersistenceMapper {

    public OrcamentoEntity toDomain(
            OrcamentoPersistenceEntity entity) {
        return OrcamentoEntity.builder()
                .id(entity.getId())
                .ordemServicoId(entity.getOrdemServicoId())
                .numeroOs(entity.getNumeroOs())
                .tipo(entity.getTipo())
                .versao(entity.getVersao())
                .status(entity.getStatus())
                .criadoEm(entity.getCriadoEm())
                .disponibilizadoEm(entity.getDisponibilizadoEm())
                .totalServicos(entity.getTotalServicos())
                .totalItens(entity.getTotalItens())
                .totalGeral(entity.getTotalGeral())
                .publicTokenHash(entity.getPublicTokenHash())
                .publicTokenExpiraEm(entity.getPublicTokenExpiraEm())
                .aprovadoEm(entity.getAprovadoEm())
                .reprovadoEm(entity.getReprovadoEm())
                .assinaturaNome(entity.getAssinaturaNome())
                .recusaMotivo(entity.getRecusaMotivo())
                .servicos(entity.getServicos() == null ? null : new ArrayList<>(entity.getServicos().stream()
                        .map(this::toDomain)
                        .toList()))
                .itens(entity.getItens() == null ? null : new ArrayList<>(entity.getItens().stream()
                        .map(this::toDomain)
                        .toList()))
                .cliente(toDomain(entity.getCliente()))
                .veiculo(toDomain(entity.getVeiculo()))
                .build();
    }

    public OrcamentoPersistenceEntity toEntity(
            OrcamentoEntity domain) {
        var entity = new OrcamentoPersistenceEntity();
        entity.setId(domain.getId());
        entity.setOrdemServicoId(domain.getOrdemServicoId());
        entity.setNumeroOs(domain.getNumeroOs());
        entity.setTipo(domain.getTipo());
        entity.setVersao(domain.getVersao());
        entity.setStatus(domain.getStatus());
        entity.setCriadoEm(domain.getCriadoEm());
        entity.setDisponibilizadoEm(domain.getDisponibilizadoEm());
        entity.setTotalServicos(domain.getTotalServicos());
        entity.setTotalItens(domain.getTotalItens());
        entity.setTotalGeral(domain.getTotalGeral());
        entity.setPublicTokenHash(domain.getPublicTokenHash());
        entity.setPublicTokenExpiraEm(domain.getPublicTokenExpiraEm());
        entity.setAprovadoEm(domain.getAprovadoEm());
        entity.setReprovadoEm(domain.getReprovadoEm());
        entity.setAssinaturaNome(domain.getAssinaturaNome());
        entity.setRecusaMotivo(domain.getRecusaMotivo());
        entity.setServicos(domain.getServicos() == null ? null : new ArrayList<>(domain.getServicos().stream()
                .map(this::toEntity)
                .toList()));
        entity.setItens(domain.getItens() == null ? null : new ArrayList<>(domain.getItens().stream()
                .map(this::toEntity)
                .toList()));
        entity.setCliente(toEntity(domain.getCliente()));
        entity.setVeiculo(toEntity(domain.getVeiculo()));
        return entity;
    }

    private ClienteOrcamentoSnapshot toDomain(
            ClienteOrcamentoSnapshotPersistenceEntity entity) {
        if (entity == null) return null;
        return ClienteOrcamentoSnapshot.builder()
                .nome(entity.getNome())
                .cpfCnpj(entity.getCpfCnpj())
                .email(entity.getEmail())
                .telefone(entity.getTelefone())
                .build();
    }

    private ClienteOrcamentoSnapshotPersistenceEntity toEntity(
            ClienteOrcamentoSnapshot domain) {
        if (domain == null) return null;
        var entity = new ClienteOrcamentoSnapshotPersistenceEntity();
        entity.setNome(domain.getNome());
        entity.setCpfCnpj(domain.getCpfCnpj());
        entity.setEmail(domain.getEmail());
        entity.setTelefone(domain.getTelefone());
        return entity;
    }

    private VeiculoOrcamentoSnapshot toDomain(
            VeiculoOrcamentoSnapshotPersistenceEntity entity) {
        if (entity == null) return null;
        return VeiculoOrcamentoSnapshot.builder()
                .placa(entity.getPlaca())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .ano(entity.getAno())
                .build();
    }

    private VeiculoOrcamentoSnapshotPersistenceEntity toEntity(
            VeiculoOrcamentoSnapshot domain) {
        if (domain == null) return null;
        var entity = new VeiculoOrcamentoSnapshotPersistenceEntity();
        entity.setPlaca(domain.getPlaca());
        entity.setMarca(domain.getMarca());
        entity.setModelo(domain.getModelo());
        entity.setAno(domain.getAno());
        return entity;
    }

    private OrcamentoServicoEntity toDomain(
            OrcamentoServicoPersistenceEntity entity) {
        return OrcamentoServicoEntity.builder()
                .servicoId(entity.getServicoId())
                .nome(entity.getNome())
                .valor(entity.getValor())
                .build();
    }

    private OrcamentoServicoPersistenceEntity toEntity(
            OrcamentoServicoEntity domain) {
        var entity = new OrcamentoServicoPersistenceEntity();
        entity.setServicoId(domain.getServicoId());
        entity.setNome(domain.getNome());
        entity.setValor(domain.getValor());
        return entity;
    }

    private OrcamentoItemNecessarioEntity toDomain(
            OrcamentoItemNecessarioPersistenceEntity entity) {
        return OrcamentoItemNecessarioEntity.builder()
                .pecaInsumoId(entity.getPecaInsumoId())
                .servicoOsId(entity.getServicoOsId())
                .nome(entity.getNome())
                .tipo(entity.getTipo())
                .valorUnitario(entity.getValorUnitario())
                .quantidade(entity.getQuantidade())
                .valorTotal(entity.getValorTotal())
                .build();
    }

    private OrcamentoItemNecessarioPersistenceEntity toEntity(
            OrcamentoItemNecessarioEntity domain) {
        var entity = new OrcamentoItemNecessarioPersistenceEntity();
        entity.setPecaInsumoId(domain.getPecaInsumoId());
        entity.setServicoOsId(domain.getServicoOsId());
        entity.setNome(domain.getNome());
        entity.setTipo(domain.getTipo());
        entity.setValorUnitario(domain.getValorUnitario());
        entity.setQuantidade(domain.getQuantidade());
        entity.setValorTotal(domain.getValorTotal());
        return entity;
    }
}
