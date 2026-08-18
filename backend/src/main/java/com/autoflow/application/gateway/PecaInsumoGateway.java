package com.autoflow.application.gateway;


import com.autoflow.application.input.PageQuery;
import com.autoflow.application.output.PageResult;
import com.autoflow.application.input.pecainsumo.PecaInsumoFiltro;
import com.autoflow.application.input.pecainsumo.PecaInsumoInput;
import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;

import java.util.List;
import java.util.Optional;

public interface PecaInsumoGateway {


    Optional<PecaInsumoOutput> findById(Long id);


    Optional<PecaInsumoOutput> findByNomeIgnoreCase(String nome);

    PecaInsumoOutput save(PecaInsumoInput input);

    PecaInsumoOutput update(Long id, PecaInsumoInput input);

    List<PecaInsumoOutput> findAll();

    PageResult<PecaInsumoOutput> findAll(PecaInsumoFiltro filtro, PageQuery pageQuery);

    boolean existsById(Long id);

    void deleteById(Long id);

}
