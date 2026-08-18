package com.autoflow.domain.notificacao;

import java.time.LocalDateTime;

public class Notificacao {

    private Long id;

    private Long orcamentoId;

    private Long clienteId;

    private CanalNotificacao canal;

    private String destinatario;

    private StatusNotificacao status;

    private LocalDateTime enviadaEm;

    private String mensagemErro;

    private LocalDateTime criadaEm;

    public Notificacao() {
    }

    public static Notificacao pendente(
            Long orcamentoId,
            Long clienteId,
            String destinatario
    ) {
        Notificacao notificacao = new Notificacao();
        notificacao.orcamentoId = orcamentoId;
        notificacao.clienteId = clienteId;
        notificacao.canal = CanalNotificacao.EMAIL;
        notificacao.destinatario = destinatario;
        notificacao.status = StatusNotificacao.PENDENTE;
        notificacao.criadaEm = LocalDateTime.now();
        return notificacao;
    }

    public static Notificacao reconstituir(
            Long id,
            Long orcamentoId,
            Long clienteId,
            CanalNotificacao canal,
            String destinatario,
            StatusNotificacao status,
            LocalDateTime enviadaEm,
            String mensagemErro,
            LocalDateTime criadaEm) {
        Notificacao notificacao = new Notificacao();
        notificacao.id = id;
        notificacao.orcamentoId = orcamentoId;
        notificacao.clienteId = clienteId;
        notificacao.canal = canal;
        notificacao.destinatario = destinatario;
        notificacao.status = status;
        notificacao.enviadaEm = enviadaEm;
        notificacao.mensagemErro = mensagemErro;
        notificacao.criadaEm = criadaEm;
        return notificacao;
    }

    public void marcarComoEnviada() {
        validarPodeAlterarStatus();
        this.status = StatusNotificacao.ENVIADA;
        this.enviadaEm = LocalDateTime.now();
        this.mensagemErro = null;
    }

    public void marcarComoFalha(String mensagemErro) {
        validarPodeAlterarStatus();
        this.status = StatusNotificacao.FALHA;
        this.enviadaEm = null;
        this.mensagemErro = mensagemErro;
    }

    private void validarPodeAlterarStatus() {
        if (status != StatusNotificacao.PENDENTE && status != StatusNotificacao.FALHA) {
            throw new IllegalStateException("A notificação não pode mais ser processada.");
        }
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
