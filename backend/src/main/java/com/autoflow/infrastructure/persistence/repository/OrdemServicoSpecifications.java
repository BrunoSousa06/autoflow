package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.infrastructure.persistence.entity.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import jakarta.persistence.criteria.Expression;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class OrdemServicoSpecifications {

    static final List<StatusOrdemServico> STATUS_OPERACIONAIS = List.of(
            StatusOrdemServico.EM_EXECUCAO,
            StatusOrdemServico.AGUARDANDO_APROVACAO,
            StatusOrdemServico.EM_DIAGNOSTICO,
            StatusOrdemServico.RECEBIDA
    );

    private OrdemServicoSpecifications() {
    }

    public static Specification<OrdemServicoEntity> comFiltros(OrdemServicoFiltroInput filtro, String emailMecanico) {
        return Specification.allOf(
                statusOperacional(),
                status(filtro.status()),
                numeroOs(filtro.numeroOs()),
                cliente(filtro.cliente()),
                mecanico(emailMecanico),
                ordenacaoOperacional()
        );
    }

    private static Specification<OrdemServicoEntity> statusOperacional() {
        return (root, query, cb) -> root.get("status").in(STATUS_OPERACIONAIS);
    }

    private static Specification<OrdemServicoEntity> status(StatusOrdemServico status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<OrdemServicoEntity> ordenacaoOperacional() {
        return (root, query, cb) -> {
            Class<?> resultType = query.getResultType();
            if (resultType == null || Long.class.equals(resultType) || long.class.equals(resultType)) {
                return null;
            }

            Expression<Integer> prioridadeStatus = cb.<StatusOrdemServico, Integer>selectCase(root.<StatusOrdemServico>get("status"))
                    .when(StatusOrdemServico.EM_EXECUCAO, 1)
                    .when(StatusOrdemServico.AGUARDANDO_APROVACAO, 2)
                    .when(StatusOrdemServico.EM_DIAGNOSTICO, 3)
                    .when(StatusOrdemServico.RECEBIDA, 4)
                    .otherwise(5);

            query.orderBy(
                    cb.asc(prioridadeStatus),
                    cb.asc(root.get("dataAbertura")),
                    cb.asc(root.get("id"))
            );
            return null;
        };
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
