package com.autoflow.presentation.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.usecases.pecainsumo.*;
import com.autoflow.infrastructure.persistence.mapper.PecaInsumoMapper;
import com.autoflow.presentation.pecainsumo.request.PecaInsumoRequest;
import com.autoflow.presentation.pecainsumo.response.PecaInsumoResponse;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/peca-insumo")
@RequiredArgsConstructor
@Tag(name = "peças e insumos", description = "Endpoints para gerenciamento das peças e insumos")
@SecurityRequirement(name = "bearerAuth")
public class PecaInsumoController {

    private final CadastrarPecaInsumoUseCase cadastrarPecaInsumoUseCase;
    private final BuscarPecaInsumoPorIdUseCase buscarPecaInsumoPorIdUseCase;
    private final ListarPecaInsumoPaginadoUseCase listarPecaInsumoPaginadoUseCase;
    private final AtualizarPecaInsumoUseCase atualizarPecaInsumoUseCase;
    private final DeletarPecaInsumoUseCase deletarPecaInsumoUseCase;
    private final PecaInsumoMapper pecaInsumoMapper;


    @Operation(summary = "Cadastrar uma peça ou insumo", description = "Retorna as informações da peça ou insumo cadastrado")
    @ApiResponse(responseCode = "201", description = "Peça/Insumo cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Peça/Insumo ja foi cadastrado ou dados inválidos")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<PecaInsumoResponse> cadastrar(@Valid @RequestBody PecaInsumoRequest request) {
        PecaInsumoInput input = pecaInsumoMapper.mapToInput(request);
        PecaInsumoOutput pecaInsumo = cadastrarPecaInsumoUseCase.execute(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(pecaInsumoMapper.toResponse(pecaInsumo));

    }

    @Operation(summary = "Listar uma peça ou insumo pelo ID", description = "Retorna as informações da peça ou insumo cadastrado")
    @ApiResponse(responseCode = "200", description = "Peça/Insumo encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Peça/Insumo não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<PecaInsumoResponse> listar(@PathVariable Long id) {
        PecaInsumoOutput pecasInsumos = buscarPecaInsumoPorIdUseCase.execute(id);
        return ResponseEntity.ok(pecaInsumoMapper.toResponse(pecasInsumos));
    }

    @Operation(summary = "Listar todas peças e insumos cadastrados", description = "Retorna as informações das peças e insumos cadastrados de forma paginada")
    @ApiResponse(responseCode = "200", description = "Peça/Insumos encontrados com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<Page<PecaInsumoResponse>> listarTodasPecasEInsumos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) CategoriaPecaInsumo tipo) {
        var pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
        Page<PecaInsumoOutput> todasPecas = listarPecaInsumoPaginadoUseCase.execute(pageable, nome, tipo);

        return ResponseEntity.ok(todasPecas.map(pecaInsumoMapper::toResponse));
    }

    @Operation(summary = "Atualizar uma peça ou insumo", description = "Atualiza as informações de uma peça ou insumo")
    @ApiResponse(responseCode = "200", description = "Peça/Insumo atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Peça/Insumo não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{id}/atualizacao")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<PecaInsumoResponse> atualizar(@Valid @RequestBody PecaInsumoRequest request, @PathVariable Long id) {
        PecaInsumoInput input = pecaInsumoMapper.mapToInput(request);
        PecaInsumoOutput pecaInsumoAtualizado = atualizarPecaInsumoUseCase.execute(id, input);
        return ResponseEntity.ok(pecaInsumoMapper.toResponse(pecaInsumoAtualizado));

    }

    @Operation(summary = "Deletar uma peça ou um insumo", description = "Deleta uma peça ou um insumo pelo ID")
    @ApiResponse(responseCode = "200", description = "Peça/Insumo deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Peça/Insumo não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public ResponseEntity<String> deletar(@PathVariable Long id){
        deletarPecaInsumoUseCase.execute(id);
        return ResponseEntity.ok().body("peca/insumo deletado com sucesso");

    }
}
