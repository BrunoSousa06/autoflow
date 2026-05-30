package com.autoflow.controller.servico;


import com.autoflow.controller.servico.request.ServicoRequest;
import com.autoflow.controller.servico.response.ServicoResponse;
import com.autoflow.service.servico.ServicoService;
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
public class ServicoController {

    private final ServicoService servicoService;


    @PostMapping
    @PreAuthorize("hasAnyRole('MECANICO', 'ADMIN')")
    public ResponseEntity<ServicoResponse> cadastrar(@Valid @RequestBody ServicoRequest request ){
        return ResponseEntity.status(HttpStatus.CREATED).body(servicoService.cadastrar(request));

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<ServicoResponse> listar(@PathVariable Long id ){
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<List<ServicoResponse>> listarTodosServicos(){
        return ResponseEntity.ok(servicoService.listar());

    }

    @PatchMapping("/{id}/atualizacao")
    @PreAuthorize("hasAnyRole('MECANICO', 'ADMIN')")
    public ResponseEntity<ServicoResponse> atualizar(@Valid @RequestBody ServicoRequest request, @PathVariable Long id){
        return ResponseEntity.ok(servicoService.atualizar(request, id));

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id){
        servicoService.deletar(id);
        return ResponseEntity.ok().body("serviço deletado com sucesso");

    }


}
