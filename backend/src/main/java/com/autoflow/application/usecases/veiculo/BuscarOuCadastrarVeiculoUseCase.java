package com.autoflow.application.usecases.veiculo;

import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BuscarOuCadastrarVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;

    public VeiculoEntity execute(
            ClienteEntity cliente,
            VeiculoOrdemServicoRequest request) {

        String placa = normalizarPlaca(request.placa());

        Optional<VeiculoEntity> existente =
                veiculoRepository.findByPlaca(placa);

        if (existente.isPresent()) {

            VeiculoEntity veiculo = existente.get();

            if (!veiculo.getCliente().getId().equals(cliente.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Placa já cadastrada para outro cliente.");
            }

            return veiculo;
        }

        if (request.marca() == null
                || request.marca().isBlank()
                || request.modelo() == null
                || request.modelo().isBlank()
                || request.ano() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Marca, modelo e ano são obrigatórios para cadastrar um novo veículo.");
        }

        VeiculoEntity novo = new VeiculoEntity();
        novo.setCliente(cliente);
        novo.setPlaca(placa);
        novo.setMarca(request.marca());
        novo.setModelo(request.modelo());
        novo.setAno(request.ano());

        return veiculoRepository.save(novo);
    }

    private String normalizarPlaca(String placa) {

        return placa
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase();
    }
}
