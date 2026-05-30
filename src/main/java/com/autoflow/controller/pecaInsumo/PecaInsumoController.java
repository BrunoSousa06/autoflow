package com.autoflow.controller.pecaInsumo;

import com.autoflow.controller.pecaInsumo.request.PecaInsumoRequest;
import com.autoflow.controller.pecaInsumo.response.PecaInsumoResponse;
import com.autoflow.service.pecaInsumo.PecaInsumoService;
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
public class PecaInsumoController {

    private final PecaInsumoService pecaInsumoService;


    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public ResponseEntity<PecaInsumoResponse> cadastrar(@Valid @RequestBody PecaInsumoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pecaInsumoService.cadastrar(request));

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<PecaInsumoResponse> listar(@PathVariable Long id) {
        return ResponseEntity.ok(pecaInsumoService.buscarPorId(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<List<PecaInsumoResponse>> listarTodasPecasEInsumos() {
        return ResponseEntity.ok(pecaInsumoService.listar());

    }

    @PatchMapping("/{id}/atualizacao")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public ResponseEntity<PecaInsumoResponse> atualizar(@Valid @RequestBody PecaInsumoRequest request, @PathVariable Long id) {
        return ResponseEntity.ok(pecaInsumoService.atualizar(request, id));

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public ResponseEntity<?> deletar(@PathVariable Long id){
        pecaInsumoService.deletar(id);
        return ResponseEntity.ok().body("peca/insumo deletado com sucesso");

    }
}
