package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.application.dto.veiculo.VeiculoFiltro;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import org.springframework.data.jpa.domain.Specification;

public final class VeiculoSpecifications {

    private VeiculoSpecifications() {
    }

    public static Specification<VeiculoEntity> comFiltros(VeiculoFiltro filtro) {
        return Specification.allOf(
                placa(filtro.placa()),
                marca(filtro.marca()),
                modelo(filtro.modelo()),
                ano(filtro.ano()),
                clienteId(filtro.clienteId())
        );
    }

    private static Specification<VeiculoEntity> placa(String placa) {
        return (root, query, cb) ->
                isBlank(placa) ? null : cb.equal(cb.lower(root.get("placa")), placa.replaceAll("[^A-Za-z0-9]", "").toLowerCase());
    }

    private static Specification<VeiculoEntity> marca(String marca) {
        return (root, query, cb) ->
                isBlank(marca) ? null : cb.like(cb.lower(root.get("marca")), "%" + marca.toLowerCase() + "%");
    }

    private static Specification<VeiculoEntity> modelo(String modelo) {
        return (root, query, cb) ->
                isBlank(modelo) ? null : cb.like(cb.lower(root.get("modelo")), "%" + modelo.toLowerCase() + "%");
    }

    private static Specification<VeiculoEntity> ano(Integer ano) {
        return (root, query, cb) ->
                ano == null ? null : cb.equal(root.get("ano"), ano);
    }

    private static Specification<VeiculoEntity> clienteId(Long clienteId) {
        return (root, query, cb) ->
                clienteId == null ? null : cb.equal(root.get("cliente").get("id"), clienteId);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
