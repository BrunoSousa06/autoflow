package com.autoflow.service.cliente;

import com.autoflow.mapper.ClienteMapper;
import com.autoflow.controller.cliente.ClienteEntrada;
import com.autoflow.controller.cliente.ClienteSaida;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.domain.cliente.ClienteEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    final ClienteRepository clienteRepository;

    final ClienteMapper clienteMapper;


    public ClienteSaida cadastrar(ClienteEntrada entrada){
        ClienteEntity clienteEntity = clienteRepository.save(clienteMapper.mapToEntity(entrada));
        return clienteMapper.mapToSaida(clienteEntity);

    }

    public ClienteSaida listar(Long id) {
        ClienteEntity clienteEntity = clienteRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "cliente não encontrado com o ID: " + id
        ));

        return clienteMapper.mapToSaida(clienteEntity);

    }

    public List<ClienteSaida> listarTodosClientes() {

        List<ClienteEntity> clientes = clienteRepository.findAll();

        return clienteMapper.mapToList(clientes);
    }

    public ClienteSaida atualizar(ClienteEntrada entrada, Long id) {
        ClienteEntity clienteEntity = clienteRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "cliente não encontrado com o ID: " + id
        ));

        clienteMapper.updateEntity(entrada, clienteEntity);
        ClienteEntity save = clienteRepository.save(clienteEntity);
        return clienteMapper.mapToSaida(save);
    }

    public void deletar(Long id) {
        if(!clienteRepository.existsById(id)){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "cliente não encontrado com o ID: " + id
            );
        }
        clienteRepository.deleteById(id);
    }
}
