package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.infrastructure.persistence.mapper.PecaInsumoMapper;
import com.autoflow.infrastructure.persistence.repository.PecaInsumoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListarPecaInsumoPaginadoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;
    private final PecaInsumoMapper mapper;

    public Page<PecaInsumoOutput> execute(
            Pageable pageable,
            String nome,
            CategoriaPecaInsumo tipo) {

        var spec = PecaInsumoSpecifications.comFiltros(nome, tipo);

        return pecaInsumoGateway.findAll(spec, pageable)
                .map(mapper::mapToOutput);
    }
}
