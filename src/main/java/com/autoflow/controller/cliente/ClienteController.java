package com.autoflow.controller.cliente;


import com.autoflow.controller.cliente.request.ClienteRequest;
import com.autoflow.controller.cliente.response.ClienteResponse;
import com.autoflow.service.cliente.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping("/{documento}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public ResponseEntity<ClienteResponse> listar(@PathVariable Long documento ){
            return ResponseEntity.ok(clienteService.listar(documento));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public ResponseEntity<List<ClienteResponse>> listarTodosClientes(){
        return ResponseEntity.ok(clienteService.listarTodosClientes());

    }


    @PatchMapping("/{id}/atualizacao")
    public ResponseEntity<ClienteResponse> atualizar(@Valid @RequestBody ClienteRequest request, @PathVariable Long id){
        return ResponseEntity.ok(clienteService.atualizar(request, id));

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id){
        clienteService.deletar(id);
        return ResponseEntity.ok().body("cliente deletado com sucesso");

    }


}
