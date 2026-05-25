package com.autoflow.service.veiculo;

import com.autoflow.controller.veiculo.VeiculoEntrada;
import com.autoflow.controller.veiculo.VeiculoSaida;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.mapper.VeiculoMapper;
import com.autoflow.repository.veiculo.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoService {


    final VeiculoRepository veiculoRepository;

    final ClienteRepository clienteRepository;

    final VeiculoMapper veiculoMapper;


    public VeiculoSaida cadastrar(VeiculoEntrada entrada){
        ClienteEntity cliente = clienteRepository.findById(entrada.getIdCliente())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado com o ID: " + entrada.getIdCliente()
                ));
        VeiculoEntity veiculoEntity = veiculoRepository.save(veiculoMapper.mapToEntity(entrada, cliente));
        return veiculoMapper.mapToSaida(veiculoEntity);

    }

    public VeiculoSaida listar(Long id) {
        VeiculoEntity veiculoEntity = veiculoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Veiculo não encontrado com o ID: " + id
        ));

        return veiculoMapper.mapToSaida(veiculoEntity);

    }

    public List<VeiculoSaida> listarTodosVeiculos() {

        List<VeiculoEntity> veiculos = veiculoRepository.findAll();

        return veiculoMapper.mapToList(veiculos);
    }

    public VeiculoSaida atualizar(VeiculoEntrada entrada, Long id) {
        VeiculoEntity veiculoEntity = veiculoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "veiculo não encontrado com o ID: " + id
        ));

        veiculoMapper.updateEntity(entrada, veiculoEntity);
        VeiculoEntity save = veiculoRepository.save(veiculoEntity);
        return veiculoMapper.mapToSaida(save);
    }

    public void deletar(Long id) {
        if(!veiculoRepository.existsById(id)){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "veiculo não encontrado com o ID: " + id
            );
        }
        veiculoRepository.deleteById(id);
    }
}

