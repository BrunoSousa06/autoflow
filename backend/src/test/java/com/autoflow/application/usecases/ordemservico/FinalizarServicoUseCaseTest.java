package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalizarServicoUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;

    @Mock
    private HistoricoStatusOsGateway historicoStatusOsGateway;

    @Test
    void deveFinalizarOsERegistrarHistoricoQuandoUltimoServicoForFinalizado() {
        var os = ordemComServicos(servicoEmExecucao(1L));
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(os));
        when(ordemServicoGateway.save(os)).thenReturn(os);

        var resultado = new FinalizarServicoUseCase(ordemServicoGateway, historicoStatusOsGateway)
                .execute("OS-1", 1L);

        assertEquals(StatusOrdemServico.FINALIZADA, resultado.getStatus());
        verify(historicoStatusOsGateway).save(any());
    }

    @Test
    void deveManterOsEmExecucaoQuandoHouverOutroServicoPendente() {
        var os = ordemComServicos(servicoEmExecucao(1L), ServicoSolicitado.criar(2L, "Pendente", BigDecimal.TEN));
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(os));
        when(ordemServicoGateway.save(os)).thenReturn(os);

        new FinalizarServicoUseCase(ordemServicoGateway, historicoStatusOsGateway)
                .execute("OS-1", 1L);

        assertEquals(StatusOrdemServico.EM_EXECUCAO, os.getStatus());
        verify(historicoStatusOsGateway, never()).save(any());
    }

    private OrdemServico ordemComServicos(ServicoSolicitado... servicos) {
        var os = new OrdemServico();
        os.setId(1L);
        os.setNumeroOs("OS-1");
        os.setStatus(StatusOrdemServico.EM_EXECUCAO);
        os.adicionarServicosSolicitados(List.of(servicos));
        return os;
    }

    private ServicoSolicitado servicoEmExecucao(Long id) {
        var servico = ServicoSolicitado.criar(id, "Servico", BigDecimal.TEN);
        servico.iniciar(List.of());
        return servico;
    }
}
