package com.autoflow.domain.ordemservico.reparoadicional;

import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ReparoAdicionalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ordemServicoId;

    private String numeroOs;

    private Long mecanicoId;

    private Long orcamentoId;

    @Enumerated(EnumType.STRING)
    private StatusReparoAdicional status = StatusReparoAdicional.PENDENTE_APROVACAO;

    private LocalDateTime criadoEm;

    private LocalDateTime aprovadoEm;

    private LocalDateTime recusadoEm;

    private String motivoRecusa;

    @OneToMany(mappedBy = "reparoAdicional", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicoSolicitadoEntity> servicos = new ArrayList<>();

    public static ReparoAdicionalEntity criar(
            String numeroOs,
            Long mecanicoId,
            List<ServicoSolicitadoEntity> servicos
    ) {
        ReparoAdicionalEntity reparo = new ReparoAdicionalEntity();
        reparo.setNumeroOs(numeroOs);
        reparo.setMecanicoId(mecanicoId);
        reparo.setCriadoEm(LocalDateTime.now());
        reparo.setStatus(StatusReparoAdicional.PENDENTE_APROVACAO);

        servicos.forEach(servico -> {
            servico.setReparoAdicional(reparo);
            reparo.getServicos().add(servico);
        });

        return reparo;
    }

    public void aprovar() {
        if (!StatusReparoAdicional.PENDENTE_APROVACAO.equals(status)) {
            throw new IllegalStateException("Reparo adicional não está pendente de aprovação.");
        }

        this.status = StatusReparoAdicional.APROVADO;
        this.aprovadoEm = LocalDateTime.now();
    }

    public void recusar(String motivo) {
        if (!StatusReparoAdicional.PENDENTE_APROVACAO.equals(status)) {
            throw new IllegalStateException("Reparo adicional não está pendente de aprovação.");
        }

        this.status = StatusReparoAdicional.RECUSADO;
        this.recusadoEm = LocalDateTime.now();
        this.motivoRecusa = motivo;
    }
}