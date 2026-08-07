package com.autoflow.service.ordemservico;

import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.policy.PlacaPolicy;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Ponte temporária para o modelo JPA exigido pela entidade de ordem de serviço.
 */
@Service
@RequiredArgsConstructor
public class BuscarOuCadastrarVeiculoForOrdemServicoUseCase {

    private final VeiculoRepository veiculoRepository;

    public VeiculoEntity execute(ClienteEntity cliente, VeiculoOrdemServicoInput input) {
        String placa = PlacaPolicy.normalizar(input.placa());
        Optional<VeiculoEntity> existente = veiculoRepository.findByPlaca(placa);

        if (existente.isPresent()) {
            VeiculoEntity veiculo = existente.get();
            if (!veiculo.getCliente().getId().equals(cliente.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Placa já cadastrada para outro cliente.");
            }
            return veiculo;
        }

        if (input.marca() == null || input.marca().isBlank()
                || input.modelo() == null || input.modelo().isBlank()
                || input.ano() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Marca, modelo e ano são obrigatórios para cadastrar um novo veículo.");
        }

        VeiculoEntity novo = new VeiculoEntity();
        novo.setCliente(cliente);
        novo.setPlaca(placa);
        novo.setMarca(input.marca());
        novo.setModelo(input.modelo());
        novo.setAno(input.ano());
        return veiculoRepository.save(novo);
    }
}
