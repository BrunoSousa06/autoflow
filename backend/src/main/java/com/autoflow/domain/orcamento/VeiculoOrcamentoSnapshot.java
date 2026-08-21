package com.autoflow.domain.orcamento;

import com.autoflow.domain.veiculo.Veiculo;

import java.util.Objects;

public class VeiculoOrcamentoSnapshot {

    private String placa;

    private String marca;

    private String modelo;

    private Integer ano;

    public VeiculoOrcamentoSnapshot() {
    }

    public VeiculoOrcamentoSnapshot(String placa, String marca, String modelo, Integer ano) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static VeiculoOrcamentoSnapshot from(Veiculo veiculo) {
        return VeiculoOrcamentoSnapshot.builder()
                .placa(veiculo.placa())
                .marca(veiculo.marca())
                .modelo(veiculo.modelo())
                .ano(veiculo.ano())
                .build();
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof VeiculoOrcamentoSnapshot that)) return false;
        return Objects.equals(placa, that.placa)
                && Objects.equals(marca, that.marca)
                && Objects.equals(modelo, that.modelo)
                && Objects.equals(ano, that.ano);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placa, marca, modelo, ano);
    }

    @Override
    public String toString() {
        return "VeiculoOrcamentoSnapshot{" +
                "placa='" + placa + '\'' +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", ano=" + ano +
                '}';
    }

    public static final class Builder {
        private String placa;
        private String marca;
        private String modelo;
        private Integer ano;

        public Builder placa(String placa) {
            this.placa = placa;
            return this;
        }

        public Builder marca(String marca) {
            this.marca = marca;
            return this;
        }

        public Builder modelo(String modelo) {
            this.modelo = modelo;
            return this;
        }

        public Builder ano(Integer ano) {
            this.ano = ano;
            return this;
        }

        public VeiculoOrcamentoSnapshot build() {
            return new VeiculoOrcamentoSnapshot(placa, marca, modelo, ano);
        }
    }
}
