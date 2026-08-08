package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.PageInput;
import com.autoflow.application.dto.servico.PageOutput;
import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.gateway.MetricsGateway;
import com.autoflow.application.gateway.ServicoGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoUseCasesTest {

    @Mock
    private ServicoGateway servicoGateway;

    @Mock
    private MetricsGateway metricsGateway;

    @Test
    void deveCriarServicoSemExporEntidade() {
        ServicoInput input = input("Revisão Completa");
        ServicoOutput output = output(1L, true);
        when(servicoGateway.existsByNomeIgnoreCase(input.nome())).thenReturn(false);
        when(servicoGateway.save(input)).thenReturn(output);

        ServicoOutput resultado = new CriarServicoUseCase(servicoGateway).execute(input);

        assertEquals(output, resultado);
        verify(servicoGateway).save(input);
    }

    @Test
    void deveRejeitarNomeDuplicadoSemSalvar() {
        ServicoInput input = input("Revisão Completa");
        when(servicoGateway.existsByNomeIgnoreCase(input.nome())).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new CriarServicoUseCase(servicoGateway).execute(input)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Serviço já foi cadastrado", exception.getReason());
        verify(servicoGateway, never()).save(any());
    }

    @Test
    void deveBuscarServicoPorId() {
        ServicoOutput output = output(1L, true);
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(output));

        ServicoOutput resultado = new BuscarServicoPorIdUseCase(servicoGateway).execute(1L);

        assertEquals(output, resultado);
    }

    @Test
    void deveRetornar404AoBuscarServicoInexistente() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new BuscarServicoPorIdUseCase(servicoGateway).execute(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deveListarServicosAtivosComPaginacaoInterna() {
        PageInput page = new PageInput(0, 20);
        ServicoOutput output = output(1L, true);
        when(servicoGateway.findAllByAtivoTrue(page))
                .thenReturn(new PageOutput<>(List.of(output), 0, 20, 1));

        PageOutput<ServicoOutput> resultado = new ListarServicosUseCase(servicoGateway).execute(page);

        assertEquals(List.of(output), resultado.content());
        assertEquals(1, resultado.totalElements());
        verify(servicoGateway).findAllByAtivoTrue(page);
    }

    @Test
    void deveAtualizarServicoExistente() {
        ServicoInput input = input("Revisão Premium");
        ServicoOutput existente = output(1L, true);
        ServicoOutput atualizado = output(1L, true);
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(existente));
        when(servicoGateway.update(1L, input)).thenReturn(atualizado);

        ServicoOutput resultado = new AtualizarServicoUseCase(servicoGateway).execute(1L, input);

        assertEquals(atualizado, resultado);
        verify(servicoGateway).update(1L, input);
    }

    @Test
    void deveRetornar404AoAtualizarServicoInexistente() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new AtualizarServicoUseCase(servicoGateway).execute(1L, input("Revisão Premium"))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(servicoGateway, never()).update(any(), any());
    }

    @Test
    void deveInativarServicoSemRemoverRegistro() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(output(1L, true)));

        new InativarServicoUseCase(servicoGateway).execute(1L);

        verify(servicoGateway).inativar(1L);
    }

    @Test
    void deveRetornar404AoInativarServicoInexistente() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> new InativarServicoUseCase(servicoGateway).execute(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(servicoGateway, never()).inativar(1L);
    }

    @Test
    void deveCalcularTemposDerivadosDaMetrica() {
        when(metricsGateway.calcularTempoMedioPorServico()).thenReturn(List.of(
                new MetricsGateway.TempoMedioServicoData(1L, "Troca de óleo", 2L, 3600.0)
        ));

        var resultado = new CalcularTempoMedioServicoUseCase(metricsGateway).execute();

        assertNotNull(resultado);
        assertEquals(3600.0, resultado.getFirst().getTempoMedioSegundos());
        assertEquals(60.0, resultado.getFirst().getTempoMedioMinutos());
        assertEquals(1.0, resultado.getFirst().getTempoMedioHoras());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaMetricas() {
        when(metricsGateway.calcularTempoMedioPorServico()).thenReturn(List.of());

        var resultado = new CalcularTempoMedioServicoUseCase(metricsGateway).execute();

        assertTrue(resultado.isEmpty());
    }

    private static ServicoInput input(String nome) {
        return new ServicoInput(nome, "Descrição do serviço", new BigDecimal("150.00"));
    }

    private static ServicoOutput output(Long id, boolean ativo) {
        return ServicoOutput.builder()
                .id(id)
                .nome("Revisão Completa")
                .descricao("Descrição do serviço")
                .valor(new BigDecimal("150.00"))
                .ativo(ativo)
                .build();
    }
}
