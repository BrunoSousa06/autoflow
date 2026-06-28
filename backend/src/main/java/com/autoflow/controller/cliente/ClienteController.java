package com.autoflow.controller.cliente;


import com.autoflow.controller.cliente.request.ClienteRequest;
import com.autoflow.controller.cliente.response.ClienteResponse;
import com.autoflow.service.cliente.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "clientes", description = "Endpoints para gerenciamento de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;


    @Operation(summary = "Retornar os dados do cliente autenticado", description = "Retorna as informações do cliente logado. Exclusivo para usuários com role CLIENTE.")
    @ApiResponse(responseCode = "200", description = "Dados do cliente retornados com sucesso")
    @ApiResponse(responseCode = "404", description = "Nenhum cliente vinculado ao usuário autenticado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão — apenas CLIENTE pode acessar este endpoint")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<ClienteResponse> meuPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(clienteService.buscarPorEmail(userDetails.getUsername()));
    }

    @Operation(summary = "Criar cadastro do cliente", description = "cria cadastro do cliente")
    @ApiResponse(responseCode = "200", description = "Cliente cadastrado com sucesso")
    @ApiResponse(responseCode = "409", description = "CPF/CNPJ ja cadastrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrarCliente(@Valid @RequestBody ClienteRequest request){
        return ResponseEntity.ok(clienteService.cadastrar(request));

    }

    @Operation(summary = "Listar um cliente pelo CPF/CNPJ ou ID", description = "Retorna os detalhes de um cliente específico")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping("/{documento}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public ResponseEntity<ClienteResponse> listar(@PathVariable Long documento ){
            return ResponseEntity.ok(clienteService.listar(documento));
    }

    @Operation(summary = "Listar todos os clientes cadastrados", description = "Retorna a lista de clientes cadastrados")
    @ApiResponse(responseCode = "200", description = "Clientes listados com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public ResponseEntity<List<ClienteResponse>> listarTodosClientes(){
        return ResponseEntity.ok(clienteService.listarTodosClientes());

    }

    @Operation(summary = "Atualizar as informações de um cliente", description = "Atualiza as informações de um cliente")
    @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{id}/atualizacao")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public ResponseEntity<ClienteResponse> atualizar(@Valid @RequestBody ClienteRequest request, @PathVariable Long id){
        return ResponseEntity.ok(clienteService.atualizar(request, id));

    }

    @Operation(summary = "Deletar um cliente", description = "Deleta um cliente pelo seu ID")
    @ApiResponse(responseCode = "200", description = "Cliente deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deletar(@PathVariable Long id){
        clienteService.deletar(id);
        return ResponseEntity.ok().body("cliente deletado com sucesso");

    }


}
