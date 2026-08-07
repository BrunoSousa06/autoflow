package com.autoflow.application.usecases.ordemservico;

import com.autoflow.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StatusOrdemServicoMensagemPolicyTest {

    @Test
    void devePossuirMensagemParaCadaStatusDaOrdem() {
        for (StatusOrdemServico status : StatusOrdemServico.values()) {
            String mensagem = StatusOrdemServicoMensagemPolicy.mensagem(status);

            assertNotNull(mensagem);
            assertFalse(mensagem.isBlank());
        }
    }
}
