package com.autoflow.domain.orcamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Orcamento {
    private Long id;

    private Long ordemServicoId;

    private String numeroOs;

    private TipoOrcamento tipo;

    private Integer versao;

    private StatusOrcamento status;

    private LocalDateTime criadoEm;

    private LocalDateTime disponibilizadoEm;

    private BigDecimal totalServicos;

    private BigDecimal totalItens;

    private BigDecimal totalGeral;

    private String publicTokenHash;

    private LocalDateTime publicTokenExpiraEm;

    private LocalDateTime aprovadoEm;

    private LocalDateTime reprovadoEm;

    private String assinaturaNome;

    private String recusaMotivo;

    private List<OrcamentoServico> servicos;

    private List<OrcamentoItemNecessario> itens;

    private ClienteOrcamentoSnapshot cliente;

    private VeiculoOrcamentoSnapshot veiculo;

    public Orcamento() {
    }

    public Orcamento(
            Long id,
            Long ordemServicoId,
            String numeroOs,
            TipoOrcamento tipo,
            Integer versao,
            StatusOrcamento status,
            LocalDateTime criadoEm,
            LocalDateTime disponibilizadoEm,
            BigDecimal totalServicos,
            BigDecimal totalItens,
            BigDecimal totalGeral,
            String publicTokenHash,
            LocalDateTime publicTokenExpiraEm,
            LocalDateTime aprovadoEm,
            LocalDateTime reprovadoEm,
            String assinaturaNome,
            String recusaMotivo,
            List<OrcamentoServico> servicos,
            List<OrcamentoItemNecessario> itens,
            ClienteOrcamentoSnapshot cliente,
            VeiculoOrcamentoSnapshot veiculo) {
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.numeroOs = numeroOs;
        this.tipo = tipo;
        this.versao = versao;
        this.status = status;
        this.criadoEm = criadoEm;
        this.disponibilizadoEm = disponibilizadoEm;
        this.totalServicos = totalServicos;
        this.totalItens = totalItens;
        this.totalGeral = totalGeral;
        this.publicTokenHash = publicTokenHash;
        this.publicTokenExpiraEm = publicTokenExpiraEm;
        this.aprovadoEm = aprovadoEm;
        this.reprovadoEm = reprovadoEm;
        this.assinaturaNome = assinaturaNome;
        this.recusaMotivo = recusaMotivo;
        this.servicos = servicos;
        this.itens = itens;
        this.cliente = java.util.Objects.requireNonNull(cliente, "cliente");
        this.veiculo = java.util.Objects.requireNonNull(veiculo, "veiculo");
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrdemServicoId() {
        return ordemServicoId;
    }

    public void setOrdemServicoId(Long ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

    public String getNumeroOs() {
        return numeroOs;
    }

    public void setNumeroOs(String numeroOs) {
        this.numeroOs = numeroOs;
    }

    public TipoOrcamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoOrcamento tipo) {
        this.tipo = tipo;
    }

    public Integer getVersao() {
        return versao;
    }

    public void setVersao(Integer versao) {
        this.versao = versao;
    }

    public StatusOrcamento getStatus() {
        return status;
    }

    public void setStatus(StatusOrcamento status) {
        this.status = status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getDisponibilizadoEm() {
        return disponibilizadoEm;
    }

    public void setDisponibilizadoEm(LocalDateTime disponibilizadoEm) {
        this.disponibilizadoEm = disponibilizadoEm;
    }

    public BigDecimal getTotalServicos() {
        return totalServicos;
    }

    public void setTotalServicos(BigDecimal totalServicos) {
        this.totalServicos = totalServicos;
    }

    public BigDecimal getTotalItens() {
        return totalItens;
    }

    public void setTotalItens(BigDecimal totalItens) {
        this.totalItens = totalItens;
    }

    public BigDecimal getTotalGeral() {
        return totalGeral;
    }

    public void setTotalGeral(BigDecimal totalGeral) {
        this.totalGeral = totalGeral;
    }

    public String getPublicTokenHash() {
        return publicTokenHash;
    }

    public void setPublicTokenHash(String publicTokenHash) {
        this.publicTokenHash = publicTokenHash;
    }

    public LocalDateTime getPublicTokenExpiraEm() {
        return publicTokenExpiraEm;
    }

    public void setPublicTokenExpiraEm(LocalDateTime publicTokenExpiraEm) {
        this.publicTokenExpiraEm = publicTokenExpiraEm;
    }

    public LocalDateTime getAprovadoEm() {
        return aprovadoEm;
    }

    public void setAprovadoEm(LocalDateTime aprovadoEm) {
        this.aprovadoEm = aprovadoEm;
    }

    public LocalDateTime getReprovadoEm() {
        return reprovadoEm;
    }

    public void setReprovadoEm(LocalDateTime reprovadoEm) {
        this.reprovadoEm = reprovadoEm;
    }

    public String getAssinaturaNome() {
        return assinaturaNome;
    }

    public void setAssinaturaNome(String assinaturaNome) {
        this.assinaturaNome = assinaturaNome;
    }

    public String getRecusaMotivo() {
        return recusaMotivo;
    }

    public void setRecusaMotivo(String recusaMotivo) {
        this.recusaMotivo = recusaMotivo;
    }

    public List<OrcamentoServico> getServicos() {
        return servicos;
    }

    public void setServicos(List<OrcamentoServico> servicos) {
        this.servicos = servicos;
    }

    public List<OrcamentoItemNecessario> getItens() {
        return itens;
    }

    public void setItens(List<OrcamentoItemNecessario> itens) {
        this.itens = itens;
    }

    public ClienteOrcamentoSnapshot getCliente() {
        return cliente;
    }

    public void setCliente(ClienteOrcamentoSnapshot cliente) {
        this.cliente = java.util.Objects.requireNonNull(cliente, "cliente");
    }

    public VeiculoOrcamentoSnapshot getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(VeiculoOrcamentoSnapshot veiculo) {
        this.veiculo = Objects.requireNonNull(veiculo, "veiculo");
    }

    public void aprovar(String assinaturaNome, LocalDateTime aprovadoEm) {
        validarDisponivel();
        this.status = StatusOrcamento.APROVADO;
        this.assinaturaNome = assinaturaNome;
        this.aprovadoEm = Objects.requireNonNull(aprovadoEm, "aprovadoEm");
    }

    public void recusar(String motivo, String assinaturaNome, LocalDateTime reprovadoEm) {
        validarDisponivel();
        this.status = StatusOrcamento.REPROVADO;
        this.assinaturaNome = assinaturaNome;
        this.recusaMotivo = motivo;
        this.reprovadoEm = Objects.requireNonNull(reprovadoEm, "reprovadoEm");
    }

    public void publicar(String tokenHash, LocalDateTime tokenExpiraEm, LocalDateTime disponibilizadoEm) {
        validarDisponivel();
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("Hash do token público é obrigatório.");
        }
        LocalDateTime expiraEm = Objects.requireNonNull(tokenExpiraEm, "tokenExpiraEm");
        LocalDateTime publicadoEm = Objects.requireNonNull(disponibilizadoEm, "disponibilizadoEm");
        this.publicTokenHash = tokenHash;
        this.publicTokenExpiraEm = expiraEm;
        if (this.disponibilizadoEm == null) {
            this.disponibilizadoEm = publicadoEm;
        }
    }

    private void validarDisponivel() {
        if (status != StatusOrcamento.DISPONIVEL) {
            throw new IllegalStateException("O orçamento deve estar disponível para esta operação.");
        }
    }

    public static final class Builder {
        private Long id;
        private Long ordemServicoId;
        private String numeroOs;
        private TipoOrcamento tipo;
        private Integer versao;
        private StatusOrcamento status;
        private LocalDateTime criadoEm;
        private LocalDateTime disponibilizadoEm;
        private BigDecimal totalServicos;
        private BigDecimal totalItens;
        private BigDecimal totalGeral;
        private String publicTokenHash;
        private LocalDateTime publicTokenExpiraEm;
        private LocalDateTime aprovadoEm;
        private LocalDateTime reprovadoEm;
        private String assinaturaNome;
        private String recusaMotivo;
        private List<OrcamentoServico> servicos;
        private List<OrcamentoItemNecessario> itens;
        private ClienteOrcamentoSnapshot cliente;
        private VeiculoOrcamentoSnapshot veiculo;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder ordemServicoId(Long value) { this.ordemServicoId = value; return this; }
        public Builder numeroOs(String value) { this.numeroOs = value; return this; }
        public Builder tipo(TipoOrcamento value) { this.tipo = value; return this; }
        public Builder versao(Integer value) { this.versao = value; return this; }
        public Builder status(StatusOrcamento value) { this.status = value; return this; }
        public Builder criadoEm(LocalDateTime value) { this.criadoEm = value; return this; }
        public Builder disponibilizadoEm(LocalDateTime value) { this.disponibilizadoEm = value; return this; }
        public Builder totalServicos(BigDecimal value) { this.totalServicos = value; return this; }
        public Builder totalItens(BigDecimal value) { this.totalItens = value; return this; }
        public Builder totalGeral(BigDecimal value) { this.totalGeral = value; return this; }
        public Builder publicTokenHash(String value) { this.publicTokenHash = value; return this; }
        public Builder publicTokenExpiraEm(LocalDateTime value) { this.publicTokenExpiraEm = value; return this; }
        public Builder aprovadoEm(LocalDateTime value) { this.aprovadoEm = value; return this; }
        public Builder reprovadoEm(LocalDateTime value) { this.reprovadoEm = value; return this; }
        public Builder assinaturaNome(String value) { this.assinaturaNome = value; return this; }
        public Builder recusaMotivo(String value) { this.recusaMotivo = value; return this; }
        public Builder servicos(List<OrcamentoServico> value) { this.servicos = value; return this; }
        public Builder itens(List<OrcamentoItemNecessario> value) { this.itens = value; return this; }
        public Builder cliente(ClienteOrcamentoSnapshot value) { this.cliente = value; return this; }
        public Builder veiculo(VeiculoOrcamentoSnapshot value) { this.veiculo = value; return this; }

        public Orcamento build() {
            return new Orcamento(
                    id,
                    ordemServicoId,
                    numeroOs,
                    tipo,
                    versao,
                    status,
                    criadoEm,
                    disponibilizadoEm,
                    totalServicos,
                    totalItens,
                    totalGeral,
                    publicTokenHash,
                    publicTokenExpiraEm,
                    aprovadoEm,
                    reprovadoEm,
                    assinaturaNome,
                    recusaMotivo,
                    servicos,
                    itens,
                    cliente,
                    veiculo);
        }
    }
}

