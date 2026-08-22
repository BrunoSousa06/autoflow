package com.autoflow.infrastructure.persistence.entity.ordemservico.reparoadicional;

import com.autoflow.domain.ordemservico.reparoadicional.StatusReparoAdicional;
import com.autoflow.infrastructure.persistence.entity.ordemservico.ServicoSolicitadoEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reparo_adicional")
@Getter
@Setter
@NoArgsConstructor
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
}
