package com.autoflow.application.usecases.veiculo;

import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DeletarVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;

    public void execute(Long id) {

        if (!veiculoRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Veículo não encontrado com o ID: " + id);
        }

        veiculoRepository.deleteById(id);
    }
}
