package com.autoflow.domain.ordemservico;

import com.autoflow.domain.cliente.Cliente;
import com.autoflow.domain.veiculo.Veiculo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OrdemServico {

    private Long id;
    private String numeroOs;
    private ClienteOs cliente;
    private Veiculo veiculo;
    private StatusOrdemServico status;
    private LocalDateTime dataAbertura;
    private Diagnostico diagnostico;
    private List<ServicoSolicitado> servicosSolicitados = new ArrayList<>();
    private LocalDateTime execucaoIniciadaEm;
    private LocalDateTime finalizadaEm;
    private LocalDateTime entregueEm;
    private LocalDateTime ultimaAtualizacao;
    private String acompanhamentoTokenHash;
    private LocalDateTime acompanhamentoTokenCriadoEm;
    private LocalDateTime acompanhamentoTokenExpiraEm;
    private LocalDateTime acompanhamentoTokenRevogadoEm;

    public OrdemServico() {
    }

    private OrdemServico(String numeroOs, Veiculo veiculo, StatusOrdemServico status, LocalDateTime dataAbertura) {
        this.numeroOs = numeroOs;
        this.veiculo = veiculo;
        this.status = status;
        this.dataAbertura = dataAbertura;
    }

    public static OrdemServico criar(Cliente cliente, Veiculo veiculo,
                                     String numeroOs, LocalDateTime dataHora) {
        validarVeiculo(veiculo);
        if (cliente == null) throw new IllegalArgumentException("Veiculo deve ter cliente para criar OS.");
        validarDadosDeCriacao(numeroOs, dataHora);
        OrdemServico ordemServico = novaOrdem(veiculo, numeroOs, dataHora);
        ordemServico.cliente = ClienteOs.fromCliente(cliente);
        ordemServico.ultimaAtualizacao = dataHora;
        return ordemServico;
    }

    public static OrdemServico criar(Long clienteId, String clienteNome, String clienteCpfCnpj,
                                     String clienteEmail, String clienteTelefone, Veiculo veiculo,
                                     String numeroOs, LocalDateTime dataHora) {
        validarVeiculo(veiculo);
        validarDadosDeCriacao(numeroOs, dataHora);
        OrdemServico ordemServico = novaOrdem(veiculo, numeroOs, dataHora);
        ordemServico.cliente = ClienteOs.fromFields(clienteId, clienteNome, clienteCpfCnpj, clienteEmail, clienteTelefone);
        ordemServico.ultimaAtualizacao = dataHora;
        return ordemServico;
    }

    public static OrdemServico reconstituir(Long id, String numeroOs, ClienteOs cliente, Veiculo veiculo,
                                             StatusOrdemServico status, LocalDateTime dataAbertura,
                                             Diagnostico diagnostico, List<ServicoSolicitado> servicosSolicitados,
                                             LocalDateTime execucaoIniciadaEm, LocalDateTime finalizadaEm,
                                             LocalDateTime entregueEm, LocalDateTime ultimaAtualizacao,
                                             String acompanhamentoTokenHash, LocalDateTime acompanhamentoTokenCriadoEm,
                                             LocalDateTime acompanhamentoTokenExpiraEm, LocalDateTime acompanhamentoTokenRevogadoEm) {
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.id = id;
        ordemServico.numeroOs = numeroOs;
        ordemServico.cliente = cliente;
        ordemServico.veiculo = veiculo;
        ordemServico.status = status;
        ordemServico.dataAbertura = dataAbertura;
        ordemServico.diagnostico = diagnostico;
        ordemServico.servicosSolicitados = new ArrayList<>(servicosSolicitados);
        ordemServico.execucaoIniciadaEm = execucaoIniciadaEm;
        ordemServico.finalizadaEm = finalizadaEm;
        ordemServico.entregueEm = entregueEm;
        ordemServico.ultimaAtualizacao = ultimaAtualizacao;
        ordemServico.acompanhamentoTokenHash = acompanhamentoTokenHash;
        ordemServico.acompanhamentoTokenCriadoEm = acompanhamentoTokenCriadoEm;
        ordemServico.acompanhamentoTokenExpiraEm = acompanhamentoTokenExpiraEm;
        ordemServico.acompanhamentoTokenRevogadoEm = acompanhamentoTokenRevogadoEm;
        return ordemServico;
    }

    private static OrdemServico novaOrdem(Veiculo veiculo, String numeroOs, LocalDateTime dataHora) {
        return new OrdemServico(numeroOs, veiculo, StatusOrdemServico.RECEBIDA, dataHora);
    }

    public void atualizarUltimaAtualizacao(LocalDateTime dataHora) {
        ultimaAtualizacao = validarDataHora(dataHora);
    }

    public void registrarLaudo(String laudo, LocalDateTime dataHora) {
        if (status != StatusOrdemServico.EM_DIAGNOSTICO) throw new IllegalArgumentException("O status deve ser EM_DIAGNOSTICO.");
        diagnostico.setLaudo(laudo);
        atualizarUltimaAtualizacao(dataHora);
    }

    public void iniciarDiagnostico(LocalDateTime dataHora) {
        validarTransicao(StatusOrdemServico.RECEBIDA, StatusOrdemServico.EM_DIAGNOSTICO);
        if (diagnostico == null) diagnostico = new Diagnostico();
        diagnostico.setIniciadoEm(validarDataHora(dataHora));
        status = StatusOrdemServico.EM_DIAGNOSTICO;
        atualizarUltimaAtualizacao(dataHora);
    }

    public void finalizarDiagnostico(LocalDateTime dataHora) {
        validaSePodeFinalizarDiagnostico();
        diagnostico.setConcluidoEm(validarDataHora(dataHora));
        atualizarUltimaAtualizacao(dataHora);
    }

    private void validaSePodeFinalizarDiagnostico() {
        if (status != StatusOrdemServico.EM_DIAGNOSTICO) throw new IllegalArgumentException("O status deve ser EM_DIAGNOSTICO.");
        if (diagnostico == null) throw new IllegalArgumentException("OS deve ter um diagnostico para finalizar diagnostico.");
        if (diagnostico.getLaudo() == null) throw new IllegalArgumentException("Diagnostico deve possuir um laudo para finalizar diagnostico.");
    }

    public void adicionarServicosSolicitados(List<ServicoSolicitado> servicos) {
        if (servicos == null || servicos.isEmpty()) return;
        if (status == StatusOrdemServico.FINALIZADA || status == StatusOrdemServico.ENTREGUE) {
            throw new IllegalStateException("Não é permitido adicionar serviços a uma ordem de serviço já finalizada ou entregue.");
        }
        Set<Long> idsJaAdicionados = this.servicosSolicitados.stream().map(ServicoSolicitado::getServicoId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> idsNovos = new HashSet<>();
        for (ServicoSolicitado servico : servicos) {
            Long servicoId = servico.getServicoId();
            if (servicoId == null) throw new IllegalArgumentException("Serviço informado sem ID.");
            if (!idsNovos.add(servicoId)) throw new IllegalArgumentException("Serviço repetido na requisição: ID " + servicoId);
            if (idsJaAdicionados.contains(servicoId)) throw new IllegalArgumentException("Serviço já incluído na ordem de serviço: ID " + servicoId);
        }
        this.servicosSolicitados.addAll(servicos);
    }

    public ServicoSolicitado buscarServicoSolicitado(Long servicoId) {
        return servicosSolicitados.stream().filter(servico -> Objects.equals(servico.getServicoId(), servicoId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado na OS."));
    }

    public void aguardarAprovacao(LocalDateTime dataHora) {
        validarTransicao(StatusOrdemServico.EM_DIAGNOSTICO, StatusOrdemServico.AGUARDANDO_APROVACAO);
        status = StatusOrdemServico.AGUARDANDO_APROVACAO; atualizarUltimaAtualizacao(dataHora);
    }

    public void finalizarPorOrcamentoRecusado(LocalDateTime dataHora) {
        validarTransicao(StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.FINALIZADA);
        status = StatusOrdemServico.FINALIZADA; finalizadaEm = validarDataHora(dataHora); atualizarUltimaAtualizacao(dataHora);
    }

    public void iniciarExecucao(LocalDateTime dataHora) {
        validarTransicao(StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.EM_EXECUCAO);
        if (execucaoIniciadaEm == null) execucaoIniciadaEm = validarDataHora(dataHora);
        atualizarUltimaAtualizacao(dataHora); status = StatusOrdemServico.EM_EXECUCAO;
    }

    public void finalizarSeTodosServicosFinalizados(LocalDateTime dataHora) {
        if (status == StatusOrdemServico.FINALIZADA || status == StatusOrdemServico.ENTREGUE) return;
        if (servicosSolicitados.isEmpty()) return;
        boolean todosFinalizados = servicosSolicitados.stream().allMatch(servico -> servico.getStatus() == StatusServicoOs.FINALIZADO
                || servico.getStatus() == StatusServicoOs.CANCELADO);
        if (todosFinalizados) { status = StatusOrdemServico.FINALIZADA; finalizadaEm = validarDataHora(dataHora); atualizarUltimaAtualizacao(dataHora); }
    }

    public void entregar(LocalDateTime dataHora) {
        validarTransicao(StatusOrdemServico.FINALIZADA, StatusOrdemServico.ENTREGUE);
        status = StatusOrdemServico.ENTREGUE; entregueEm = validarDataHora(dataHora); atualizarUltimaAtualizacao(dataHora);
    }

    private void validarTransicao(StatusOrdemServico esperado, StatusOrdemServico destino) {
        if (status != esperado) throw new IllegalStateException("Transicao invalida: OS esta " + status
                + " e nao pode ir para " + destino + ". Status esperado: " + esperado);
    }

    private static void validarVeiculo(Veiculo veiculo) {
        if (veiculo == null) throw new IllegalArgumentException("Veiculo e obrigatorio.");
    }

    private static void validarDadosDeCriacao(String numeroOs, LocalDateTime dataHora) {
        if (numeroOs == null || numeroOs.isBlank()) throw new IllegalArgumentException("Numero da OS e obrigatorio.");
        validarDataHora(dataHora);
    }

    private static LocalDateTime validarDataHora(LocalDateTime dataHora) {
        if (dataHora == null) throw new IllegalArgumentException("Data e hora sao obrigatorias.");
        return dataHora;
    }

    public List<ServicoSolicitado> getServicosSolicitados() { return Collections.unmodifiableList(servicosSolicitados); }
    public Long getClienteId() { return cliente.getId(); }
    public Long getVeiculoId() { return veiculo == null ? null : veiculo.id(); }
    public String getVeiculoPlaca() { return veiculo == null ? null : veiculo.placa(); }

    public void configurarAcompanhamentoPublico(String tokenHash, LocalDateTime criadoEm, LocalDateTime expiraEm) {
        if (tokenHash == null || tokenHash.isBlank()) throw new IllegalArgumentException("Hash do token de acompanhamento é obrigatório");
        if (criadoEm == null) throw new IllegalArgumentException("Data de criação do token é obrigatória");
        if (expiraEm != null && !expiraEm.isAfter(criadoEm)) throw new IllegalArgumentException("Expiração deve ser posterior à criação do token");
        acompanhamentoTokenHash = tokenHash; acompanhamentoTokenCriadoEm = criadoEm; acompanhamentoTokenExpiraEm = expiraEm; acompanhamentoTokenRevogadoEm = null;
    }

    public boolean acompanhamentoPublicoDisponivel(LocalDateTime agora) {
        if (acompanhamentoTokenHash == null || acompanhamentoTokenRevogadoEm != null) return false;
        return acompanhamentoTokenExpiraEm == null || acompanhamentoTokenExpiraEm.isAfter(agora);
    }

    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public String getNumeroOs() { return numeroOs; }
    public void setNumeroOs(String value) { numeroOs = value; }
    public ClienteOs getCliente() { return cliente; }
    public void setCliente(ClienteOs value) { cliente = value; }
    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo value) { veiculo = value; }
    public StatusOrdemServico getStatus() { return status; }
    public void setStatus(StatusOrdemServico value) { status = value; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime value) { dataAbertura = value; }
    public Diagnostico getDiagnostico() { return diagnostico; }
    public void setDiagnostico(Diagnostico value) { diagnostico = value; }
    public void setServicosSolicitados(List<ServicoSolicitado> value) { servicosSolicitados = value; }
    public LocalDateTime getExecucaoIniciadaEm() { return execucaoIniciadaEm; }
    public void setExecucaoIniciadaEm(LocalDateTime value) { execucaoIniciadaEm = value; }
    public LocalDateTime getFinalizadaEm() { return finalizadaEm; }
    public void setFinalizadaEm(LocalDateTime value) { finalizadaEm = value; }
    public LocalDateTime getEntregueEm() { return entregueEm; }
    public void setEntregueEm(LocalDateTime value) { entregueEm = value; }
    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
    public void setUltimaAtualizacao(LocalDateTime value) { ultimaAtualizacao = value; }
    public String getAcompanhamentoTokenHash() { return acompanhamentoTokenHash; }
    public void setAcompanhamentoTokenHash(String value) { acompanhamentoTokenHash = value; }
    public LocalDateTime getAcompanhamentoTokenCriadoEm() { return acompanhamentoTokenCriadoEm; }
    public void setAcompanhamentoTokenCriadoEm(LocalDateTime value) { acompanhamentoTokenCriadoEm = value; }
    public LocalDateTime getAcompanhamentoTokenExpiraEm() { return acompanhamentoTokenExpiraEm; }
    public void setAcompanhamentoTokenExpiraEm(LocalDateTime value) { acompanhamentoTokenExpiraEm = value; }
    public LocalDateTime getAcompanhamentoTokenRevogadoEm() { return acompanhamentoTokenRevogadoEm; }
    public void setAcompanhamentoTokenRevogadoEm(LocalDateTime value) { acompanhamentoTokenRevogadoEm = value; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof OrdemServico that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
