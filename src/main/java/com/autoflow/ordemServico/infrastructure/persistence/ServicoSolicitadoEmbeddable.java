package com.autoflow.ordemServico.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServicoSolicitadoEmbeddable {

    @Column(name = "servico_id", nullable = false)
    private UUID servicoId;

    @Column(nullable = false)
    private String nome;

}
