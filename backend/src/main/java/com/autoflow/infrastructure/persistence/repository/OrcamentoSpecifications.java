package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.application.dto.orcamento.OrcamentoFiltro;
import com.autoflow.infrastructure.persistence.entity.orcamento.OrcamentoPersistenceEntity;
import org.springframework.data.jpa.domain.Specification;

public final class OrcamentoSpecifications {
    private OrcamentoSpecifications() {
    }


    public static Specification<OrcamentoPersistenceEntity> comFiltros(OrcamentoFiltro filtro) {
        return Specification.allOf(
                status(filtro.status()),
                numeroOs(filtro.numeroOs()),
                placa(filtro.placa()),
                clienteEmail(filtro.clienteEmail()),
                clienteDocumento(filtro.clienteDocumento()),
                tipo(filtro.tipo())
        );
    }

    private static Specification<OrcamentoPersistenceEntity> status(Object status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<OrcamentoPersistenceEntity> numeroOs(String numeroOs) {
        return (root, query, cb) ->
                isBlank(numeroOs) ? null : cb.equal(cb.lower(root.get("numeroOs")), numeroOs.toLowerCase());
    }

    private static Specification<OrcamentoPersistenceEntity> placa(String placa) {
        return (root, query, cb) ->
                isBlank(placa) ? null : cb.equal(cb.lower(root.get("veiculo").get("placa")), placa.toLowerCase());
    }

    private static Specification<OrcamentoPersistenceEntity> clienteEmail(String email) {
        return (root, query, cb) ->
                isBlank(email) ? null : cb.equal(cb.lower(root.get("cliente").get("email")), email.toLowerCase());
    }

    private static Specification<OrcamentoPersistenceEntity> clienteDocumento(String documento) {
        return (root, query, cb) ->
                isBlank(documento) ? null : cb.equal(root.get("cliente").get("cpfCnpj"), documento);
    }

    private static Specification<OrcamentoPersistenceEntity> tipo(Object tipo) {
        return (root, query, cb) ->
                tipo == null ? null : cb.equal(root.get("tipo"), tipo);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
