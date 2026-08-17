package com.autoflow.infrastructure.persistence.entity.orcamento;

import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orcamento")
@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OrcamentoPersistenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "ordem_servico_id", nullable = false)
    private Long ordemServicoId;

    @Column(name = "numero_os", nullable = false)
    private String numeroOs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOrcamento tipo;

    @Column(nullable = false)
    private Integer versao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrcamento status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "disponibilizado_em")
    private LocalDateTime disponibilizadoEm;

    @Column(name = "total_servicos", nullable = false)
    private BigDecimal totalServicos;

    @Column(name = "total_itens", nullable = false)
    private BigDecimal totalItens;

    @Column(name = "total_geral", nullable = false)
    private BigDecimal totalGeral;

    @Column(name = "public_token_hash", nullable = true)
    private String publicTokenHash;

    private LocalDateTime publicTokenExpiraEm;

    @Column(name = "aprovado_em")
    private LocalDateTime aprovadoEm;

    @Column(name = "reprovado_em")
    private LocalDateTime reprovadoEm;

    @Column(name = "assinatura_nome")
    private String assinaturaNome;

    @Column(name = "recusa_motivo")
    private String recusaMotivo;

    @ElementCollection
    @CollectionTable(name = "orcamento_servico_item", joinColumns = @JoinColumn(name = "orcamento_id"))
    @OrderColumn(name = "ordem")
    private List<OrcamentoServicoPersistenceEntity> servicos;

    @ElementCollection
    @CollectionTable(name = "orcamento_item_necessario_item", joinColumns = @JoinColumn(name = "orcamento_id"))
    @OrderColumn(name = "ordem")
    private List<OrcamentoItemNecessarioPersistenceEntity> itens;

    @Embedded
    @NonNull
    private ClienteOrcamentoSnapshotPersistenceEntity cliente;

    @Embedded
    @NonNull
    private VeiculoOrcamentoSnapshotPersistenceEntity veiculo;
}
