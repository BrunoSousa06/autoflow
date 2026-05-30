package com.autoflow.controller.veiculo;


import com.autoflow.controller.veiculo.request.VeiculoRequest;
import com.autoflow.controller.veiculo.response.VeiculoResponse;
import com.autoflow.service.veiculo.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
public class VeiculoController {

    private final VeiculoService veiculoService;


    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(@RequestBody VeiculoRequest request ){
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculoService.cadastrar(request));

    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> listar(@PathVariable Long id ){
        return ResponseEntity.ok(veiculoService.listar(id));
    }

    @GetMapping
    public ResponseEntity<List<VeiculoResponse>> listarTodosVeiculos(){
        return ResponseEntity.ok(veiculoService.listarTodosVeiculos());

    }

    @PatchMapping("/{id}/atualizacao")
    public ResponseEntity<VeiculoResponse> atualizar(@RequestBody VeiculoRequest request, @PathVariable Long id){
        return ResponseEntity.ok(veiculoService.atualizar(request, id));

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id){
        veiculoService.deletar(id);
        return ResponseEntity.ok().body("veiculo deletado com sucesso");

    }


}
