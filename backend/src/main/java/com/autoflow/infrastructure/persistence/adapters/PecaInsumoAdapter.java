package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.infrastructure.persistence.mapper.PecaInsumoMapper;
import com.autoflow.infrastructure.persistence.repository.PecaInsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PecaInsumoAdapter implements PecaInsumoGateway {

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
    public Page<PecaInsumoEntity> findAll(Specification<PecaInsumoEntity> spec, Pageable pageable) {
        return pecaInsumoRepository.findAll(spec, pageable);
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
    public List<PecaInsumoEntity> findAllById(List<Long> ids) {
        return pecaInsumoRepository.findAllById(ids);
    }

    @Override
    public void saveAll(List<PecaInsumoEntity> alterados) {
        pecaInsumoRepository.saveAll(alterados);
    }
}
