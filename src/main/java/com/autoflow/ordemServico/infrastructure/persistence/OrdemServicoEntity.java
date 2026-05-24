package com.autoflow.ordemServico.infrastructure.persistence;

import com.autoflow.ordemServico.domain.StatusOrdemServico;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ordem_servico")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrdemServicoEntity {

    @Id
    private UUID id;

    @Column(name = "numero_os", nullable = false, unique = true)
    private String numeroOs;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "veiculo_id", nullable = false)
    private UUID veiculoId;

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
    private List<ServicoSolicitadoEmbeddable> servicosSolicitados = new ArrayList<>();

}
