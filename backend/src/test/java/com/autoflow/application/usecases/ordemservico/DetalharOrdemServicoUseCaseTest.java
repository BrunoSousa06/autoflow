package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetalharOrdemServicoUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;
    @Mock
    private OrcamentoGateway orcamentoGateway;

    @Test
    void deveRetornarOsEOrcamentoDisponivel() {
        OrdemServico ordemServico = new OrdemServico();
        OrcamentoEntity orcamento = new OrcamentoEntity();
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(ordemServico));
        when(orcamentoGateway.findByNumeroOsAndStatus("OS-123", StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.of(orcamento));

        var resultado = new DetalharOrdemServicoUseCase(ordemServicoGateway, orcamentoGateway).execute("OS-123");

        assertSame(ordemServico, resultado.ordemServico());
        assertSame(orcamento, resultado.orcamentoAtual());
    }

    @Test
    void deveUsarUltimoOrcamentoQuandoNaoHouverDisponivel() {
        OrdemServico ordemServico = new OrdemServico();
        OrcamentoEntity orcamento = new OrcamentoEntity();
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(ordemServico));
        when(orcamentoGateway.findByNumeroOsAndStatus("OS-123", StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.empty());
        when(orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc("OS-123"))
                .thenReturn(Optional.of(orcamento));

        var resultado = new DetalharOrdemServicoUseCase(ordemServicoGateway, orcamentoGateway).execute("OS-123");

        assertSame(orcamento, resultado.orcamentoAtual());
        verify(orcamentoGateway).findTopByNumeroOsOrderByVersaoDesc("OS-123");
    }

    @Test
    void deveFalharQuandoOsNaoExistir() {
        when(ordemServicoGateway.findByNumeroOs("OS-404")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> new DetalharOrdemServicoUseCase(ordemServicoGateway, orcamentoGateway)
                .execute("OS-404"));
    }
}
