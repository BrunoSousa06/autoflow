package com.autoflow.domain.ordemservico;

import java.time.LocalDateTime;

public class HistoricoStatusOs {

    private Long id;
    private Long ordemServicoId;
    private String numeroOs;
    private StatusOrdemServico status;
    private LocalDateTime registradoEm;
    private String mensagemCliente;

    public static HistoricoStatusOs criar(Long ordemServicoId, StatusOrdemServico status,
                                           String mensagemCliente, String numeroOs,
                                           LocalDateTime registradoEm) {
        HistoricoStatusOs historico = new HistoricoStatusOs();
        historico.ordemServicoId = ordemServicoId;
        historico.status = status;
        historico.numeroOs = numeroOs;
        historico.mensagemCliente = mensagemCliente;
        if (registradoEm == null) throw new IllegalArgumentException("Data do historico e obrigatoria.");
        historico.registradoEm = registradoEm;
        return historico;
    }

    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public Long getOrdemServicoId() { return ordemServicoId; }
    public void setOrdemServicoId(Long value) { ordemServicoId = value; }
    public String getNumeroOs() { return numeroOs; }
    public void setNumeroOs(String value) { numeroOs = value; }
    public StatusOrdemServico getStatus() { return status; }
    public void setStatus(StatusOrdemServico value) { status = value; }
    public LocalDateTime getRegistradoEm() { return registradoEm; }
    public void setRegistradoEm(LocalDateTime value) { registradoEm = value; }
    public String getMensagemCliente() { return mensagemCliente; }
    public void setMensagemCliente(String value) { mensagemCliente = value; }
}
