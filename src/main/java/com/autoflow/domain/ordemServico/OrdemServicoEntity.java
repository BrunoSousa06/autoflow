package com.autoflow.domain.ordemServico;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.lang.Long;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Entity
@Table(name = "ordem_servico")
@Getter
@NoArgsConstructor
public class OrdemServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_os", nullable = false, unique = true)
    private String numeroOs;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "veiculo_id", nullable = false)
    private Long veiculoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrdemServico status;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @ElementCollection
    @CollectionTable(
            name = "ordem_servico_servico_solicitado",
            joinColumns = @JoinColumn(name = "ordem_servico_id")
    )
    @OrderColumn(name = "ordem")
    private List<ServicoSolicitadoEntity> servicosSolicitados = new ArrayList<>();

    private OrdemServicoEntity(
            String numeroOs,
            Long clienteId,
            Long veiculoId,
            StatusOrdemServico status,
            LocalDateTime dataAbertura,
            List<ServicoSolicitadoEntity> servicosSolicitados
    ) {
        this.numeroOs = numeroOs;
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.status = status;
        this.dataAbertura = dataAbertura;
        this.servicosSolicitados = new ArrayList<>(servicosSolicitados);
    }

    public static OrdemServicoEntity criar(
            Long clienteId,
            Long veiculoId,
            List<ServicoSolicitadoEntity> servicosSolicitados
    ) {
        validarCliente(clienteId);
        validarVeiculo(veiculoId);
        validarServicos(servicosSolicitados);

        return new OrdemServicoEntity(
                gerarNumeroOs(),
                clienteId,
                veiculoId,
                StatusOrdemServico.RECEBIDA,
                LocalDateTime.now(),
                servicosSolicitados
        );
    }

    private static void validarCliente(Long clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("Cliente e obrigatorio.");
        }
    }

    private static void validarVeiculo(Long veiculoId) {
        if (veiculoId == null) {
            throw new IllegalArgumentException("Veiculo e obrigatorio.");
        }
    }

    private static void validarServicos(List<ServicoSolicitadoEntity> servicosSolicitados) {
        if (servicosSolicitados == null || servicosSolicitados.isEmpty()) {
            throw new IllegalArgumentException("A ordem de servico deve ter ao menos um servico solicitado.");
        }
    }

    private static String gerarNumeroOs() {
        return "OS-" + System.currentTimeMillis();
    }

    public List<ServicoSolicitadoEntity> getServicosSolicitados() {
        return Collections.unmodifiableList(servicosSolicitados);
    }

}
