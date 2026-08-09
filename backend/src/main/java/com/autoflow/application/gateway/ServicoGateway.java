package com.autoflow.application.gateway;

import com.autoflow.application.dto.servico.PageInput;
import com.autoflow.application.dto.servico.PageOutput;
import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;

import java.util.Optional;

public interface ServicoGateway {

    ServicoOutput save(ServicoInput input);

    ServicoOutput update(Long id, ServicoInput input);

    Optional<ServicoOutput> findById(Long id);

    boolean existsByNomeIgnoreCase(String nome);

    PageOutput<ServicoOutput> findAllByAtivoTrue(PageInput page);

    void inativar(Long id);

}
