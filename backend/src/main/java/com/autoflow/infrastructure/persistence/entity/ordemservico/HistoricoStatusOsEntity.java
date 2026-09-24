package com.autoflow.infrastructure.persistence.entity.ordemservico;

import com.autoflow.domain.ordemservico.StatusOrdemServico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordem_servico_status_historico")
@Getter
@Setter
@NoArgsConstructor
public class HistoricoStatusOsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ordem_servico_id", nullable = false)
    private Long ordemServicoId;
    private String numeroOs;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrdemServico status;
    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;
    @Column(name = "mensagem_cliente", nullable = false)
    private String mensagemCliente;
}
