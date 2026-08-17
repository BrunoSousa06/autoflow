package com.autoflow.domain.ordemservico.reparoadicional;

import com.autoflow.domain.ordemservico.ServicoSolicitado;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReparoAdicional {

    private Long id;
    private Long ordemServicoId;
    private String numeroOs;
    private Long mecanicoId;
    private Long orcamentoId;
    private StatusReparoAdicional status = StatusReparoAdicional.PENDENTE_APROVACAO;
    private LocalDateTime criadoEm;
    private LocalDateTime aprovadoEm;
    private LocalDateTime recusadoEm;
    private String motivoRecusa;
    private List<ServicoSolicitado> servicos = new ArrayList<>();

    public static ReparoAdicional criar(String numeroOs, Long mecanicoId, List<ServicoSolicitado> servicos) {
        ReparoAdicional reparo = new ReparoAdicional();
        reparo.numeroOs = numeroOs;
        reparo.mecanicoId = mecanicoId;
        reparo.criadoEm = LocalDateTime.now();
        reparo.status = StatusReparoAdicional.PENDENTE_APROVACAO;
        reparo.servicos.addAll(servicos);
        reparo.servicos.forEach(servico -> servico.setReparoAdicional(reparo));
        return reparo;
    }

    public void aprovar() {
        if (!StatusReparoAdicional.PENDENTE_APROVACAO.equals(status)) throw new IllegalStateException("Reparo adicional não está pendente de aprovação.");
        status = StatusReparoAdicional.APROVADO; aprovadoEm = LocalDateTime.now();
    }

    public void recusar(String motivo) {
        if (!StatusReparoAdicional.PENDENTE_APROVACAO.equals(status)) throw new IllegalStateException("Reparo adicional não está pendente de aprovação.");
        status = StatusReparoAdicional.RECUSADO; recusadoEm = LocalDateTime.now(); motivoRecusa = motivo;
    }

    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public Long getOrdemServicoId() { return ordemServicoId; }
    public void setOrdemServicoId(Long value) { ordemServicoId = value; }
    public String getNumeroOs() { return numeroOs; }
    public void setNumeroOs(String value) { numeroOs = value; }
    public Long getMecanicoId() { return mecanicoId; }
    public void setMecanicoId(Long value) { mecanicoId = value; }
    public Long getOrcamentoId() { return orcamentoId; }
    public void setOrcamentoId(Long value) { orcamentoId = value; }
    public StatusReparoAdicional getStatus() { return status; }
    public void setStatus(StatusReparoAdicional value) { status = value; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime value) { criadoEm = value; }
    public LocalDateTime getAprovadoEm() { return aprovadoEm; }
    public void setAprovadoEm(LocalDateTime value) { aprovadoEm = value; }
    public LocalDateTime getRecusadoEm() { return recusadoEm; }
    public void setRecusadoEm(LocalDateTime value) { recusadoEm = value; }
    public String getMotivoRecusa() { return motivoRecusa; }
    public void setMotivoRecusa(String value) { motivoRecusa = value; }
    public List<ServicoSolicitado> getServicos() { return servicos; }
    public void setServicos(List<ServicoSolicitado> value) { servicos = value; }
}
