package com.autoflow.domain.ordemServico;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.Long;


@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServicoSolicitadoEntity {

    @Column(name = "servico_id", nullable = false)
    private Long servicoId;

    @Column(nullable = false)
    private String nome;

    public ServicoSolicitadoEntity(Long servicoId, String nome) {
        if (servicoId == null) {
            throw new IllegalArgumentException("Servico e obrigatorio.");
        }

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do servico e obrigatorio.");
        }

        this.servicoId = servicoId;
        this.nome = nome;
    }
}
