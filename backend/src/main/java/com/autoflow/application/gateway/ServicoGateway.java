package com.autoflow.application.gateway;

import com.autoflow.domain.servico.ServicoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface ServicoGateway {


    ServicoEntity save(ServicoEntity servico);


    Optional<ServicoEntity> findById(Long id);


    Optional<ServicoEntity> findByNomeIgnoreCase(String nome);


    Page<ServicoEntity> findAllByAtivoTrue(Pageable pageable);


    boolean existsById(Long id);

    void deleteById(Long id);

}
