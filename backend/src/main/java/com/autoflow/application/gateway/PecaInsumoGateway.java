package com.autoflow.application.gateway;


import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.pecainsumo.PecaInsumoFiltro;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;

import java.util.List;
import java.util.Optional;

public interface PecaInsumoGateway {


    Optional<PecaInsumoEntity> findById(Long id);


    Optional<PecaInsumoEntity> findByNomeIgnoreCase(String nome);

    PecaInsumoEntity save(PecaInsumoEntity pecaInsumoEntity);

    List<PecaInsumoEntity> findAll();

    PageResult<PecaInsumoEntity> findAll(PecaInsumoFiltro filtro, PageQuery pageQuery);

    boolean existsById(Long id);

    void deleteById(Long id);

}
