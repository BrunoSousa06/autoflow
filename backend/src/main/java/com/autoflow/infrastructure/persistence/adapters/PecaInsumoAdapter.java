package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.input.PageQuery;
import com.autoflow.application.output.PageResult;
import com.autoflow.application.output.pecainsumo.EstoqueItemOutput;
import com.autoflow.application.input.pecainsumo.PecaInsumoFiltro;
import com.autoflow.application.input.pecainsumo.PecaInsumoInput;
import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.EstoqueGateway;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.infrastructure.persistence.entity.pecainsumo.PecaInsumoEntity;
import com.autoflow.infrastructure.persistence.mapper.PecaInsumoPersistenceMapper;
import com.autoflow.infrastructure.persistence.repository.PecaInsumoRepository;
import com.autoflow.infrastructure.persistence.repository.PecaInsumoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PecaInsumoAdapter implements PecaInsumoGateway, EstoqueGateway {

    private final PecaInsumoRepository pecaInsumoRepository;
    private final PecaInsumoPersistenceMapper mapper;

    @Override
    public Optional<PecaInsumoOutput> findById(Long id) {
        return pecaInsumoRepository.findById(id).map(mapper::toOutput);
    }

    @Override
    public Optional<PecaInsumoOutput> findByNomeIgnoreCase(String nome) {
        return pecaInsumoRepository.findByNomeIgnoreCase(nome).map(mapper::toOutput);
    }

    @Override
    public PecaInsumoOutput save(PecaInsumoInput input) {
        return mapper.toOutput(pecaInsumoRepository.save(mapper.toEntity(input)));
    }

    @Override
    public PecaInsumoOutput update(Long id, PecaInsumoInput input) {
        PecaInsumoEntity entity = pecaInsumoRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Peça/Insumo não encontrado: " + id));
        mapper.updateEntity(input, entity);
        return mapper.toOutput(pecaInsumoRepository.save(entity));
    }

    @Override
    public List<PecaInsumoOutput> findAll() {
        return pecaInsumoRepository.findAll().stream().map(mapper::toOutput).toList();
    }

    @Override
    public PageResult<PecaInsumoOutput> findAll(PecaInsumoFiltro filtro, PageQuery pageQuery) {
        Page<PecaInsumoEntity> page = pecaInsumoRepository.findAll(
                PecaInsumoSpecifications.comFiltros(filtro.nome(), filtro.tipo()),
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("nome").ascending()));
        return new PageResult<>(page.getContent().stream().map(mapper::toOutput).toList(), page.getTotalElements(),
                pageQuery.page(), pageQuery.size());
    }

    @Override
    public boolean existsById(Long id) {
        return pecaInsumoRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        pecaInsumoRepository.deleteById(id);
    }

    @Override
    public List<EstoqueItemOutput> findAllById(List<Long> ids) {
        return toEstoqueOutput(pecaInsumoRepository.findAllById(ids));
    }

    @Override
    public List<EstoqueItemOutput> findAllByIdForUpdate(List<Long> ids) {
        return toEstoqueOutput(pecaInsumoRepository.findAllByIdForUpdate(ids));
    }

    @Override
    public void saveAll(List<EstoqueItemOutput> alterados) {
        if (alterados.isEmpty()) {
            return;
        }

        List<Long> ids = alterados.stream().map(EstoqueItemOutput::id).toList();
        Map<Long, PecaInsumoEntity> entidades = pecaInsumoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PecaInsumoEntity::getId, Function.identity()));

        List<PecaInsumoEntity> entidadesAlteradas = alterados.stream()
                .map(item -> {
                    PecaInsumoEntity entidade = entidades.get(item.id());
                    if (entidade == null) {
                        throw new IllegalStateException("Peça/Insumo não encontrado com o ID: " + item.id());
                    }
                    entidade.setQuantidade(item.quantidade());
                    return entidade;
                })
                .toList();

        pecaInsumoRepository.saveAll(entidadesAlteradas);
    }

    private List<EstoqueItemOutput> toEstoqueOutput(List<PecaInsumoEntity> itens) {
        return itens.stream()
                .map(item -> new EstoqueItemOutput(
                        item.getId(),
                        item.getNome(),
                        item.getTipo(),
                        item.getValor(),
                        item.getQuantidade()
                ))
                .toList();
    }
}
