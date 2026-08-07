package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.application.gateway.AcompanhamentoMapperGateway;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    @Mock
    private AcompanhamentoMapperGateway acompanhamentoMapper;

    @InjectMocks
    private AcompanharOrdemServicoUseCase useCase;

    @Test
    void deveRetornarListaDeAcompanhamentos() {
        OrdemServicoEntity ordemServico = ordemServico("OS001");
        OrcamentoEntity orcamento = new OrcamentoEntity();
        HistoricoStatusOsEntity historico = new HistoricoStatusOsEntity();
        AcompanhamentoOrdemServicoOutput output = mock(AcompanhamentoOrdemServicoOutput.class);

        configurarClienteComOs(ordemServico);
        when(orcamentoGateway.findByNumeroOsAndStatus("OS001", StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.of(orcamento));
        when(historicoStatusOsGateway.findByNumeroOsOrderByRegistradoEmAsc("OS001"))
                .thenReturn(List.of(historico));
        when(acompanhamentoMapper.mapToOutput(ordemServico, orcamento, List.of(historico)))
                .thenReturn(output);

        assertEquals(List.of(output), useCase.execute("cliente@email.com"));
    }

    @Test
    void deveUtilizarUltimaVersaoQuandoNaoExistirOrcamentoDisponivel() {
        OrdemServicoEntity ordemServico = ordemServico("OS001");
        OrcamentoEntity ultimaVersao = new OrcamentoEntity();
        AcompanhamentoOrdemServicoOutput output = mock(AcompanhamentoOrdemServicoOutput.class);

        configurarClienteComOs(ordemServico);
        when(orcamentoGateway.findByNumeroOsAndStatus("OS001", StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.empty());
        when(orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc("OS001"))
                .thenReturn(Optional.of(ultimaVersao));
        when(historicoStatusOsGateway.findByNumeroOsOrderByRegistradoEmAsc("OS001"))
                .thenReturn(Collections.emptyList());
        when(acompanhamentoMapper.mapToOutput(ordemServico, ultimaVersao, Collections.emptyList()))
                .thenReturn(output);

        assertEquals(List.of(output), useCase.execute("cliente@email.com"));
    }

    @Test
    void deveRetornarOrcamentoNuloQuandoNaoExistirOrcamento() {
        OrdemServicoEntity ordemServico = ordemServico("OS001");
        AcompanhamentoOrdemServicoOutput output = mock(AcompanhamentoOrdemServicoOutput.class);

        configurarClienteComOs(ordemServico);
        when(orcamentoGateway.findByNumeroOsAndStatus("OS001", StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.empty());
        when(orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc("OS001"))
                .thenReturn(Optional.empty());
        when(historicoStatusOsGateway.findByNumeroOsOrderByRegistradoEmAsc("OS001"))
                .thenReturn(Collections.emptyList());
        when(acompanhamentoMapper.mapToOutput(ordemServico, null, Collections.emptyList()))
                .thenReturn(output);

        assertEquals(List.of(output), useCase.execute("cliente@email.com"));
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {
        when(clienteGateway.findIdByUsuarioEmail("ausente@email.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> useCase.execute("ausente@email.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(ordemServicoGateway, orcamentoGateway,
                historicoStatusOsGateway, acompanhamentoMapper);
    }

    @Test
    void deveRetornarListaVaziaQuandoClienteNaoPossuirOrdensServico() {
        when(clienteGateway.findIdByUsuarioEmail("cliente@email.com")).thenReturn(Optional.of(1L));
        when(ordemServicoGateway.findByClienteIdOrderByDataAberturaDesc(1L))
                .thenReturn(Collections.emptyList());

        assertTrue(useCase.execute("cliente@email.com").isEmpty());
        verifyNoInteractions(orcamentoGateway, historicoStatusOsGateway, acompanhamentoMapper);
    }

    private void configurarClienteComOs(OrdemServicoEntity ordemServico) {
        when(clienteGateway.findIdByUsuarioEmail("cliente@email.com")).thenReturn(Optional.of(1L));
        when(ordemServicoGateway.findByClienteIdOrderByDataAberturaDesc(1L))
                .thenReturn(List.of(ordemServico));
    }

    private OrdemServicoEntity ordemServico(String numeroOs) {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setNumeroOs(numeroOs);
        return ordemServico;
    }
}
