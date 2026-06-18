package com.autoflow.repository.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.service.ordemservico.dto.OrdemServicoFiltro;
import org.springframework.data.jpa.domain.Specification;

public final class OrdemServicoSpecifications {

    private OrdemServicoSpecifications() {}

    public static Specification<OrdemServicoEntity> comFiltros(OrdemServicoFiltro filtro, String emailMecanico) {
        return Specification.allOf(
                status(filtro.status()),
                numeroOs(filtro.numeroOs()),
                cliente(filtro.cliente()),
                mecanico(emailMecanico)
        );
    }

    private static Specification<OrdemServicoEntity> status(com.autoflow.domain.ordemservico.StatusOrdemServico status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<OrdemServicoEntity> numeroOs(String numeroOs) {
        return (root, query, cb) -> {
            if (isBlank(numeroOs)) return null;
            return cb.like(cb.lower(root.get("numeroOs")), "%" + numeroOs.toLowerCase() + "%");
        };
    }

    private static Specification<OrdemServicoEntity> cliente(String cliente) {
        return (root, query, cb) -> {
            if (isBlank(cliente)) return null;
            String pattern = "%" + cliente.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("cliente").get("nome")), pattern),
                    cb.like(cb.lower(root.get("cliente").get("cpfCnpj")), pattern)
            );
        };
    }

    private static Specification<OrdemServicoEntity> mecanico(String emailMecanico) {
        return (root, query, cb) -> {
            if (isBlank(emailMecanico)) return null;
            return cb.equal(root.get("diagnostico").get("mecanico").get("email"), emailMecanico);
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
