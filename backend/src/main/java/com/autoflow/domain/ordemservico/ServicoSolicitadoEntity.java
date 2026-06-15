package com.autoflow.domain.ordemservico;

import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
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
    @CollectionTable(
            name = "ordem_servico_servico_item_necessario",
            joinColumns = @JoinColumn(name = "servico_solicitado_id")
    )
    @OrderColumn(name = "ordem")
    @BatchSize(size = 50)
    private List<ItemNecessarioEntity> itensNecessarios = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "reparo_adicional_id")
    private ReparoAdicionalEntity reparoAdicional;

    public ServicoSolicitadoEntity(Long servicoId) {
        validarServicoId(servicoId);
        this.servicoId = servicoId;
    }

    public ServicoSolicitadoEntity(Long servicoId, String nome) {
        validarServicoId(servicoId);
        validarNome(nome);
        this.servicoId = servicoId;
        this.nome = nome;
    }

    public ServicoSolicitadoEntity(Long servicoId, String nome, BigDecimal valor) {
        validarServicoId(servicoId);
        validarNome(nome);
        validarValor(valor);
        this.servicoId = servicoId;
        this.nome = nome;
        this.valor = valor;
    }

    public static ServicoSolicitadoEntity criar(Long servicoId, String nome, BigDecimal valor) {
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(servicoId, nome, valor);
        servico.setStatus(StatusServicoOs.AGUARDANDO);
        return servico;
    }

    public void registrarItensNecessarios(List<ItemNecessarioEntity> itens) {
        this.itensNecessarios.clear();
        this.itensNecessarios.addAll(itens);
    }

    public void iniciar(List<ItemNecessarioEntity> itensAtualizados) {
        if (this.status == StatusServicoOs.FINALIZADO) {
            throw new IllegalStateException("Servico ja finalizado.");
        }

        this.itensNecessarios.clear();
        this.itensNecessarios.addAll(itensAtualizados);
        this.status = StatusServicoOs.EM_EXECUCAO;
        this.iniciadoEm = LocalDateTime.now();
    }

    public void finalizar() {
        if (this.status != StatusServicoOs.EM_EXECUCAO) {
            throw new IllegalStateException("Servico deve estar em execucao para finalizar.");
        }

        this.status = StatusServicoOs.FINALIZADO;
        this.finalizadoEm = LocalDateTime.now();
    }

    private static void validarServicoId(Long servicoId) {
        if (servicoId == null) {
            throw new IllegalArgumentException("Servico e obrigatorio.");
        }
    }

    private static void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do servico e obrigatorio.");
        }
    }

    private static void validarValor(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("Valor do servico e obrigatorio.");
        }
    }
}
