package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.port.in.pecainsumo.BaixarEstoqueUseCase;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IniciarServicoUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;

    @Mock
    private BaixarEstoqueUseCase baixarEstoqueUseCase;

    @Test
    void deveBaixarEstoqueSomenteAntesDeIniciarServicoAguardando() {
        var ordem = ordemEmExecucao();
        var servico = servico(10L);
        var item = item(1L, 2);
        servico.registrarItensNecessarios(List.of(item));
        ordem.adicionarServicosSolicitados(List.of(servico));
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-1")).thenReturn(Optional.of(ordem));
        when(baixarEstoqueUseCase.execute(List.of(item))).thenReturn(List.of(item));
        when(ordemServicoGateway.save(ordem)).thenReturn(ordem);

        var resultado = new IniciarServicoUseCase(ordemServicoGateway, baixarEstoqueUseCase)
                .execute("OS-1", 10L);

        assertEquals(ordem, resultado);
        assertEquals(StatusServicoOs.EM_EXECUCAO, servico.getStatus());
        verify(baixarEstoqueUseCase).execute(List.of(item));
        verify(ordemServicoGateway).save(ordem);
    }

    @Test
    void deveImpedirNovaBaixaQuandoServicoJaFoiIniciado() {
        var ordem = ordemEmExecucao();
        var servico = servico(10L);
        servico.iniciar(List.of());
        ordem.adicionarServicosSolicitados(List.of(servico));
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-1")).thenReturn(Optional.of(ordem));
        var useCase = new IniciarServicoUseCase(ordemServicoGateway, baixarEstoqueUseCase);

        assertThrows(IllegalStateException.class,
                () -> useCase.execute("OS-1", 10L));

        verifyNoInteractions(baixarEstoqueUseCase);
        verify(ordemServicoGateway, never()).save(ordem);
    }

    @Test
    void deveInformarQuandoOrdemNaoExistir() {
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-404")).thenReturn(Optional.empty());
        var useCase = new IniciarServicoUseCase(ordemServicoGateway, baixarEstoqueUseCase);

        var erro = assertThrows(ApplicationException.class,
                () -> useCase.execute("OS-404", 10L));

        assertEquals(ApplicationException.ErrorType.NOT_FOUND, erro.type());
        verifyNoInteractions(baixarEstoqueUseCase);
    }

    @Test
    void deveImpedirInicioQuandoOrdemNaoEstiverEmExecucao() {
        var ordem = ordemEmExecucao();
        ordem.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        var servico = servico(10L);
        ordem.adicionarServicosSolicitados(List.of(servico));
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-1")).thenReturn(Optional.of(ordem));
        var useCase = new IniciarServicoUseCase(ordemServicoGateway, baixarEstoqueUseCase);

        assertThrows(IllegalStateException.class,
                () -> useCase.execute("OS-1", 10L));

        verifyNoInteractions(baixarEstoqueUseCase);
        verify(ordemServicoGateway, never()).save(ordem);
    }

    private OrdemServico ordemEmExecucao() {
        var ordem = new OrdemServico();
        ordem.setNumeroOs("OS-1");
        ordem.setStatus(StatusOrdemServico.EM_EXECUCAO);
        return ordem;
    }

    private ServicoSolicitado servico(Long id) {
        return new ServicoSolicitado(id, "Servico", BigDecimal.TEN);
    }

    private ItemNecessario item(Long id, int quantidade) {
        return ItemNecessario.criar(
                id,
                "Item",
                CategoriaPecaInsumo.PECA,
                BigDecimal.ONE,
                quantidade,
                null
        );
    }
}
