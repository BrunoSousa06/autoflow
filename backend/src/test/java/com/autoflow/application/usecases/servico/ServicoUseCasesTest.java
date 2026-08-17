package com.autoflow.application.usecases.servico;

import com.autoflow.application.input.servico.PageInput;
import com.autoflow.application.output.servico.PageOutput;
import com.autoflow.application.input.servico.ServicoInput;
import com.autoflow.application.output.servico.ServicoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.MetricsGateway;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.domain.servico.Servico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(servicoGateway.save(any(Servico.class))).thenReturn(toDomain(output));

        ServicoOutput resultado = new CriarServicoUseCaseImpl(servicoGateway).execute(input);

        assertEquals(output, resultado);
        verify(servicoGateway).save(any(Servico.class));
    }

    @Test
    void deveRejeitarNomeDuplicadoSemSalvar() {
        ServicoInput input = input("Revisão Completa");
        when(servicoGateway.existsByNomeIgnoreCase(input.nome())).thenReturn(true);
        var useCase = new CriarServicoUseCaseImpl(servicoGateway);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> useCase.execute(input)
        );

        assertEquals(ApplicationException.ErrorType.CONFLICT, exception.type());
        assertEquals("Serviço já foi cadastrado", exception.getMessage());
        verify(servicoGateway, never()).save(any());
    }

    @Test
    void deveBuscarServicoPorId() {
        ServicoOutput output = output(1L, true);
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(toDomain(output)));

        ServicoOutput resultado = new BuscarServicoPorIdUseCaseImpl(servicoGateway).execute(1L);

        assertEquals(output, resultado);
    }

    @Test
    void deveRetornar404AoBuscarServicoInexistente() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.empty());
        var useCase = new BuscarServicoPorIdUseCaseImpl(servicoGateway);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> useCase.execute(1L)
        );

        assertEquals(ApplicationException.ErrorType.NOT_FOUND, exception.type());
    }

    @Test
    void deveListarServicosAtivosComPaginacaoInterna() {
        PageInput page = new PageInput(0, 20);
        ServicoOutput output = output(1L, true);
        when(servicoGateway.findAllByAtivoTrue(page))
                .thenReturn(new PageOutput<>(List.of(toDomain(output)), 0, 20, 1));

        PageOutput<ServicoOutput> resultado = new ListarServicosUseCaseImpl(servicoGateway).execute(page);

        assertEquals(List.of(output), resultado.content());
        assertEquals(1, resultado.totalElements());
        verify(servicoGateway).findAllByAtivoTrue(page);
    }

    @Test
    void deveAtualizarServicoExistente() {
        ServicoInput input = input("Revisão Premium");
        ServicoOutput existente = output(1L, true);
        ServicoOutput atualizado = output(1L, true);
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(toDomain(existente)));
        when(servicoGateway.update(any(Servico.class))).thenReturn(toDomain(atualizado));

        ServicoOutput resultado = new AtualizarServicoUseCaseImpl(servicoGateway).execute(1L, input);

        assertEquals(atualizado, resultado);
        verify(servicoGateway).update(any(Servico.class));
    }

    @Test
    void deveRetornar404AoAtualizarServicoInexistente() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.empty());
        ServicoInput input = input("Revisão Premium");
        var useCase = new AtualizarServicoUseCaseImpl(servicoGateway);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> useCase.execute(1L, input)
        );

        assertEquals(ApplicationException.ErrorType.NOT_FOUND, exception.type());
        verify(servicoGateway, never()).update(any(Servico.class));
    }

    @Test
    void deveInativarServicoSemRemoverRegistro() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.of(toDomain(output(1L, true))));

        new InativarServicoUseCaseImpl(servicoGateway).execute(1L);

        verify(servicoGateway).inativar(1L);
    }

    @Test
    void deveRetornar404AoInativarServicoInexistente() {
        when(servicoGateway.findById(1L)).thenReturn(Optional.empty());
        var useCase = new InativarServicoUseCaseImpl(servicoGateway);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> useCase.execute(1L)
        );

        assertEquals(ApplicationException.ErrorType.NOT_FOUND, exception.type());
        verify(servicoGateway, never()).inativar(1L);
    }

    @Test
    void deveCalcularTemposDerivadosDaMetrica() {
        when(metricsGateway.calcularTempoMedioPorServico()).thenReturn(List.of(
                new MetricsGateway.TempoMedioServicoData(1L, "Troca de óleo", 2L, 3600.0)
        ));

        var resultado = new CalcularTempoMedioServicoUseCaseImpl(metricsGateway).execute();

        assertNotNull(resultado);
        assertEquals(3600.0, resultado.getFirst().getTempoMedioSegundos());
        assertEquals(60.0, resultado.getFirst().getTempoMedioMinutos());
        assertEquals(1.0, resultado.getFirst().getTempoMedioHoras());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaMetricas() {
        when(metricsGateway.calcularTempoMedioPorServico()).thenReturn(List.of());

        var resultado = new CalcularTempoMedioServicoUseCaseImpl(metricsGateway).execute();

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

    private static Servico toDomain(ServicoOutput output) {
        return Servico.reconstituir(output.getId(), output.getNome(), output.getDescricao(),
                output.getValor(), output.isAtivo());
    }
}
