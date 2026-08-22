package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.input.servico.PageInput;
import com.autoflow.application.output.servico.PageOutput;
import com.autoflow.domain.servico.Servico;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.infrastructure.persistence.mapper.ServicoPersistenceMapper;
import com.autoflow.infrastructure.persistence.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ServicoRepositoryAdapter implements ServicoGateway {

    private final ServicoRepository servicoRepository;
    private final ServicoPersistenceMapper servicoMapper;

    @Override
    public Servico save(Servico servico) {
        ServicoEntity entity = servicoMapper.mapToEntity(servico);
        return servicoMapper.mapToDomain(servicoRepository.save(entity));
    }

    @Override
    public Servico update(Servico servico) {
        ServicoEntity entity = servicoRepository.findById(servico.id()).orElseThrow();
        servicoMapper.updateEntity(servico, entity);
        return servicoMapper.mapToDomain(servicoRepository.save(entity));
    }

    @Override
    public Optional<Servico> findById(Long id) {
        return servicoRepository.findById(id).map(servicoMapper::mapToDomain);
    }

    @Override
    public boolean existsByNomeIgnoreCase(String nome) {
        return servicoRepository.findByNomeIgnoreCase(nome).isPresent();
    }

    @Override
    public PageOutput<Servico> findAllByAtivoTrue(PageInput page) {
        PageRequest pageable = PageRequest.of(page.page(), page.size(), Sort.by("id").descending());
        Page<Servico> result = servicoRepository.findAllByAtivoTrue(pageable)
                .map(servicoMapper::mapToDomain);
        return new PageOutput<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Override
    public void inativar(Long id) {
        ServicoEntity entity = servicoRepository.findById(id).orElseThrow();
        entity.setAtivo(false);
        servicoRepository.save(entity);
    }

}
