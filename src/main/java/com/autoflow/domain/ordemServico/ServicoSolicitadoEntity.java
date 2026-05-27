package com.autoflow.domain.ordemServico;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.Long;
import java.math.BigDecimal;


@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServicoSolicitadoEntity {

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal preco;

    public ServicoSolicitadoEntity(Long servicoId, String descricao) {
        if (servicoId == null) {
            throw new IllegalArgumentException("Servico e obrigatorio.");
        }

        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Nome do servico e obrigatorio.");
        }

        this.servicoId = servicoId;
        this.descricao = descricao;
    }
}
