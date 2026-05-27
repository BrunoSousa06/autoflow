package com.autoflow.domain.ordemServico;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Entity
@Table(name = "ordem_servico")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class OrdemServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_os", nullable = false, unique = true)
    private String numeroOs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @ToString.Exclude
    private ClienteEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    @ToString.Exclude
    private VeiculoEntity veiculo;

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

    @Embedded
    private DiagnosticoEntity diagnostico;

    private OrdemServicoEntity(
            String numeroOs,
            ClienteEntity cliente,
            VeiculoEntity veiculo,
            StatusOrdemServico status,
            LocalDateTime dataAbertura,
            List<ServicoSolicitadoEntity> servicosSolicitados
    ) {
        this.numeroOs = numeroOs;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.status = status;
        this.dataAbertura = dataAbertura;
        this.servicosSolicitados = new ArrayList<>(servicosSolicitados);
    }

    public static OrdemServicoEntity criar(
            ClienteEntity cliente,
            VeiculoEntity veiculo,
            List<ServicoSolicitadoEntity> servicosSolicitados
    ) {
        validarCliente(cliente);
        validarVeiculo(veiculo);
        validarServicos(servicosSolicitados);

        return new OrdemServicoEntity(
                gerarNumeroOs(),
                cliente,
                veiculo,
                StatusOrdemServico.RECEBIDA,
                LocalDateTime.now(),
                servicosSolicitados
        );
    }

    public void adicionarServicos(List<ServicoSolicitadoEntity> servicosSolicitados) {
        validarServicos(servicosSolicitados);
        this.servicosSolicitados.addAll(servicosSolicitados);
    }


    private static void validarCliente(ClienteEntity cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente e obrigatorio.");
        }
    }

    private static void validarVeiculo(VeiculoEntity veiculo) {
        if (veiculo == null) {
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

    public Long getClienteId() {
        return cliente.getId();
    }

    public Long getVeiculoId() {
        return veiculo.getId();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        OrdemServicoEntity that = (OrdemServicoEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }


}
