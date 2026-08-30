package com.autoflow.infrastructure.persistence.entity.ordemservico;

import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ItemNecessarioEntity {

    @Column(name = "peca_insumo_id", nullable = false)
    private Long pecaInsumoId;
    @Column(nullable = false)
    private String nome;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaPecaInsumo tipo;
    @Column(name = "valor_unitario", nullable = false)
    private BigDecimal valorUnitario;
    @Column(nullable = false)
    private Integer quantidade;
    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusItemNecessario status;
    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_pendencia")
    private MotivoPendenciaItem motivoPendencia;
    @Column(name = "quantidade_disponivel")
    private Integer quantidadeDisponivel;
    @Column(name = "mensagem_status")
    private String mensagemStatus;
}
