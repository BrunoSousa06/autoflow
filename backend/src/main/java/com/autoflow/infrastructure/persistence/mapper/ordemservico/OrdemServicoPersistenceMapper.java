package com.autoflow.infrastructure.persistence.mapper.ordemservico;

import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import com.autoflow.infrastructure.persistence.entity.ordemservico.ClienteOsEntity;
import com.autoflow.infrastructure.persistence.entity.ordemservico.DiagnosticoEntity;
import com.autoflow.infrastructure.persistence.entity.ordemservico.ItemNecessarioEntity;
import com.autoflow.infrastructure.persistence.entity.ordemservico.OrdemServicoEntity;
import com.autoflow.infrastructure.persistence.entity.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.infrastructure.persistence.entity.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.mapper.UsuarioPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class OrdemServicoPersistenceMapper {

    private final UsuarioPersistenceMapper usuarioMapper;

    public OrdemServicoPersistenceMapper(UsuarioPersistenceMapper usuarioMapper) {
        this.usuarioMapper = usuarioMapper;
    }

    public OrdemServico toDomain(OrdemServicoEntity entity) {
        var servicos = entity.getServicosSolicitados().stream().map(this::toDomain).toList();
        return OrdemServico.reconstituir(
                entity.getId(), entity.getNumeroOs(), toDomain(entity.getCliente()),
                new Veiculo(entity.getVeiculo().getId(), entity.getVeiculo().getPlaca(), entity.getVeiculo().getMarca(),
                        entity.getVeiculo().getModelo(), entity.getVeiculo().getAno()), entity.getStatus(),
                entity.getDataAbertura(), toDomain(entity.getDiagnostico()), servicos,
                entity.getExecucaoIniciadaEm(), entity.getFinalizadaEm(), entity.getEntregueEm(),
                entity.getUltimaAtualizacao(), entity.getAcompanhamentoTokenHash(),
                entity.getAcompanhamentoTokenCriadoEm(), entity.getAcompanhamentoTokenExpiraEm(),
                entity.getAcompanhamentoTokenRevogadoEm());
    }

    public OrdemServicoEntity toEntity(OrdemServico domain) {
        OrdemServicoEntity entity = new OrdemServicoEntity();
        entity.setId(domain.getId());
        entity.setNumeroOs(domain.getNumeroOs());
        entity.setCliente(toEntity(domain.getCliente()));
        entity.setVeiculo(toVehicleReference(domain.getVeiculo()));
        entity.setStatus(domain.getStatus());
        entity.setDataAbertura(domain.getDataAbertura());
        entity.setDiagnostico(toEntity(domain.getDiagnostico()));
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
            ServicoSolicitadoEntity servicoEntity = toEntity(servico);
            servicoEntity.setOrdemServico(entity);
            servicos.add(servicoEntity);
        });
        entity.setServicosSolicitados(servicos);
        return entity;
    }

    private ServicoSolicitado toDomain(ServicoSolicitadoEntity entity) {
        ServicoSolicitado domain = new ServicoSolicitado();
        domain.setId(entity.getId());
        domain.setServicoId(entity.getServicoId());
        domain.setNome(entity.getNome());
        domain.setValor(entity.getValor());
        domain.setStatus(entity.getStatus());
        domain.setIniciadoEm(entity.getIniciadoEm());
        domain.setFinalizadoEm(entity.getFinalizadoEm());
        domain.setItensNecessarios(entity.getItensNecessarios().stream().map(this::toDomain).toList());
        if (entity.getReparoAdicional() != null) {
            domain.setReparoAdicional(toRepairReference(entity.getReparoAdicional()));
        }
        return domain;
    }

    public ServicoSolicitado toDomainServico(ServicoSolicitadoEntity entity) {
        return toDomain(entity);
    }

    private ServicoSolicitadoEntity toEntity(ServicoSolicitado domain) {
        ServicoSolicitadoEntity entity = new ServicoSolicitadoEntity();
        entity.setId(domain.getId());
        entity.setServicoId(domain.getServicoId());
        entity.setNome(domain.getNome());
        entity.setValor(domain.getValor());
        entity.setStatus(domain.getStatus());
        entity.setIniciadoEm(domain.getIniciadoEm());
        entity.setFinalizadoEm(domain.getFinalizadoEm());
        entity.setItensNecessarios(new ArrayList<>(domain.getItensNecessarios().stream()
                .map(this::toEntity)
                .toList()));
        if (domain.getReparoAdicional() != null) {
            ReparoAdicionalEntity repair = new ReparoAdicionalEntity();
            repair.setId(domain.getReparoAdicional().getId());
            entity.setReparoAdicional(repair);
        }
        return entity;
    }

    public ServicoSolicitadoEntity toEntityServico(ServicoSolicitado domain) {
        return toEntity(domain);
    }

    private ItemNecessario toDomain(ItemNecessarioEntity entity) {
        ItemNecessario item = new ItemNecessario();
        item.setPecaInsumoId(entity.getPecaInsumoId()); item.setNome(entity.getNome()); item.setTipo(entity.getTipo());
        item.setValorUnitario(entity.getValorUnitario()); item.setQuantidade(entity.getQuantidade());
        item.setValorTotal(entity.getValorTotal()); item.setStatus(entity.getStatus());
        item.setMotivoPendencia(entity.getMotivoPendencia()); item.setQuantidadeDisponivel(entity.getQuantidadeDisponivel());
        item.setMensagemStatus(entity.getMensagemStatus());
        return item;
    }

    private ItemNecessarioEntity toEntity(ItemNecessario domain) {
        ItemNecessarioEntity entity = new ItemNecessarioEntity();
        entity.setPecaInsumoId(domain.getPecaInsumoId()); entity.setNome(domain.getNome()); entity.setTipo(domain.getTipo());
        entity.setValorUnitario(domain.getValorUnitario()); entity.setQuantidade(domain.getQuantidade());
        entity.setValorTotal(domain.getValorTotal()); entity.setStatus(domain.getStatus());
        entity.setMotivoPendencia(domain.getMotivoPendencia()); entity.setQuantidadeDisponivel(domain.getQuantidadeDisponivel());
        entity.setMensagemStatus(domain.getMensagemStatus());
        return entity;
    }

    private Diagnostico toDomain(DiagnosticoEntity entity) {
        if (entity == null) return null;
        Diagnostico domain = new Diagnostico();
        domain.setMecanico(entity.getMecanico() == null ? null : usuarioMapper.toDomain(entity.getMecanico()));
        domain.setIniciadoEm(entity.getIniciadoEm()); domain.setConcluidoEm(entity.getConcluidoEm()); domain.setLaudo(entity.getLaudo());
        return domain;
    }

    private DiagnosticoEntity toEntity(Diagnostico domain) {
        if (domain == null) return null;
        DiagnosticoEntity entity = new DiagnosticoEntity();
        entity.setMecanico(domain.getMecanico() == null ? null : usuarioMapper.toEntity(domain.getMecanico()));
        entity.setIniciadoEm(domain.getIniciadoEm()); entity.setConcluidoEm(domain.getConcluidoEm()); entity.setLaudo(domain.getLaudo());
        return entity;
    }

    private ClienteOs toDomain(ClienteOsEntity entity) {
        return ClienteOs.fromFields(entity.getId(), entity.getNome(), entity.getCpfCnpj(), entity.getEmail(), entity.getTelefone());
    }

    private ClienteOsEntity toEntity(ClienteOs domain) {
        ClienteOsEntity entity = new ClienteOsEntity();
        entity.setId(domain.getId()); entity.setNome(domain.getNome()); entity.setCpfCnpj(domain.getCpfCnpj());
        entity.setEmail(domain.getEmail()); entity.setTelefone(domain.getTelefone());
        return entity;
    }

    private VeiculoEntity toVehicleReference(Veiculo domain) {
        VeiculoEntity entity = new VeiculoEntity();
        entity.setId(domain.id()); entity.setPlaca(domain.placa()); entity.setMarca(domain.marca());
        entity.setModelo(domain.modelo()); entity.setAno(domain.ano());
        return entity;
    }

    private ReparoAdicional toRepairReference(ReparoAdicionalEntity entity) {
        var domain = new ReparoAdicional();
        domain.setId(entity.getId()); domain.setOrdemServicoId(entity.getOrdemServicoId()); domain.setNumeroOs(entity.getNumeroOs());
        domain.setMecanicoId(entity.getMecanicoId()); domain.setOrcamentoId(entity.getOrcamentoId()); domain.setStatus(entity.getStatus());
        return domain;
    }
}
