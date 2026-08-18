package com.autoflow.infrastructure.persistence.mapper.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.veiculo.Veiculo;
import com.autoflow.infrastructure.persistence.entity.ordemservico.OrdemServicoEntity;
import com.autoflow.infrastructure.persistence.entity.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.mapper.UsuarioPersistenceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class OrdemServicoPersistenceMapper {

    private final ClienteOsPersistenceMapper clienteMapper;
    private final DiagnosticoPersistenceMapper diagnosticoMapper;
    private final ServicoSolicitadoPersistenceMapper servicoMapper;

    @Autowired
    public OrdemServicoPersistenceMapper(
            ClienteOsPersistenceMapper clienteMapper,
            DiagnosticoPersistenceMapper diagnosticoMapper,
            ServicoSolicitadoPersistenceMapper servicoMapper) {
        this.clienteMapper = clienteMapper;
        this.diagnosticoMapper = diagnosticoMapper;
        this.servicoMapper = servicoMapper;
    }

    public OrdemServicoPersistenceMapper(UsuarioPersistenceMapper usuarioMapper) {
        this(new ClienteOsPersistenceMapper(), new DiagnosticoPersistenceMapper(usuarioMapper), new ServicoSolicitadoPersistenceMapper());
    }

    public OrdemServico toDomain(OrdemServicoEntity entity) {
        var servicos = entity.getServicosSolicitados().stream().map(servicoMapper::toDomain).toList();
        VeiculoEntity veiculo = entity.getVeiculo();
        return OrdemServico.reconstituir(
                entity.getId(), entity.getNumeroOs(), clienteMapper.toDomain(entity.getCliente()),
                new Veiculo(veiculo.getId(), veiculo.getPlaca(), veiculo.getMarca(), veiculo.getModelo(), veiculo.getAno()),
                entity.getStatus(), entity.getDataAbertura(), diagnosticoMapper.toDomain(entity.getDiagnostico()), servicos,
                entity.getExecucaoIniciadaEm(), entity.getFinalizadaEm(), entity.getEntregueEm(), entity.getUltimaAtualizacao(),
                entity.getAcompanhamentoTokenHash(), entity.getAcompanhamentoTokenCriadoEm(),
                entity.getAcompanhamentoTokenExpiraEm(), entity.getAcompanhamentoTokenRevogadoEm());
    }

    public OrdemServicoEntity toEntity(OrdemServico domain) {
        OrdemServicoEntity entity = new OrdemServicoEntity();
        entity.setId(domain.getId());
        entity.setNumeroOs(domain.getNumeroOs());
        entity.setCliente(clienteMapper.toEntity(domain.getCliente()));
        entity.setVeiculo(toVehicleReference(domain.getVeiculo()));
        entity.setStatus(domain.getStatus());
        entity.setDataAbertura(domain.getDataAbertura());
        entity.setDiagnostico(diagnosticoMapper.toEntity(domain.getDiagnostico()));
        entity.setExecucaoIniciadaEm(domain.getExecucaoIniciadaEm());
        entity.setFinalizadaEm(domain.getFinalizadaEm());
        entity.setEntregueEm(domain.getEntregueEm());
        entity.setUltimaAtualizacao(domain.getUltimaAtualizacao());
        entity.setAcompanhamentoTokenHash(domain.getAcompanhamentoTokenHash());
        entity.setAcompanhamentoTokenCriadoEm(domain.getAcompanhamentoTokenCriadoEm());
        entity.setAcompanhamentoTokenExpiraEm(domain.getAcompanhamentoTokenExpiraEm());
        entity.setAcompanhamentoTokenRevogadoEm(domain.getAcompanhamentoTokenRevogadoEm());
        var servicos = new ArrayList<ServicoSolicitadoEntity>();
        domain.getServicosSolicitados().forEach(servico -> {
            ServicoSolicitadoEntity servicoEntity = servicoMapper.toEntity(servico);
            servicoEntity.setOrdemServico(entity);
            servicos.add(servicoEntity);
        });
        entity.setServicosSolicitados(servicos);
        return entity;
    }

    public ServicoSolicitado toDomainServico(ServicoSolicitadoEntity entity) {
        return servicoMapper.toDomain(entity);
    }

    public ServicoSolicitadoEntity toEntityServico(ServicoSolicitado domain) {
        return servicoMapper.toEntity(domain);
    }

    private VeiculoEntity toVehicleReference(Veiculo domain) {
        VeiculoEntity entity = new VeiculoEntity();
        entity.setId(domain.id());
        entity.setPlaca(domain.placa());
        entity.setMarca(domain.marca());
        entity.setModelo(domain.modelo());
        entity.setAno(domain.ano());
        return entity;
    }
}
