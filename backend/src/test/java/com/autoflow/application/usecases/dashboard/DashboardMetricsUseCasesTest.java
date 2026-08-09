package com.autoflow.application.usecases.dashboard;

import com.autoflow.application.gateway.MetricsGateway;
import com.autoflow.application.usecases.ordemservico.CalcularTempoMedioOrdemServicoUseCase;
import com.autoflow.application.usecases.servico.CalcularTempoMedioServicoUseCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardMetricsUseCasesTest {

    private final MetricsGateway metricsGateway = mock(MetricsGateway.class);

    @Test
    void deveConverterSegundosEmMinutosEHorasParaOrdensServico() {
        when(metricsGateway.calcularTempoMedioOrdensServico())
                .thenReturn(new MetricsGateway.TempoMedioOrdemServicoData(5L, 5400.0));

        var resultado = new CalcularTempoMedioOrdemServicoUseCase(metricsGateway).execute();

        assertEquals(5L, resultado.quantidadeOrdensFinalizadas());
        assertEquals(5400.0, resultado.tempoMedioSegundos());
        assertEquals(90.0, resultado.tempoMedioMinutos());
        assertEquals(1.5, resultado.tempoMedioHoras());
    }

    @Test
    void devePreservarTemposNulosQuandoNaoHaOrdensFinalizadas() {
        when(metricsGateway.calcularTempoMedioOrdensServico())
                .thenReturn(new MetricsGateway.TempoMedioOrdemServicoData(0L, null));

        var resultado = new CalcularTempoMedioOrdemServicoUseCase(metricsGateway).execute();

        assertEquals(0L, resultado.quantidadeOrdensFinalizadas());
        assertNull(resultado.tempoMedioSegundos());
        assertNull(resultado.tempoMedioMinutos());
        assertNull(resultado.tempoMedioHoras());
    }

    @Test
    void deveConverterMetricasDeServicoEPreservarQuantidade() {
        when(metricsGateway.calcularTempoMedioPorServico()).thenReturn(List.of(
                new MetricsGateway.TempoMedioServicoData(1L, "Revisao", 8L, 90.0),
                new MetricsGateway.TempoMedioServicoData(2L, "Diagnostico", 2L, 7200.0)
        ));

        var resultado = new CalcularTempoMedioServicoUseCase(metricsGateway).execute();

        assertEquals(2, resultado.size());
        assertEquals(1.5, resultado.getFirst().getTempoMedioMinutos());
        assertEquals(0.025, resultado.getFirst().getTempoMedioHoras());
        assertEquals(8L, resultado.getFirst().getQuantidadeExecucoes());
        assertEquals(120.0, resultado.getLast().getTempoMedioMinutos());
        assertEquals(2.0, resultado.getLast().getTempoMedioHoras());
    }

    @Test
    void devePreservarNuloNasConversoesQuandoTempoDoServicoForNulo() {
        when(metricsGateway.calcularTempoMedioPorServico()).thenReturn(List.of(
                new MetricsGateway.TempoMedioServicoData(1L, "Revisao", 0L, null)
        ));

        var resultado = new CalcularTempoMedioServicoUseCase(metricsGateway).execute().getFirst();

        assertNull(resultado.getTempoMedioSegundos());
        assertNull(resultado.getTempoMedioMinutos());
        assertNull(resultado.getTempoMedioHoras());
        assertEquals(0L, resultado.getQuantidadeExecucoes());
    }
}
