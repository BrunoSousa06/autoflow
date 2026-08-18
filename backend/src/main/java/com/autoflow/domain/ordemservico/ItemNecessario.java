package com.autoflow.domain.ordemservico;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

import java.math.BigDecimal;

public class ItemNecessario {

    private Long pecaInsumoId;
    private String nome;
    private CategoriaPecaInsumo tipo;
    private BigDecimal valorUnitario;
    private Integer quantidade;
    private BigDecimal valorTotal;
    private StatusItemNecessario status;
    private MotivoPendenciaItem motivoPendencia;
    private Integer quantidadeDisponivel;
    private String mensagemStatus;

    public static ItemNecessario criar(
            Long id,
            String nome,
            CategoriaPecaInsumo tipo,
            BigDecimal valor,
            Integer quantidade,
            StatusItemNecessario status) {
        return criar(id, nome, tipo, valor, quantidade, status, new SituacaoEstoque(null, null));
    }

    public static ItemNecessario criar(
            Long id,
            String nome,
            CategoriaPecaInsumo tipo,
            BigDecimal valor,
            Integer quantidade,
            StatusItemNecessario status,
            SituacaoEstoque situacaoEstoque) {
        ItemNecessario item = new ItemNecessario();
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
                status, quantidade, situacaoEstoque.quantidadeDisponivel(), situacaoEstoque.motivoPendencia()));
        return item;
    }

    private static String criarMensagemStatus(
            StatusItemNecessario status,
            Integer quantidadeSolicitada,
            Integer quantidadeDisponivel,
            MotivoPendenciaItem motivoPendencia) {
        if (StatusItemNecessario.PENDENTE.equals(status)
                && MotivoPendenciaItem.ESTOQUE_INSUFICIENTE.equals(motivoPendencia)) {
            return "Estoque insuficiente. Solicitado: " + quantidadeSolicitada
                    + ", disponivel: " + quantidadeDisponivel + ".";
        }
        return null;
    }

    public Long getPecaInsumoId() { return pecaInsumoId; }
    public void setPecaInsumoId(Long value) { this.pecaInsumoId = value; }
    public String getNome() { return nome; }
    public void setNome(String value) { this.nome = value; }
    public CategoriaPecaInsumo getTipo() { return tipo; }
    public void setTipo(CategoriaPecaInsumo value) { this.tipo = value; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal value) { this.valorUnitario = value; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer value) { this.quantidade = value; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal value) { this.valorTotal = value; }
    public StatusItemNecessario getStatus() { return status; }
    public void setStatus(StatusItemNecessario value) { this.status = value; }
    public MotivoPendenciaItem getMotivoPendencia() { return motivoPendencia; }
    public void setMotivoPendencia(MotivoPendenciaItem value) { this.motivoPendencia = value; }
    public Integer getQuantidadeDisponivel() { return quantidadeDisponivel; }
    public void setQuantidadeDisponivel(Integer value) { this.quantidadeDisponivel = value; }
    public String getMensagemStatus() { return mensagemStatus; }
    public void setMensagemStatus(String value) { this.mensagemStatus = value; }
}
