package com.autoflow.application.gateway;

import com.autoflow.application.input.servico.PageInput;
import com.autoflow.application.output.servico.PageOutput;
import com.autoflow.domain.servico.Servico;

import java.util.Optional;

public interface ServicoGateway {

    Servico save(Servico servico);

    Servico update(Servico servico);

    Optional<Servico> findById(Long id);

    boolean existsByNomeIgnoreCase(String nome);

    PageOutput<Servico> findAllByAtivoTrue(PageInput page);

    void inativar(Long id);

}
