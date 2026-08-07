package com.autoflow.application.gateway;


import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface PecaInsumoGateway {


    Optional<PecaInsumoEntity> findById(Long id);


    Optional<PecaInsumoEntity> findByNomeIgnoreCase(String nome);

    PecaInsumoEntity save(PecaInsumoEntity pecaInsumoEntity);

    List<PecaInsumoEntity> findAll();

    Page<PecaInsumoEntity> findAll(Specification<PecaInsumoEntity> spec, Pageable pageable);

    boolean existsById(Long id);

    void deleteById(Long id);

}
