package com.autoflow.application.exception;

public class OrdemServicoNaoEncontradaException
        extends RuntimeException {

    public OrdemServicoNaoEncontradaException(Long ordemServicoId) {
        super(
                "Ordem de serviço não encontrada para o ID: "
                        + ordemServicoId
        );
    }
}