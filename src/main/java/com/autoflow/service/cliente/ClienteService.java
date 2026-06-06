package com.autoflow.service.cliente;

import com.autoflow.controller.cliente.request.ClienteRequest;
import com.autoflow.controller.cliente.response.ClienteResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.mapper.ClienteMapper;
import com.autoflow.repository.cliente.ClienteRepository;
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

    public ClienteEntity buscarPorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cliente nao encontrado com o ID: " + id
        ));
    }

    public ClienteEntity buscarPorCpfCnpj(String cpfCnpj) {
        return clienteRepository.findByCpfCnpj(cpfCnpj).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Cliente nao encontrado com o CPF/CNPJ: " + cpfCnpj
        ));
    }


    public ClienteResponse listar(Long documento) {

        String identificador =String.valueOf(documento).replaceAll("\\D", "");

        if (identificador.matches("\\d{11}|\\d{14}")) {
            return clienteMapper.maptoResponse(buscarPorCpfCnpj(identificador));
        }

        ClienteEntity clienteEntity = clienteRepository.findById(documento).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "cliente não encontrado com o ID: " + documento
        ));

        return clienteMapper.maptoResponse(clienteEntity);

    }



    public List<ClienteResponse> listarTodosClientes() {

        List<ClienteEntity> clientes = clienteRepository.findAll();

        return clienteMapper.mapToList(clientes);
    }

    public ClienteResponse atualizar(ClienteRequest request, Long id) {
        ClienteEntity clienteEntity = clienteRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "cliente não encontrado com o ID: " + id
        ));

        clienteMapper.updateEntity(request, clienteEntity);

        UsuarioEntity usuario = clienteEntity.getUsuario();

        if (usuario != null) {
            usuario.setNome(request.nome());
            usuario.setEmail(request.email());
        }

        ClienteEntity save = clienteRepository.save(clienteEntity);

        return clienteMapper.maptoResponse(save);
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
