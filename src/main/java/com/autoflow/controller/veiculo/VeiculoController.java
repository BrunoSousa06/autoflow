package com.autoflow.controller.veiculo;


import com.autoflow.controller.veiculo.request.VeiculoRequest;
import com.autoflow.controller.veiculo.request.VeiculoUpdateRequest;
import com.autoflow.controller.veiculo.response.VeiculoResponse;
import com.autoflow.service.veiculo.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
@Tag(name = "veiculos", description = "Endpoints para gerenciamento de veiculos")
@SecurityRequirement(name = "bearerAuth")
public class VeiculoController {

    private final VeiculoService veiculoService;

    @Operation(summary = "Cadastrar um veiculo", description = "Retorna as informações do veiculo cadastrado")
    @ApiResponse(responseCode = "201", description = "Veiculo cadastrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente do veiculo não foi encontrado")
    @ApiResponse(responseCode = "409", description = "Placa do veiculo ja foi cadastrada")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody VeiculoRequest request ){
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculoService.cadastrar(request));

    }

    @Operation(summary = "Listar veiculo por ID", description = "Retorna as informações do veiculo")
    @ApiResponse(responseCode = "200", description = "Veiculo encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Veiculo não foi encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    public ResponseEntity<VeiculoResponse> listar(@PathVariable Long id ){
        return ResponseEntity.ok(veiculoService.listar(id));
    }

    @Operation(summary = "Listar todos veiculos", description = "Retorna a lista de veiculos cadastrados")
    @ApiResponse(responseCode = "200", description = "Veiculos listados com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    public ResponseEntity<List<VeiculoResponse>> listarTodosVeiculos(){
        return ResponseEntity.ok(veiculoService.listarTodosVeiculos());

    }

    @Operation(summary = "Atualizar as informações do veiculo", description = "Atualizar as informações do veiculo pelo ID")
    @ApiResponse(responseCode = "200", description = "Veiculo atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Veiculo não foi encontrado")
    @ApiResponse(responseCode = "409", description = "Placa informada ja foi cadastrada")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{id}/atualizacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'CLIENTE')")
    public ResponseEntity<VeiculoResponse> atualizar(@Valid @RequestBody VeiculoUpdateRequest request, @PathVariable Long id){
        return ResponseEntity.ok(veiculoService.atualizar(request, id));

    }

    @Operation(summary = "Remover o veiculo cadastrado", description = "Remove o veiculo cadastrado pelo ID")
    @ApiResponse(responseCode = "200", description = "Veiculo deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Veiculo não foi encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<String> deletar(@PathVariable Long id){
        veiculoService.deletar(id);
        return ResponseEntity.ok().body("veiculo deletado com sucesso");

    }


}
