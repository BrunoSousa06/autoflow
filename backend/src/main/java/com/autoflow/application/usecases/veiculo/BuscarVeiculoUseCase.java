package com.autoflow.application.usecases.veiculo;


import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.security.AuthorizationService;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.mapper.VeiculoMapper;
import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BuscarVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoMapper veiculoMapper;
    private final AuthorizationService authorizationService;

    public VeiculoOutput execute(Long id) {

        VeiculoEntity veiculo = buscarPorId(id);

        authorizationService.validarPermissao(veiculo);

        return veiculoMapper.mapToOutput(veiculo);
    }

    private VeiculoEntity buscarPorId(Long id) {

        return veiculoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Veículo não encontrado com o ID: " + id));
    }
}
