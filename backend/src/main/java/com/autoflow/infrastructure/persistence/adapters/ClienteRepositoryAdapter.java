package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.mapper.ClienteMapper;
import com.autoflow.infrastructure.persistence.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class ClienteRepositoryAdapter implements ClienteGateway {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    public ClienteEntity save(ClienteEntity cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public Optional<ClienteEntity> findById(Long id) {
        return clienteRepository.findById(id);
    }

    @Override
    public Optional<ClienteEntity> findByCpfCnpj(String cpfCnpj) {
        return clienteRepository.findByCpfCnpj(cpfCnpj);

    }

    @Override
    public boolean existsByCpfCnpj(String cpfCnpj) {
        return clienteRepository.existsByCpfCnpj(cpfCnpj);
    }

    @Override
    public Optional<ClienteOutput> findByUsuarioEmail(String usuarioEmail) {
        Optional<ClienteEntity> byUsuarioEmail = clienteRepository.findByUsuarioEmail(usuarioEmail);


        return clienteMapper.mapToOutputOpt(byUsuarioEmail);
    }

    @Override
    public List<ClienteOutput> findAll() {
        List<ClienteEntity> allClientes = clienteRepository.findAll();
        return clienteMapper.mapToListOutput(allClientes);
    }

    @Override
    public void deleteById(Long id) {
        clienteRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return clienteRepository.existsById(id);
    }
}
