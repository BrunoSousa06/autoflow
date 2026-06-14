package com.autoflow.repository.servico;

import com.autoflow.domain.servico.ServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<ServicoEntity, Long> {

    Optional<ServicoEntity> findByNomeIgnoreCase(String nome);
}
