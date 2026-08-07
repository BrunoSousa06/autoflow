package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.policy.PlacaPolicy;
import com.autoflow.application.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AtualizarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final AuthorizationService authorizationService;

    public VeiculoOutput execute(Long id, VeiculoInput input) {
        VeiculoOutput veiculo = buscarPorId(id);
        authorizationService.validarPermissao(veiculo);

        String placa = PlacaPolicy.normalizar(input.placa());
        veiculoGateway.findByPlaca(placa)
                .filter(outroVeiculo -> !outroVeiculo.id().equals(id))
                .ifPresent(outroVeiculo -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Placa já cadastrada");
                });

        VeiculoInput inputNormalizado = new VeiculoInput(
                input.marca(),
                input.ano(),
                placa,
                input.modelo());

        return veiculoGateway.update(id, inputNormalizado);
    }

    private VeiculoOutput buscarPorId(Long id) {
        return veiculoGateway.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Veículo não encontrado com o ID: " + id));
    }
}
