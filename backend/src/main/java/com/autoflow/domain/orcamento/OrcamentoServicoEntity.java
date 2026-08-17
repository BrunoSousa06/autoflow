package com.autoflow.domain.orcamento;

import java.math.BigDecimal;

public class OrcamentoServicoEntity {

    private Long servicoId;

    private String nome;

    private BigDecimal valor;

    public OrcamentoServicoEntity() {
    }

    public OrcamentoServicoEntity(Long servicoId, String nome, BigDecimal valor) {
        this.servicoId = servicoId;
        this.nome = nome;
        this.valor = valor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getServicoId() {
        return servicoId;
    }

    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public static final class Builder {
        private Long servicoId;
        private String nome;
        private BigDecimal valor;

        public Builder servicoId(Long servicoId) {
            this.servicoId = servicoId;
            return this;
        }

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder valor(BigDecimal valor) {
            this.valor = valor;
            return this;
        }

        public OrcamentoServicoEntity build() {
            return new OrcamentoServicoEntity(servicoId, nome, valor);
        }
    }
}
