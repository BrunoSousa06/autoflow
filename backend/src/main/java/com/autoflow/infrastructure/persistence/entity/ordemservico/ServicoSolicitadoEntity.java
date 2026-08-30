package com.autoflow.infrastructure.persistence.entity.ordemservico;

import com.autoflow.domain.ordemservico.StatusServicoOs;
import com.autoflow.infrastructure.persistence.entity.ordemservico.reparoadicional.ReparoAdicionalEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordem_servico_servico_solicitado")
@Getter
@Setter
@NoArgsConstructor
public class ServicoSolicitadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "servico_id", nullable = false)
    private Long servicoId;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private BigDecimal valor;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusServicoOs status = StatusServicoOs.AGUARDANDO;
    @Column(name = "iniciado_em")
    private LocalDateTime iniciadoEm;
    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id")
    private OrdemServicoEntity ordemServico;
    @ElementCollection
    @CollectionTable(name = "ordem_servico_servico_item_necessario", joinColumns = @JoinColumn(name = "servico_solicitado_id"))
    @OrderColumn(name = "ordem")
    @BatchSize(size = 50)
    private List<ItemNecessarioEntity> itensNecessarios = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "reparo_adicional_id")
    private ReparoAdicionalEntity reparoAdicional;
}
