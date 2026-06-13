package com.autoflow.repository.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.service.orcamento.dto.OrcamentoFiltro;
import org.springframework.data.jpa.domain.Specification;

public final class OrcamentoSpecifications {
    private OrcamentoSpecifications(){}


    public static Specification<OrcamentoEntity> comFiltros(OrcamentoFiltro filtro) {
        return Specification.allOf(
                status(filtro.status()),
                numeroOs(filtro.numeroOs()),
                placa(filtro.placa()),
                clienteEmail(filtro.clienteEmail()),
                clienteDocumento(filtro.clienteDocumento()),
                tipo(filtro.tipo())
        );
    }

    private static Specification<OrcamentoEntity> status(Object status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<OrcamentoEntity> numeroOs(String numeroOs) {
        return (root, query, cb) ->
                isBlank(numeroOs) ? null : cb.equal(cb.lower(root.get("numeroOs")), numeroOs.toLowerCase());
    }

    private static Specification<OrcamentoEntity> placa(String placa) {
        return (root, query, cb) ->
                isBlank(placa) ? null : cb.equal(cb.lower(root.get("veiculo").get("placa")), placa.toLowerCase());
    }

    private static Specification<OrcamentoEntity> clienteEmail(String email) {
        return (root, query, cb) ->
                isBlank(email) ? null : cb.equal(cb.lower(root.get("cliente").get("email")), email.toLowerCase());
    }

    private static Specification<OrcamentoEntity> clienteDocumento(String documento) {
        return (root, query, cb) ->
                isBlank(documento) ? null : cb.equal(root.get("cliente").get("cpfCnpj"), documento);
    }

    private static Specification<OrcamentoEntity> tipo(Object tipo) {
        return (root, query, cb) ->
                tipo == null ? null : cb.equal(root.get("tipo"), tipo);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
