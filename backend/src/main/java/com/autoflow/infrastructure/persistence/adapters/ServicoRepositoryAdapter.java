package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.dto.servico.PageInput;
import com.autoflow.application.dto.servico.PageOutput;
import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.infrastructure.persistence.mapper.ServicoMapper;
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
    private final ServicoMapper servicoMapper;

    @Override
    public ServicoOutput save(ServicoInput input) {
        ServicoEntity entity = servicoMapper.mapToEntity(input);
        return servicoMapper.mapToOutput(servicoRepository.save(entity));
    }

    @Override
    public ServicoOutput update(Long id, ServicoInput input) {
        ServicoEntity entity = servicoRepository.findById(id).orElseThrow();
        servicoMapper.updateEntity(input, entity);
        return servicoMapper.mapToOutput(servicoRepository.save(entity));
    }

    @Override
    public Optional<ServicoOutput> findById(Long id) {
        return servicoRepository.findById(id).map(servicoMapper::mapToOutput);
    }

    @Override
    public boolean existsByNomeIgnoreCase(String nome) {
        return servicoRepository.findByNomeIgnoreCase(nome).isPresent();
    }

    @Override
    public PageOutput<ServicoOutput> findAllByAtivoTrue(PageInput page) {
        PageRequest pageable = PageRequest.of(page.page(), page.size(), Sort.by("id").descending());
        Page<ServicoOutput> result = servicoRepository.findAllByAtivoTrue(pageable)
                .map(servicoMapper::mapToOutput);
        return new PageOutput<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Override
    public void inativar(Long id) {
        ServicoEntity entity = servicoRepository.findById(id).orElseThrow();
        entity.setAtivo(false);
        servicoRepository.save(entity);
    }

}
