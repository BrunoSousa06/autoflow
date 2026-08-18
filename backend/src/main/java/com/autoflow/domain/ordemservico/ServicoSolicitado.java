package com.autoflow.domain.ordemservico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServicoSolicitado {

    private Long id;
    private Long servicoId;
    private String nome;
    private BigDecimal valor;
    private StatusServicoOs status = StatusServicoOs.AGUARDANDO;
    private LocalDateTime iniciadoEm;
    private LocalDateTime finalizadoEm;
    private List<ItemNecessario> itensNecessarios = new ArrayList<>();

    public ServicoSolicitado() {
    }

    public ServicoSolicitado(Long servicoId) {
        validarServicoId(servicoId);
        this.servicoId = servicoId;
    }

    public ServicoSolicitado(Long servicoId, String nome) {
        validarServicoId(servicoId);
        validarNome(nome);
        this.servicoId = servicoId;
        this.nome = nome;
    }

    public ServicoSolicitado(Long servicoId, String nome, BigDecimal valor) {
        validarServicoId(servicoId);
        validarNome(nome);
        validarValor(valor);
        this.servicoId = servicoId;
        this.nome = nome;
        this.valor = valor;
    }

    public static ServicoSolicitado criar(Long servicoId, String nome, BigDecimal valor) {
        return new ServicoSolicitado(servicoId, nome, valor);
    }

    public void registrarItensNecessarios(List<ItemNecessario> itens) {
        if (status != StatusServicoOs.AGUARDANDO) {
            throw new IllegalStateException("Não é permitido modificar itens de um serviço que já foi iniciado ou finalizado. "
                    + "Para novos itens após o início, utilize o fluxo de reparo adicional para gerar um novo orçamento.");
        }
        itensNecessarios.clear();
        itensNecessarios.addAll(itens);
    }

    public void iniciar(List<ItemNecessario> itensAtualizados) {
        validarStatusParaInicio();
        atualizarExecucao(itensAtualizados);
    }

    public void iniciar(List<ItemNecessario> itensAtualizados, StatusOrdemServico statusOrdemServico) {
        validarPodeIniciar(statusOrdemServico);
        atualizarExecucao(itensAtualizados);
    }

    private void atualizarExecucao(List<ItemNecessario> itensAtualizados) {
        itensNecessarios.clear();
        itensNecessarios.addAll(itensAtualizados);
        status = StatusServicoOs.EM_EXECUCAO;
        iniciadoEm = LocalDateTime.now();
    }

    public void validarPodeIniciar(StatusOrdemServico statusOrdemServico) {
        validarStatusParaInicio();
        if (statusOrdemServico != StatusOrdemServico.EM_EXECUCAO) {
            throw new IllegalStateException("Um serviço só pode ser iniciado se a Ordem de Serviço estiver em execução (após a aprovação do orçamento).");
        }
    }

    private void validarStatusParaInicio() {
        if (status != StatusServicoOs.AGUARDANDO) {
            throw new IllegalStateException("O serviço só pode ser iniciado se estiver no status AGUARDANDO. Status atual: " + status);
        }
    }

    public void finalizar() {
        if (status != StatusServicoOs.EM_EXECUCAO) {
            throw new IllegalStateException("Servico deve estar em execucao para finalizar.");
        }
        status = StatusServicoOs.FINALIZADO;
        finalizadoEm = LocalDateTime.now();
    }

    private static void validarServicoId(Long servicoId) {
        if (servicoId == null) throw new IllegalArgumentException("Servico e obrigatorio.");
    }

    private static void validarNome(String nome) {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("Nome do servico e obrigatorio.");
    }

    private static void validarValor(BigDecimal valor) {
        if (valor == null) throw new IllegalArgumentException("Valor do servico e obrigatorio.");
    }

    public Long getId() { return id; }
    public void setId(Long value) { this.id = value; }
    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long value) { this.servicoId = value; }
    public String getNome() { return nome; }
    public void setNome(String value) { this.nome = value; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal value) { this.valor = value; }
    public StatusServicoOs getStatus() { return status; }
    public void setStatus(StatusServicoOs value) { this.status = value; }
    public LocalDateTime getIniciadoEm() { return iniciadoEm; }
    public void setIniciadoEm(LocalDateTime value) { this.iniciadoEm = value; }
    public LocalDateTime getFinalizadoEm() { return finalizadoEm; }
    public void setFinalizadoEm(LocalDateTime value) { this.finalizadoEm = value; }
    public List<ItemNecessario> getItensNecessarios() { return itensNecessarios; }
    public void setItensNecessarios(List<ItemNecessario> value) {
        this.itensNecessarios = value == null ? new ArrayList<>() : new ArrayList<>(value);
    }
}
