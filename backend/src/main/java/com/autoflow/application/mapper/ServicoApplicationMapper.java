package com.autoflow.application.mapper;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.domain.servico.Servico;

public final class ServicoApplicationMapper {
    private ServicoApplicationMapper() {
    }

    public static Servico toDomain(ServicoInput input) {
        return Servico.criar(input.nome(), input.descricao(), input.valor());
    }

    public static Servico toDomain(Long id, ServicoInput input, boolean ativo) {
        return Servico.reconstituir(id, input.nome(), input.descricao(), input.valor(), ativo);
    }

    public static ServicoOutput toOutput(Servico servico) {
        return ServicoOutput.builder()
                .id(servico.id())
                .nome(servico.nome())
                .descricao(servico.descricao())
                .valor(servico.valor())
                .ativo(servico.ativo())
                .build();
    }
}
