package com.autoflow.domain.ordemservico;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


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
            ClienteEntity cliente,
            VeiculoEntity veiculo
    ) {
        validarVeiculo(veiculo);

        if(cliente == null) throw new IllegalArgumentException("Veiculo deve ter cliente para criar OS.");

        OrdemServicoEntity ordemServico = new OrdemServicoEntity(
                gerarNumeroOs(),
                veiculo,
                StatusOrdemServico.RECEBIDA,
                LocalDateTime.now()
        );
        ordemServico.cliente = ClienteOsEntity.fromCliente(cliente);
        ordemServico.atualizarUltimaAtualizacao();
        return ordemServico;
    }

    public void atualizarUltimaAtualizacao() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public void registrarLaudo(
            String laudo
    ){
        if(this.status != StatusOrdemServico.EM_DIAGNOSTICO){
            throw new IllegalArgumentException("O status deve ser EM_DIAGNOSTICO.");
        }
        this.diagnostico.setLaudo(laudo);
        this.atualizarUltimaAtualizacao();
    }
    public void iniciarDiagnostico() {
        validarTransicao(StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO);
        if (this.diagnostico == null) {
            this.diagnostico = new DiagnosticoEntity();
        }

        this.diagnostico.setIniciadoEm(LocalDateTime.now());
        this.status = StatusOrdemServico.EM_DIAGNOSTICO;
        this.atualizarUltimaAtualizacao();
    }

    public void finalizarDiagnostico(){
        validaSePodeFinalizarDiagnostico();
        this.diagnostico.setConcluidoEm(LocalDateTime.now());
        this.atualizarUltimaAtualizacao();
    }

    private void validaSePodeFinalizarDiagnostico() {
        if(this.status != StatusOrdemServico.EM_DIAGNOSTICO){
            throw new IllegalArgumentException("O status deve ser EM_DIAGNOSTICO.");
        }
        if(this.diagnostico == null){
            throw new IllegalArgumentException("OS deve ter um diagnostico para finalizar diagnostico.");
        }
        if(this.diagnostico.getLaudo() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Diagnostico deve possuir um laudo para finalizar diagnostico.");
        }
    }

    public void adicionarServicosSolicitados(List<ServicoSolicitadoEntity> servicosSolicitados) {
        if (servicosSolicitados == null || servicosSolicitados.isEmpty()) {
            return;
        }

        if (this.status == StatusOrdemServico.FINALIZADA || this.status == StatusOrdemServico.ENTREGUE) {
            throw new IllegalStateException("Não é permitido adicionar serviços a uma ordem de serviço já finalizada ou entregue.");
        }

        Set<Long> idsJaAdicionados = this.servicosSolicitados.stream()
                .map(ServicoSolicitadoEntity::getServicoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> idsNovos = new HashSet<>();

        for (ServicoSolicitadoEntity servico : servicosSolicitados) {
            Long servicoId = servico.getServicoId();

            if (servicoId == null) {
                throw new IllegalArgumentException("Serviço informado sem ID.");
            }

            if (!idsNovos.add(servicoId)) {
                throw new IllegalArgumentException("Serviço repetido na requisição: ID " + servicoId);
            }

            if (idsJaAdicionados.contains(servicoId)) {
                throw new IllegalArgumentException("Serviço já incluído na ordem de serviço: ID " + servicoId);
            }
        }

        servicosSolicitados.forEach(servico -> {
            servico.setOrdemServico(this);
            this.servicosSolicitados.add(servico);
        });
    }

    public ServicoSolicitadoEntity buscarServicoSolicitado(Long servicoOsId) {
        return servicosSolicitados.stream()
                .filter(servico -> Objects.equals(servico.getServicoId(), servicoOsId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado na OS."));
    }

    public void aguardarAprovacao(){
        validarTransicao(StatusOrdemServico.EM_DIAGNOSTICO, StatusOrdemServico.AGUARDANDO_APROVACAO);
        this.status = StatusOrdemServico.AGUARDANDO_APROVACAO;
        this.atualizarUltimaAtualizacao();
    }

    private static void validarVeiculo(VeiculoEntity veiculo) {
        if (veiculo == null) {
            throw new IllegalArgumentException("Veiculo e obrigatorio.");
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

    public void finalizarPorOrcamentoRecusado() {
        validarTransicao(StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.FINALIZADA);
        this.status = StatusOrdemServico.FINALIZADA;
        this.finalizadaEm = LocalDateTime.now();
        atualizarUltimaAtualizacao();
    }

    public void iniciarExecucao() {
        validarTransicao(StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.EM_EXECUCAO);
        if (this.execucaoIniciadaEm == null) {
            this.execucaoIniciadaEm = LocalDateTime.now();
        }
        this.atualizarUltimaAtualizacao();
        this.status = StatusOrdemServico.EM_EXECUCAO;
    }

    public void finalizarSeTodosServicosFinalizados() {
        boolean todosFinalizados = servicosSolicitados.stream()
                .allMatch(servico -> servico.getStatus() == StatusServicoOs.FINALIZADO);

        if (todosFinalizados) {
            this.status = StatusOrdemServico.FINALIZADA;
            this.finalizadaEm = LocalDateTime.now();
            this.atualizarUltimaAtualizacao();
        }
    }

    public void entregar() {
        validarTransicao(StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE);
        this.status = StatusOrdemServico.ENTREGUE;
        this.entregueEm = LocalDateTime.now();
        this.atualizarUltimaAtualizacao();
    }

    private void validarTransicao(StatusOrdemServico esperado, StatusOrdemServico destino) {
        if (this.status != esperado) {
            throw new IllegalStateException(
                    "Transicao invalida: OS esta " + this.status +
                            " e nao pode ir para " + destino +
                            ". Status esperado: " + esperado
            );
        }
    }
}
