package com.autoflow.controller.veiculo;


import com.autoflow.service.veiculo.VeiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    @Autowired
    VeiculoService veiculoService;


    @PostMapping("/cadastro")
    public ResponseEntity<VeiculoSaida> cadastrarVeiculo(@RequestBody VeiculoEntrada entrada ){
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculoService.cadastrar(entrada));

    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoSaida> listarCliente(@PathVariable Long id ){
        return ResponseEntity.ok(veiculoService.listar(id));
    }

    @GetMapping()
    public ResponseEntity<List<VeiculoSaida>> listarTodosVeiculos(){
        return ResponseEntity.ok(veiculoService.listarTodosVeiculos());

    }

    @PatchMapping("/{id}/atualizacao")
    public ResponseEntity<VeiculoSaida> atualizarCliente(@RequestBody VeiculoEntrada entrada, @PathVariable Long id){
        return ResponseEntity.ok(veiculoService.atualizar(entrada, id));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCliente(@PathVariable Long id){
        veiculoService.deletar(id);
        return ResponseEntity.ok().body("veiculo deletado com sucesso");

    }


}
