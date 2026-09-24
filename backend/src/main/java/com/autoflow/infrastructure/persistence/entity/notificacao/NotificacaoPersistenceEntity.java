package com.autoflow.infrastructure.persistence.entity.notificacao;

import com.autoflow.domain.notificacao.CanalNotificacao;
import com.autoflow.domain.notificacao.StatusNotificacao;
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
public class NotificacaoPersistenceEntity {
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
}
