package com.autoflow.domain.notificacao;

import java.time.LocalDateTime;

public class NotificacaoEntity {

    private Long id;

    private Long orcamentoId;

    private Long clienteId;

    private CanalNotificacao canal;

    private String destinatario;

    private StatusNotificacao status;

    private LocalDateTime enviadaEm;

    private String mensagemErro;

    private LocalDateTime criadaEm;

    public NotificacaoEntity() {
    }

    public static NotificacaoEntity pendente(
            Long orcamentoId,
            Long clienteId,
            String destinatario
    ) {
        NotificacaoEntity notificacao = new NotificacaoEntity();
        notificacao.orcamentoId = orcamentoId;
        notificacao.clienteId = clienteId;
        notificacao.canal = CanalNotificacao.EMAIL;
        notificacao.destinatario = destinatario;
        notificacao.status = StatusNotificacao.PENDENTE;
        notificacao.criadaEm = LocalDateTime.now();
        return notificacao;
    }

    public void marcarComoEnviada() {
        this.status = StatusNotificacao.ENVIADA;
        this.enviadaEm = LocalDateTime.now();
        this.mensagemErro = null;
    }

    public void marcarComoFalha(String mensagemErro) {
        this.status = StatusNotificacao.FALHA;
        this.mensagemErro = mensagemErro;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrcamentoId() {
        return orcamentoId;
    }

    public void setOrcamentoId(Long orcamentoId) {
        this.orcamentoId = orcamentoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public CanalNotificacao getCanal() {
        return canal;
    }

    public void setCanal(CanalNotificacao canal) {
        this.canal = canal;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public StatusNotificacao getStatus() {
        return status;
    }

    public void setStatus(StatusNotificacao status) {
        this.status = status;
    }

    public LocalDateTime getEnviadaEm() {
        return enviadaEm;
    }

    public void setEnviadaEm(LocalDateTime enviadaEm) {
        this.enviadaEm = enviadaEm;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }
}
