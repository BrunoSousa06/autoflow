package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import org.springframework.data.jpa.domain.Specification;

public final class PecaInsumoSpecifications {

    private PecaInsumoSpecifications() {
    }

    public static Specification<PecaInsumoEntity> comFiltros(String nome, CategoriaPecaInsumo tipo) {
        return Specification.allOf(
                porNome(nome),
                porTipo(tipo)
        );
    }

    private static Specification<PecaInsumoEntity> porNome(String nome) {
        return (root, query, cb) ->
                isBlank(nome) ? null : cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
    }

    private static Specification<PecaInsumoEntity> porTipo(CategoriaPecaInsumo tipo) {
        return (root, query, cb) ->
                tipo == null ? null : cb.equal(root.get("tipo"), tipo);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
