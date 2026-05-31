package com.autoflow.domain.ordemservico;

import com.autoflow.domain.pecaInsumo.CategoriaPecaInsumo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.math.BigDecimal;

@Embeddable
@Data
@EqualsAndHashCode
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

    @Column(name ="status")
    @Enumerated(EnumType.STRING)
    private StatusItemNecessario status;


    public static ItemNecessarioEntity criar(Long id, String nome, CategoriaPecaInsumo tipo, BigDecimal valor, Integer quantidade, StatusItemNecessario status) {
        ItemNecessarioEntity item = new ItemNecessarioEntity();
        item.setPecaInsumoId(id);
        item.setNome(nome);
        item.setTipo(tipo);
        item.setValorUnitario(valor);
        item.setQuantidade(quantidade);
        item.setValorTotal(valor.multiply(BigDecimal.valueOf(quantidade)));
        item.setStatus(status);
        return item;
    }
}
