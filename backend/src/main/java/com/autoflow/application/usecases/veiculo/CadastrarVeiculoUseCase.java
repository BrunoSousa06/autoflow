package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.mapper.VeiculoMapper;
import com.autoflow.infrastructure.persistence.repository.ClienteRepository;
import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CadastrarVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoMapper veiculoMapper;

    public VeiculoOutput execute(CadastrarVeiculoInput input) {

        ClienteEntity cliente = clienteRepository
                .findByCpfCnpj(input.cpfCnpj())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cliente não encontrado com o CPF/CNPJ: " + input.cpfCnpj()));

        if (veiculoRepository.existsByPlaca(input.placa())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe um veículo cadastrado com a placa: " + input.placa());
        }

        VeiculoEntity entity =
                veiculoMapper.mapToEntity(input, cliente);

        VeiculoEntity salvo =
                veiculoRepository.save(entity);

        return veiculoMapper.mapToOutput(salvo);
    }
}
