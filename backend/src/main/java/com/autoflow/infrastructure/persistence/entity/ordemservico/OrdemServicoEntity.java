package com.autoflow.infrastructure.persistence.entity.ordemservico;

import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.autoflow.domain.ordemservico.StatusOrdemServico;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordem_servico")
@Getter
@Setter
@NoArgsConstructor
public class OrdemServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "numero_os", nullable = false, unique = true)
    private String numeroOs;
    @Embedded
    private ClienteOsEntity cliente;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private VeiculoEntity veiculo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrdemServico status;
    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;
    @Embedded
    private DiagnosticoEntity diagnostico;
    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicoSolicitadoEntity> servicosSolicitados = new ArrayList<>();
    @Column(name = "execucao_iniciada_em")
    private LocalDateTime execucaoIniciadaEm;
    @Column(name = "finalizada_em")
    private LocalDateTime finalizadaEm;
    @Column(name = "entregue_em")
    private LocalDateTime entregueEm;
    @Column(name = "ultima_atualizacao", nullable = false)
    private LocalDateTime ultimaAtualizacao;
    private String acompanhamentoTokenHash;
    private LocalDateTime acompanhamentoTokenCriadoEm;
    private LocalDateTime acompanhamentoTokenExpiraEm;
    private LocalDateTime acompanhamentoTokenRevogadoEm;
}
