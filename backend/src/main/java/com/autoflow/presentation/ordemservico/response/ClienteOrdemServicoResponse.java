package com.autoflow.presentation.ordemservico.response;

import com.autoflow.domain.ordemservico.OrdemServico;

public record ClienteOrdemServicoResponse(
        Long id,
        String nome,
        String cpfCnpj,
        String email,
        String telefone
) {
    public static ClienteOrdemServicoResponse fromDomain(OrdemServico os) {
        return new ClienteOrdemServicoResponse(
                os.getCliente().getId(),
                os.getCliente().getNome(),
                os.getCliente().getCpfCnpj(),
                os.getCliente().getEmail(),
                os.getCliente().getTelefone()
        );
    }
}
