package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.pecainsumo.EstoqueItemOutput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoFiltro;
import com.autoflow.application.gateway.EstoqueGateway;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
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

    @Override
    public Optional<PecaInsumoEntity> findById(Long id) {
        return pecaInsumoRepository.findById(id);
    }

    @Override
    public Optional<PecaInsumoEntity> findByNomeIgnoreCase(String nome) {
        return pecaInsumoRepository.findByNomeIgnoreCase(nome);
    }

    @Override
    public PecaInsumoEntity save(PecaInsumoEntity pecaInsumoEntity) {
        return pecaInsumoRepository.save(pecaInsumoEntity);
    }

    @Override
    public List<PecaInsumoEntity> findAll() {
        return pecaInsumoRepository.findAll();
    }

    @Override
    public PageResult<PecaInsumoEntity> findAll(PecaInsumoFiltro filtro, PageQuery pageQuery) {
        Page<PecaInsumoEntity> page = pecaInsumoRepository.findAll(
                PecaInsumoSpecifications.comFiltros(filtro.nome(), filtro.tipo()),
                PageRequest.of(pageQuery.page(), pageQuery.size(), Sort.by("nome").ascending()));
        return new PageResult<>(page.getContent(), page.getTotalElements(),
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
