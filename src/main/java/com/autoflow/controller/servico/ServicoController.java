package com.autoflow.controller.servico;


import com.autoflow.controller.servico.request.ServicoRequest;
import com.autoflow.controller.servico.response.ServicoResponse;
import com.autoflow.service.servico.ServicoService;
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
@RequestMapping("/servicos")
@RequiredArgsConstructor
@Tag(name = "serviços", description = "Endpoints para gerenciamento dos serviços")
public class ServicoController {

    private final ServicoService servicoService;

    @Operation(summary = "Cadastrar um serviço", description = "Retorna as informações do serviço cadastrado")
    @ApiResponse(responseCode = "200", description = "Orçamento encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Orçamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PostMapping
    @PreAuthorize("hasAnyRole('MECANICO', 'ADMIN')")
    public ResponseEntity<ServicoResponse> cadastrar(@Valid @RequestBody ServicoRequest request ){
        return ResponseEntity.status(HttpStatus.CREATED).body(servicoService.cadastrar(request));

    }

    @Operation(summary = "Listar serviço pelo ID", description = "Retorna as informações do serviço")
    @ApiResponse(responseCode = "200", description = "Serviço encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<ServicoResponse> listar(@PathVariable Long id ){
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @Operation(summary = "Listar todos serviços", description = "Listar todos serviços cadastrados")
    @ApiResponse(responseCode = "200", description = "Serviços encontrados com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<List<ServicoResponse>> listarTodosServicos(){
        return ResponseEntity.ok(servicoService.listar());

    }

    @Operation(summary = "Atualizar as informações do serviço", description = "Atualizar as informações do serviço a partir do ID")
    @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{id}/atualizacao")
    @PreAuthorize("hasAnyRole('MECANICO', 'ADMIN')")
    public ResponseEntity<ServicoResponse> atualizar(@Valid @RequestBody ServicoRequest request, @PathVariable Long id){
        return ResponseEntity.ok(servicoService.atualizar(request, id));

    }

    @Operation(summary = "Deletar um serviço", description = "Deleta um serviço pelo ID")
    @ApiResponse(responseCode = "200", description = "Serviço deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id){
        servicoService.deletar(id);
        return ResponseEntity.ok().body("serviço deletado com sucesso");

    }


}
