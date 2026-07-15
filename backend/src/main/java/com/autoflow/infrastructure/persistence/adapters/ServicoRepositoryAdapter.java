package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.infrastructure.persistence.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public class ServicoRepositoryAdapter implements ServicoGateway {

    private final ServicoRepository servicoRepository;

    @Override
    public ServicoEntity save(ServicoEntity servico) {
        return servicoRepository.save(servico);
    }

    @Override
    public Optional<ServicoEntity> findById(Long id) {
        return servicoRepository.findById(id);
    }

    @Override
    public Optional<ServicoEntity> findByNomeIgnoreCase(String nome) {
        return servicoRepository.findByNomeIgnoreCase(nome);
    }

    @Override
    public Page<ServicoEntity> findAllByAtivoTrue(Pageable pageable) {
        return servicoRepository.findAllByAtivoTrue(pageable);
    }

    @Override
    public boolean existsById(Long id) {
        return servicoRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        servicoRepository.deleteById(id);
    }

}
