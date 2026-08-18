package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.application.usecases.ordemservico.acompanhamento.AcompanharOrdemServicoUseCaseImpl;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.HistoricoStatusOs;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.ordemservico.Veiculo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcompanharOrdemServicoUseCaseTest {

    @Mock
    private VeiculoClienteGateway clienteGateway;
    @Mock
    private OrdemServicoGateway ordemServicoGateway;
    @Mock
    private OrcamentoGateway orcamentoGateway;
    @Mock
    private HistoricoStatusOsGateway historicoStatusOsGateway;
    @InjectMocks
    private AcompanharOrdemServicoUseCaseImpl useCase;

    @Test
    void deveRetornarListaDeAcompanhamentos() {
        OrdemServico ordemServico = ordemServico("OS001");
        Orcamento orcamento = new Orcamento();
        orcamento.setStatus(StatusOrcamento.DISPONIVEL);
        HistoricoStatusOs historico = new HistoricoStatusOs();
        configurarClienteComOs(ordemServico);
        when(orcamentoGateway.findByNumeroOsAndStatus("OS001", StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.of(orcamento));
        when(historicoStatusOsGateway.findByNumeroOsOrderByRegistradoEmAsc("OS001"))
                .thenReturn(List.of(historico));
        var resultado = useCase.execute("cliente@email.com");

        assertEquals(1, resultado.size());
        assertEquals("OS001", resultado.get(0).numeroOs());
        assertEquals(StatusOrcamento.DISPONIVEL, resultado.get(0).situacaoAprovacao());
        assertEquals(1, resultado.get(0).historicoStatus().size());
    }

    @Test
    void deveUtilizarUltimaVersaoQuandoNaoExistirOrcamentoDisponivel() {
        OrdemServico ordemServico = ordemServico("OS001");
        Orcamento ultimaVersao = new Orcamento();
        ultimaVersao.setStatus(StatusOrcamento.DISPONIVEL);
        configurarClienteComOs(ordemServico);
        when(orcamentoGateway.findByNumeroOsAndStatus("OS001", StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.empty());
        when(orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc("OS001"))
                .thenReturn(Optional.of(ultimaVersao));
        when(historicoStatusOsGateway.findByNumeroOsOrderByRegistradoEmAsc("OS001"))
                .thenReturn(Collections.emptyList());
        var resultado = useCase.execute("cliente@email.com");

        assertEquals(1, resultado.size());
        assertEquals(StatusOrcamento.DISPONIVEL, resultado.get(0).situacaoAprovacao());
    }

    @Test
    void deveRetornarOrcamentoNuloQuandoNaoExistirOrcamento() {
        OrdemServico ordemServico = ordemServico("OS001");
        configurarClienteComOs(ordemServico);
        when(orcamentoGateway.findByNumeroOsAndStatus("OS001", StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.empty());
        when(orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc("OS001"))
                .thenReturn(Optional.empty());
        when(historicoStatusOsGateway.findByNumeroOsOrderByRegistradoEmAsc("OS001"))
                .thenReturn(Collections.emptyList());
        var resultado = useCase.execute("cliente@email.com");

        assertEquals(1, resultado.size());
        assertNull(resultado.get(0).orcamentoAtual());
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {
        when(clienteGateway.findIdByUsuarioEmail("ausente@email.com")).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> useCase.execute("ausente@email.com"));

        assertEquals(ApplicationException.ErrorType.NOT_FOUND, exception.type());
        verifyNoInteractions(ordemServicoGateway, orcamentoGateway,
                historicoStatusOsGateway);
    }

    @Test
    void deveRetornarListaVaziaQuandoClienteNaoPossuirOrdensServico() {
        when(clienteGateway.findIdByUsuarioEmail("cliente@email.com")).thenReturn(Optional.of(1L));
        when(ordemServicoGateway.findByClienteIdOrderByDataAberturaDesc(1L))
                .thenReturn(Collections.emptyList());

        assertTrue(useCase.execute("cliente@email.com").isEmpty());
        verifyNoInteractions(orcamentoGateway, historicoStatusOsGateway);
    }

    private void configurarClienteComOs(OrdemServico ordemServico) {
        when(clienteGateway.findIdByUsuarioEmail("cliente@email.com")).thenReturn(Optional.of(1L));
        when(ordemServicoGateway.findByClienteIdOrderByDataAberturaDesc(1L))
                .thenReturn(List.of(ordemServico));
    }

    private OrdemServico ordemServico(String numeroOs) {
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setNumeroOs(numeroOs);
        ordemServico.setVeiculo(new Veiculo(1L, "ABC1D23", null, null, null));
        ordemServico.setStatus(StatusOrdemServico.RECEBIDA);
        ordemServico.setServicosSolicitados(Collections.emptyList());
        return ordemServico;
    }
}
