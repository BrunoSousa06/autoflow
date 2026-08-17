package com.autoflow.domain.notificacao;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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
}
