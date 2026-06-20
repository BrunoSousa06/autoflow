package com.autoflow.controller.servico;


import com.autoflow.controller.servico.request.ServicoRequest;
import com.autoflow.controller.servico.response.ServicoResponse;
import com.autoflow.controller.servico.response.TempoMedioServicoResponse;
import com.autoflow.service.servico.ServicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
@Tag(name = "serviços", description = "Endpoints para gerenciamento dos serviços")
@SecurityRequirement(name = "bearerAuth")
public class ServicoController {

    private final ServicoService servicoService;

    @Operation(summary = "Cadastrar um serviço", description = "Retorna as informações do serviço cadastrado")
    @ApiResponse(responseCode = "201", description = "Serviço cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados obrigatórios não informados ou inválidos")
    @ApiResponse(responseCode = "409", description = "Serviço ja foi cadastrado")
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
    public ResponseEntity<Page<ServicoResponse>> listarTodosServicos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(servicoService.listar(pageable));

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

    @Operation(summary = "Inativar um serviço", description = "Inativa um serviço pelo ID (soft-delete)")
    @ApiResponse(responseCode = "200", description = "Serviço inativado com sucesso")
    @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> inativar(@PathVariable Long id){
        servicoService.inativar(id);
        return ResponseEntity.ok().body("Serviço inativado com sucesso");

    }

    @Operation(
            summary = "Consultar tempo médio por serviço",
            description = "Retorna o tempo médio de execução agrupado por serviço finalizado."
    )
    @ApiResponse(responseCode = "200", description = "Tempo médio por serviço consultado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping("/metricas/tempo-medio")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TempoMedioServicoResponse>> listarTempoMedioPorServico() {
        return ResponseEntity.ok(servicoService.listarTempoMedioPorServico());
    }


}
