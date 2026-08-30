package com.autoflow.domain.orcamento;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

import java.math.BigDecimal;

public class OrcamentoItemNecessario {

    private Long pecaInsumoId;

    private Long servicoOsId;

    private String nome;

    private CategoriaPecaInsumo tipo;

    private BigDecimal valorUnitario;

    private Integer quantidade;

    private BigDecimal valorTotal;

    public OrcamentoItemNecessario() {
    }

    public OrcamentoItemNecessario(
            Long pecaInsumoId,
            Long servicoOsId,
            String nome,
            CategoriaPecaInsumo tipo,
            BigDecimal valorUnitario,
            Integer quantidade,
            BigDecimal valorTotal) {
        this.pecaInsumoId = pecaInsumoId;
        this.servicoOsId = servicoOsId;
        this.nome = nome;
        this.tipo = tipo;
        this.valorUnitario = valorUnitario;
        this.quantidade = quantidade;
        this.valorTotal = valorTotal;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getPecaInsumoId() {
        return pecaInsumoId;
    }

    public void setPecaInsumoId(Long pecaInsumoId) {
        this.pecaInsumoId = pecaInsumoId;
    }

    public Long getServicoOsId() {
        return servicoOsId;
    }

    public void setServicoOsId(Long servicoOsId) {
        this.servicoOsId = servicoOsId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public CategoriaPecaInsumo getTipo() {
        return tipo;
    }

    public void setTipo(CategoriaPecaInsumo tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public static final class Builder {
        private Long pecaInsumoId;
        private Long servicoOsId;
        private String nome;
        private CategoriaPecaInsumo tipo;
        private BigDecimal valorUnitario;
        private Integer quantidade;
        private BigDecimal valorTotal;

        public Builder pecaInsumoId(Long pecaInsumoId) {
            this.pecaInsumoId = pecaInsumoId;
            return this;
        }

        public Builder servicoOsId(Long servicoOsId) {
            this.servicoOsId = servicoOsId;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder tipo(CategoriaPecaInsumo tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder valorUnitario(BigDecimal valorUnitario) {
            this.valorUnitario = valorUnitario;
            return this;
        }

        public Builder quantidade(Integer quantidade) {
            this.quantidade = quantidade;
            return this;
        }

        public Builder valorTotal(BigDecimal valorTotal) {
            this.valorTotal = valorTotal;
            return this;
        }

        public OrcamentoItemNecessario build() {
            return new OrcamentoItemNecessario(
                    pecaInsumoId,
                    servicoOsId,
                    nome,
                    tipo,
                    valorUnitario,
                    quantidade,
                    valorTotal);
        }
    }
}
