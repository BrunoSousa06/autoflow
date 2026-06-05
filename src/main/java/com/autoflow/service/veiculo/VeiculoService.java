package com.autoflow.service.veiculo;

import com.autoflow.controller.veiculo.request.VeiculoRequest;
import com.autoflow.controller.veiculo.response.VeiculoResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.mapper.VeiculoMapper;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.repository.veiculo.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoMapper veiculoMapper;

    public VeiculoResponse cadastrar(VeiculoRequest request) {

        ClienteEntity cliente =
                clienteRepository.findByCpfCnpj(request.cpfCnpj())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado com o CPF/CNPJ: " + request.cpfCnpj()));

        if(veiculoRepository.existsByPlaca(request.placa())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um veiculo cadastrado com a placa:" + request.placa());
        }
        VeiculoEntity veiculo = veiculoRepository.save(veiculoMapper.mapToEntity(request, cliente));

        return veiculoMapper.mapToResponse(veiculo);
    }

    public VeiculoResponse listar(Long id) {
        return veiculoMapper.mapToResponse(buscarPorId(id));
    }

    public List<VeiculoResponse> listarTodosVeiculos() {
        return veiculoMapper.mapToList(veiculoRepository.findAll());
    }

    public VeiculoResponse atualizar(VeiculoRequest request, Long id) {

        VeiculoEntity veiculo = buscarPorId(id);

        veiculoMapper.updateEntity(request, veiculo);

        VeiculoEntity atualizado = veiculoRepository.save(veiculo);

        return veiculoMapper.mapToResponse(atualizado);
    }

    public void deletar(Long id) {

        if (!veiculoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado com o ID: " + id
            );
        }

        veiculoRepository.deleteById(id);
    }

    public VeiculoEntity buscarPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado com o ID: " + id));
    }
}

