package com.autoflow.controller.pecainsumo;

import com.autoflow.controller.pecainsumo.request.PecaInsumoRequest;
import com.autoflow.controller.pecainsumo.response.PecaInsumoResponse;
import com.autoflow.service.pecainsumo.PecaInsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/peca-insumo")
@RequiredArgsConstructor
@Tag(name = "peças e insumos", description = "Endpoints para gerenciamento das peças e insumos")
public class PecaInsumoController {

    private final PecaInsumoService pecaInsumoService;


    @Operation(summary = "Cadastrar uma peça ou insumo", description = "Retorna as informações da peça ou insumo cadastrado")
    @ApiResponse(responseCode = "200", description = "Peça/Insumo cadastrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Peça/Insumo ja foi cadastrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public ResponseEntity<PecaInsumoResponse> cadastrar(@Valid @RequestBody PecaInsumoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pecaInsumoService.cadastrar(request));

    }

    @Operation(summary = "Listar uma peça ou insumo pelo ID", description = "Retorna as informações da peça ou insumo cadastrado")
    @ApiResponse(responseCode = "200", description = "Peça/Insumo encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Peça/Insumo não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<PecaInsumoResponse> listar(@PathVariable Long id) {
        return ResponseEntity.ok(pecaInsumoService.buscarPorId(id));
    }

    @Operation(summary = "Listar todas peças e insumos cadastrados", description = "Retorna as informações das peças e insumos cadastrados")
    @ApiResponse(responseCode = "200", description = "Peça/Insumos encontrados com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<List<PecaInsumoResponse>> listarTodasPecasEInsumos() {
        return ResponseEntity.ok(pecaInsumoService.listar());

    }

    @Operation(summary = "Atualizar uma peça ou insumo", description = "Atualiza as informações de uma peça ou insumo")
    @ApiResponse(responseCode = "200", description = "Peça/Insumo atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Peça/Insumo não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{id}/atualizacao")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<PecaInsumoResponse> atualizar(@Valid @RequestBody PecaInsumoRequest request, @PathVariable Long id) {
        return ResponseEntity.ok(pecaInsumoService.atualizar(request, id));

    }

    @Operation(summary = "Deletar uma peça ou um insumo", description = "Deleta uma peça ou um insumo pelo ID")
    @ApiResponse(responseCode = "200", description = "Peça/Insumo deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Peça/Insumo não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public ResponseEntity<String> deletar(@PathVariable Long id){
        pecaInsumoService.deletar(id);
        return ResponseEntity.ok().body("peca/insumo deletado com sucesso");

    }
}
