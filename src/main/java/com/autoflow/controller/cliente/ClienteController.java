package com.autoflow.controller.cliente;


import com.autoflow.service.cliente.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    ClienteService clienteService;


    @PostMapping
    public ResponseEntity<ClienteSaida> cadastrarCliente(@RequestBody ClienteEntrada entrada ){
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.cadastrar(entrada));

    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteSaida> listarCliente(@PathVariable Long id ){
            return ResponseEntity.ok(clienteService.listar(id));
    }

    @GetMapping
    public ResponseEntity<List<ClienteSaida>> listarTodosClientes(){
        return ResponseEntity.ok(clienteService.listarTodosClientes());

    }

    @PatchMapping("/{id}/atualizacao")
    public ResponseEntity<ClienteSaida> atualizarCliente(@RequestBody ClienteEntrada entrada, @PathVariable Long id){
        return ResponseEntity.ok(clienteService.atualizar(entrada, id));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCliente(@PathVariable Long id){
        clienteService.deletar(id);
        return ResponseEntity.ok().body("cliente deletado com sucesso");

    }


}
