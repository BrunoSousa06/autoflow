package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.usecases.pecainsumo.BaixarEstoqueUseCase;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.ordemservico.StatusServicoOs;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

        assertThrows(IllegalStateException.class,
                () -> new IniciarServicoUseCase(ordemServicoGateway, baixarEstoqueUseCase)
                        .execute("OS-1", 10L));

        verifyNoInteractions(baixarEstoqueUseCase);
        verify(ordemServicoGateway, never()).save(ordem);
    }

    @Test
    void deveInformarQuandoOrdemNaoExistir() {
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-404")).thenReturn(Optional.empty());

        var erro = assertThrows(ResponseStatusException.class,
                () -> new IniciarServicoUseCase(ordemServicoGateway, baixarEstoqueUseCase)
                        .execute("OS-404", 10L));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        verifyNoInteractions(baixarEstoqueUseCase);
    }

    @Test
    void deveImpedirInicioQuandoOrdemNaoEstiverEmExecucao() {
        var ordem = ordemEmExecucao();
        ordem.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        var servico = servico(10L);
        ordem.adicionarServicosSolicitados(List.of(servico));
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-1")).thenReturn(Optional.of(ordem));

        assertThrows(IllegalStateException.class,
                () -> new IniciarServicoUseCase(ordemServicoGateway, baixarEstoqueUseCase)
                        .execute("OS-1", 10L));

        verifyNoInteractions(baixarEstoqueUseCase);
        verify(ordemServicoGateway, never()).save(ordem);
    }

    private OrdemServicoEntity ordemEmExecucao() {
        var ordem = new OrdemServicoEntity();
        ordem.setNumeroOs("OS-1");
        ordem.setStatus(StatusOrdemServico.EM_EXECUCAO);
        return ordem;
    }

    private ServicoSolicitadoEntity servico(Long id) {
        return new ServicoSolicitadoEntity(id, "Servico", BigDecimal.TEN);
    }

    private ItemNecessarioEntity item(Long id, int quantidade) {
        return ItemNecessarioEntity.criar(
                id,
                "Item",
                CategoriaPecaInsumo.PECA,
                BigDecimal.ONE,
                quantidade,
                null
        );
    }
}
