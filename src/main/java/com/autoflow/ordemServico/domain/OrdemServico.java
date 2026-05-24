package com.autoflow.ordemServico.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class OrdemServico {

    private final UUID id;
    private final String numeroOs;
    private final UUID clienteId;
    private final UUID veiculoId;
    private final StatusOrdemServico status;
    private final LocalDateTime dataAbertura;
    private final List<ServicoSolicitado> servicosSolicitados;

    private OrdemServico(
            UUID id,
            String numeroOs,
            UUID clienteId,
            UUID veiculoId,
            StatusOrdemServico status,
            LocalDateTime dataAbertura,
            List<ServicoSolicitado> servicosSolicitados
    ) {
        this.id = id;
        this.numeroOs = numeroOs;
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.status = status;
        this.dataAbertura = dataAbertura;
        this.servicosSolicitados = List.copyOf(servicosSolicitados);
    }

    public static OrdemServico criar(
            UUID clienteId,
            UUID veiculoId,
            List<ServicoSolicitado> servicosSolicitados
    ) {
        validarCliente(clienteId);
        validarVeiculo(veiculoId);
        validarServicos(servicosSolicitados);

        return new OrdemServico(
                UUID.randomUUID(),
                gerarNumeroOs(),
                clienteId,
                veiculoId,
                StatusOrdemServico.RECEBIDA,
                LocalDateTime.now(),
                servicosSolicitados
        );
    }

    public static OrdemServico restaurar(
            UUID id,
            String numeroOs,
            UUID clienteId,
            UUID veiculoId,
            StatusOrdemServico status,
            LocalDateTime dataAbertura,
            List<ServicoSolicitado> servicosSolicitados
    ) {
        if (id == null) {
            throw new IllegalArgumentException("Id e obrigatorio.");
        }

        if (numeroOs == null || numeroOs.isBlank()) {
            throw new IllegalArgumentException("Numero da OS e obrigatorio.");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status e obrigatorio.");
        }

        if (dataAbertura == null) {
            throw new IllegalArgumentException("Data de abertura e obrigatoria.");
        }

        validarCliente(clienteId);
        validarVeiculo(veiculoId);
        validarServicos(servicosSolicitados);

        return new OrdemServico(
                id,
                numeroOs,
                clienteId,
                veiculoId,
                status,
                dataAbertura,
                servicosSolicitados
        );
    }

    private static void validarCliente(UUID clienteId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("Cliente e obrigatorio.");
        }
    }

    private static void validarVeiculo(UUID veiculoId) {
        if (veiculoId == null) {
            throw new IllegalArgumentException("Veiculo e obrigatorio.");
        }
    }

    private static void validarServicos(List<ServicoSolicitado> servicosSolicitados) {
        if (servicosSolicitados == null || servicosSolicitados.isEmpty()) {
            throw new IllegalArgumentException("A ordem de servico deve ter ao menos um servico solicitado.");
        }
    }

    private static String gerarNumeroOs() {
        return "OS-" + System.currentTimeMillis();
    }

}
