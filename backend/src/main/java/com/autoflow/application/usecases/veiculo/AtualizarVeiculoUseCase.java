package com.autoflow.application.usecases.veiculo;


import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.security.AuthorizationService;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.mapper.VeiculoMapper;
import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AtualizarVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoMapper veiculoMapper;
    private final AuthorizationService authorizationService;

    public VeiculoOutput execute(Long id, VeiculoInput input) {

        VeiculoEntity veiculo = buscarPorId(id);

        authorizationService.validarPermissao(veiculo);

        Optional<VeiculoEntity> veiculoPlaca =
                veiculoRepository.findByPlaca(input.placa());

        if (veiculoPlaca.isPresent()
                && !veiculoPlaca.get().getId().equals(id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Placa já cadastrada");
        }

        veiculoMapper.updateEntity(input, veiculo);

        VeiculoEntity atualizado =
                veiculoRepository.save(veiculo);

        return veiculoMapper.mapToOutput(atualizado);
    }

    private VeiculoEntity buscarPorId(Long id) {

        return veiculoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Veículo não encontrado com o ID: " + id));
    }
}
