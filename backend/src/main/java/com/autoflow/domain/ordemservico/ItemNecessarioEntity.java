package com.autoflow.domain.ordemservico;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_pendencia")
    private MotivoPendenciaItem motivoPendencia;

    @Column(name = "quantidade_disponivel")
    private Integer quantidadeDisponivel;

    @Column(name = "mensagem_status")
    private String mensagemStatus;


    public static ItemNecessarioEntity criar(
            Long id,
            String nome,
            CategoriaPecaInsumo tipo,
            BigDecimal valor,
            Integer quantidade,
            StatusItemNecessario status
    ) {
        return criar(id, nome, tipo, valor, quantidade, status, new SituacaoEstoque(null, null));
    }


    public static ItemNecessarioEntity criar(
            Long id,
            String nome,
            CategoriaPecaInsumo tipo,
            BigDecimal valor,
            Integer quantidade,
            StatusItemNecessario status,
            SituacaoEstoque situacaoEstoque
    ) {
        ItemNecessarioEntity item = new ItemNecessarioEntity();
        item.setPecaInsumoId(id);
        item.setNome(nome);
        item.setTipo(tipo);
        item.setValorUnitario(valor);
        item.setQuantidade(quantidade);
        item.setValorTotal(valor.multiply(BigDecimal.valueOf(quantidade)));
        item.setStatus(status);
        item.setQuantidadeDisponivel(situacaoEstoque.quantidadeDisponivel());
        item.setMotivoPendencia(situacaoEstoque.motivoPendencia());
        item.setMensagemStatus(criarMensagemStatus(
                status,
                quantidade,
                situacaoEstoque.quantidadeDisponivel(),
                situacaoEstoque.motivoPendencia()
        ));
        return item;
    }

    private static String criarMensagemStatus(
            StatusItemNecessario status,
            Integer quantidadeSolicitada,
            Integer quantidadeDisponivel,
            MotivoPendenciaItem motivoPendencia
    ) {
        if (StatusItemNecessario.PENDENTE.equals(status)
                && MotivoPendenciaItem.ESTOQUE_INSUFICIENTE.equals(motivoPendencia)) {
            return "Estoque insuficiente. Solicitado: "
                    + quantidadeSolicitada
                    + ", disponivel: "
                    + quantidadeDisponivel
                    + ".";
        }

        return null;
    }
}
