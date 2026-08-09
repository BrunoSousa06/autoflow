package com.autoflow.application.gateway;

import com.autoflow.application.dto.pecainsumo.EstoqueItemOutput;

import java.util.List;

public interface EstoqueGateway {

    List<EstoqueItemOutput> findAllById(List<Long> ids);

    List<EstoqueItemOutput> findAllByIdForUpdate(List<Long> ids);

    void saveAll(List<EstoqueItemOutput> itens);
}
