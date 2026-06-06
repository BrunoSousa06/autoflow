package com.autoflow.domain.ordemservico;

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

    @Embedded
    private ClienteOsEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    @ToString.Exclude
    private VeiculoEntity veiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrdemServico status;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Embedded
    private DiagnosticoEntity diagnostico;

    @OneToMany(
            mappedBy = "ordemServico",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderColumn(name = "ordem")
    private List<ServicoSolicitadoEntity> servicosSolicitados = new ArrayList<>();

    @Column(name = "execucao_iniciada_em")
    private LocalDateTime execucaoIniciadaEm;

    @Column(name = "finalizada_em")
    private LocalDateTime finalizadaEm;

    private OrdemServicoEntity(
            String numeroOs,
            VeiculoEntity veiculo,
            StatusOrdemServico status,
            LocalDateTime dataAbertura
    ) {
        this.numeroOs = numeroOs;
        this.veiculo = veiculo;
        this.status = status;
        this.dataAbertura = dataAbertura;
    }

    public static OrdemServicoEntity criar(
            VeiculoEntity veiculo
    ) {
        validarVeiculo(veiculo);

        if(veiculo.getCliente() == null) throw new IllegalArgumentException("Veiculo deve ter cliente para criar OS.");

        OrdemServicoEntity ordemServico = new OrdemServicoEntity(
                gerarNumeroOs(),
                veiculo,
                StatusOrdemServico.RECEBIDA,
                LocalDateTime.now()
        );
        ordemServico.cliente = ClienteOsEntity.fromCliente(veiculo.getCliente());
        return ordemServico;
    }

    public static OrdemServicoEntity criar(
            VeiculoEntity veiculo,
            List<ServicoSolicitadoEntity> servicosSolicitados
    ) {
        OrdemServicoEntity ordemServico = criar(veiculo);
        ordemServico.adicionarServicos(servicosSolicitados);
        return ordemServico;
    }

    public void registrarLaudo(
            String laudo
    ){
        if(this.status != StatusOrdemServico.EM_DIAGNOSTICO){
            throw new IllegalArgumentException("O status deve ser EM_DIAGNOSTICO.");
        }
        this.diagnostico.setLaudo(laudo);
    }

    public void finalizarDiagnostico(){
        validaSePodeFinalizarDiagnostico();
        this.diagnostico.setConcluidoEm(LocalDateTime.now());
    }

    private void validaSePodeFinalizarDiagnostico() {
        if(this.status != StatusOrdemServico.EM_DIAGNOSTICO){
            throw new IllegalArgumentException("O status deve ser EM_DIAGNOSTICO.");
        }
        if(this.diagnostico == null){
            throw new IllegalArgumentException("OS deve ter um diagnostico para finalizar diagnostico.");
        }
        if(this.diagnostico.getLaudo() == null){
            throw new IllegalArgumentException("Diagnostico deve possuir um laudo para finalizar diagnostico.");
        }
    }

    public void adicionarServicos(List<ServicoSolicitadoEntity> servicosSolicitados) {
        validarServicos(servicosSolicitados);

        servicosSolicitados.forEach(servico -> {
            servico.setOrdemServico(this);
            this.servicosSolicitados.add(servico);
        });
    }

    public ServicoSolicitadoEntity buscarServicoSolicitado(Long servicoOsId) {
        return servicosSolicitados.stream()
                .filter(servico -> Objects.equals(servico.getId(), servicoOsId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado na OS."));
    }

    public void aguardarAprovacao(){
        this.status = StatusOrdemServico.AGUARDANDO_APROVACAO;
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
        Class<?> oEffectiveClass = o instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        OrdemServicoEntity that = (OrdemServicoEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hibernateProxy ? hibernateProxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public void iniciarExecucaoSeNecessario() {
        if (this.execucaoIniciadaEm == null) {
            this.execucaoIniciadaEm = LocalDateTime.now();
        }

        this.status = StatusOrdemServico.EM_EXECUCAO;
    }

    public void finalizarSeTodosServicosFinalizados() {
        boolean todosFinalizados = servicosSolicitados.stream()
                .allMatch(servico -> servico.getStatus() == StatusServicoOs.FINALIZADO);

        if (todosFinalizados) {
            this.status = StatusOrdemServico.FINALIZADA;
            this.finalizadaEm = LocalDateTime.now();
        }
    }

}
