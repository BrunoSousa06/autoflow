package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntregarOrdemServicoUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;

    @Mock
    private HistoricoStatusOsGateway historicoStatusOsGateway;

    @Test
    void deveEntregarOsERegistrarHistorico() {
        var os = ordemFinalizada();
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(os));
        when(ordemServicoGateway.save(os)).thenReturn(os);

        var resultado = new EntregarOrdemServicoUseCase(ordemServicoGateway, historicoStatusOsGateway)
                .execute("OS-1");

        assertEquals(StatusOrdemServico.ENTREGUE, resultado.getStatus());
        var captor = ArgumentCaptor.forClass(HistoricoStatusOsEntity.class);
        verify(historicoStatusOsGateway).save(captor.capture());
        assertEquals(1L, captor.getValue().getOrdemServicoId());
        assertEquals("OS-1", captor.getValue().getNumeroOs());
        assertEquals(StatusOrdemServico.ENTREGUE, captor.getValue().getStatus());
    }

    @Test
    void deveRetornar404QuandoOsNaoExiste() {
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.empty());

        var exception = assertThrows(ResponseStatusException.class,
                () -> new EntregarOrdemServicoUseCase(ordemServicoGateway, historicoStatusOsGateway)
                        .execute("OS-1"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deveRejeitarEntregaForaDoStatusFinalizado() {
        var os = ordemFinalizada();
        os.setStatus(StatusOrdemServico.EM_EXECUCAO);
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(os));

        assertThrows(IllegalStateException.class,
                () -> new EntregarOrdemServicoUseCase(ordemServicoGateway, historicoStatusOsGateway)
                        .execute("OS-1"));
    }

    private OrdemServicoEntity ordemFinalizada() {
        var os = new OrdemServicoEntity();
        os.setId(1L);
        os.setNumeroOs("OS-1");
        os.setStatus(StatusOrdemServico.FINALIZADA);
        return os;
    }
}
