package com.autoflow.presentation.veiculo;


import com.autoflow.application.input.veiculo.CadastrarVeiculoCommand;
import com.autoflow.application.input.veiculo.PageInput;
import com.autoflow.application.input.veiculo.VeiculoInput;
import com.autoflow.application.output.veiculo.PageOutput;
import com.autoflow.application.output.veiculo.VeiculoOutput;
import com.autoflow.application.port.in.veiculo.*;
import com.autoflow.presentation.veiculo.request.VeiculoRequest;
import com.autoflow.presentation.veiculo.request.VeiculoUpdateRequest;
import com.autoflow.presentation.veiculo.response.VeiculoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
@Tag(name = "veículos", description = "Endpoints para gerenciamento de veículos")
@SecurityRequirement(name = "bearerAuth")
public class VeiculoController {

    private final CadastrarVeiculoUseCase cadastrarVeiculoUseCase;
    private final BuscarVeiculoUseCase buscarVeiculoUseCase;
    private final ListarVeiculosUseCase listarVeiculosUseCase;
    private final AtualizarVeiculoUseCase atualizarVeiculoUseCase;
    private final DeletarVeiculoUseCase deletarVeiculoUseCase;

    private final VeiculoControllerMapper mapper;

    @Operation(summary = "Cadastrar um veículo")
    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(
            @Valid @RequestBody VeiculoRequest request) {

        CadastrarVeiculoCommand input = mapper.toInput(request);

        VeiculoOutput output =
                cadastrarVeiculoUseCase.execute(input);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(output));
    }

    @Operation(summary = "Listar veículo por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    public ResponseEntity<VeiculoResponse> listar(
            @PathVariable Long id) {

        VeiculoOutput output =
                buscarVeiculoUseCase.execute(id);

        return ResponseEntity.ok(
                mapper.toResponse(output));
    }

    @Operation(summary = "Listar veículos")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    public ResponseEntity<Page<VeiculoResponse>> listar(

            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) Integer ano,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("id").descending());

        VeiculoInput input =
                new VeiculoInput(
                        marca,
                        ano,
                        placa,
                        modelo
                );

        PageOutput<VeiculoOutput> output =
                listarVeiculosUseCase.execute(input, new PageInput(page, size));

        Page<VeiculoResponse> response = new PageImpl<>(
                output.content().stream().map(mapper::toResponse).toList(),
                pageable,
                output.totalElements());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar veículo")
    @PatchMapping("/{id}/atualizacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    public ResponseEntity<VeiculoResponse> atualizar(

            @PathVariable Long id,
            @Valid @RequestBody VeiculoUpdateRequest request) {

        VeiculoInput input =
                mapper.toInput(request);

        VeiculoOutput output =
                atualizarVeiculoUseCase.execute(id, input);

        return ResponseEntity.ok(
                mapper.toResponse(output));
    }

    @Operation(summary = "Excluir veículo")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id) {

        deletarVeiculoUseCase.execute(id);

        return ResponseEntity.noContent().build();
    }

}
