package com.autoflow.domain.notificacao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
@Getter
@Setter
@NoArgsConstructor
public class NotificacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orcamentoId;

    private Long clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalNotificacao canal;

    @Column(nullable = false)
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusNotificacao status;

    private LocalDateTime enviadaEm;

    @Column(length = 1000)
    private String mensagemErro;

    @Column(nullable = false)
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
