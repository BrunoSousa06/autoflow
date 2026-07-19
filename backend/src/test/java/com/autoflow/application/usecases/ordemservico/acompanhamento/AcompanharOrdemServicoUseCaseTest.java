package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.mapper.AcompanhamentoMapper;
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
    private ClienteGateway clienteGateway;

    @Mock
    private OrdemServicoGateway ordemServicoGateway;

    @Mock
    private OrcamentoGateway orcamentoGateway;

    @Mock
    private HistoricoStatusOsGateway historicoStatusOsGateway;

    @Mock
    private AcompanhamentoMapper acompanhamentoMapper;

    @InjectMocks
    private AcompanharOrdemServicoUseCase useCase;

    @Test
    void deveRetornarListaDeAcompanhamentos() {

        String email = "cliente@email.com";

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);

        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setNumeroOs("OS001");

        OrcamentoEntity orcamento = new OrcamentoEntity();

        HistoricoStatusOsEntity historico = new HistoricoStatusOsEntity();

        AcompanhamentoOrdemServicoOutput output =
                mock(AcompanhamentoOrdemServicoOutput.class);

        when(clienteGateway.findByUsuarioEmail(email))
                .thenReturn(Optional.of(cliente));

        when(ordemServicoGateway.findByClienteIdOrderByDataAberturaDesc(1L))
                .thenReturn(List.of(ordemServico));

        when(orcamentoGateway.findByNumeroOsAndStatus(
                "OS001",
                StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.of(orcamento));

        when(historicoStatusOsGateway.findByNumeroOsOrderByRegistradoEmAsc("OS001"))
                .thenReturn(List.of(historico));

        when(acompanhamentoMapper.mapToOutPut(
                ordemServico,
                orcamento,
                List.of(historico)))
                .thenReturn(output);

        List<AcompanhamentoOrdemServicoOutput> resultado =
                useCase.execute(email);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(output, resultado.getFirst());

        verify(clienteGateway).findByUsuarioEmail(email);
        verify(ordemServicoGateway)
                .findByClienteIdOrderByDataAberturaDesc(1L);
        verify(orcamentoGateway)
                .findByNumeroOsAndStatus("OS001", StatusOrcamento.DISPONIVEL);
        verify(historicoStatusOsGateway)
                .findByNumeroOsOrderByRegistradoEmAsc("OS001");
        verify(acompanhamentoMapper)
                .mapToOutPut(ordemServico, orcamento, List.of(historico));
    }

    @Test
    void deveUtilizarUltimaVersaoQuandoNaoExistirOrcamentoDisponivel() {

        String email = "cliente@email.com";

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);

        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setNumeroOs("OS001");

        OrcamentoEntity ultimaVersao = new OrcamentoEntity();

        AcompanhamentoOrdemServicoOutput output =
                mock(AcompanhamentoOrdemServicoOutput.class);

        when(clienteGateway.findByUsuarioEmail(email))
                .thenReturn(Optional.of(cliente));

        when(ordemServicoGateway.findByClienteIdOrderByDataAberturaDesc(1L))
                .thenReturn(List.of(ordemServico));

        when(orcamentoGateway.findByNumeroOsAndStatus(
                "OS001",
                StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.empty());

        when(orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc("OS001"))
                .thenReturn(Optional.of(ultimaVersao));

        when(historicoStatusOsGateway.findByNumeroOsOrderByRegistradoEmAsc("OS001"))
                .thenReturn(Collections.emptyList());

        when(acompanhamentoMapper.mapToOutPut(
                ordemServico,
                ultimaVersao,
                Collections.emptyList()))
                .thenReturn(output);

        List<AcompanhamentoOrdemServicoOutput> resultado =
                useCase.execute(email);

        assertEquals(1, resultado.size());
        assertEquals(output, resultado.getFirst());

        verify(orcamentoGateway)
                .findByNumeroOsAndStatus("OS001", StatusOrcamento.DISPONIVEL);

        verify(orcamentoGateway)
                .findTopByNumeroOsOrderByVersaoDesc("OS001");
    }

    @Test
    void deveRetornarOrcamentoNuloQuandoNaoExistirOrcamento() {

        String email = "cliente@email.com";

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);

        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setNumeroOs("OS001");

        AcompanhamentoOrdemServicoOutput output =
                mock(AcompanhamentoOrdemServicoOutput.class);

        when(clienteGateway.findByUsuarioEmail(email))
                .thenReturn(Optional.of(cliente));

        when(ordemServicoGateway.findByClienteIdOrderByDataAberturaDesc(1L))
                .thenReturn(List.of(ordemServico));

        when(orcamentoGateway.findByNumeroOsAndStatus(
                "OS001",
                StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.empty());

        when(orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc("OS001"))
                .thenReturn(Optional.empty());

        when(historicoStatusOsGateway.findByNumeroOsOrderByRegistradoEmAsc("OS001"))
                .thenReturn(Collections.emptyList());

        when(acompanhamentoMapper.mapToOutPut(
                ordemServico,
                null,
                Collections.emptyList()))
                .thenReturn(output);

        List<AcompanhamentoOrdemServicoOutput> resultado =
                useCase.execute(email);

        assertEquals(1, resultado.size());

        verify(acompanhamentoMapper)
                .mapToOutPut(ordemServico, null, Collections.emptyList());
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {

        String email = "cliente@email.com";

        when(clienteGateway.findByUsuarioEmail(email))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class,
                        () -> useCase.execute(email));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(
                "404 NOT_FOUND \"Cliente autenticado não encontrado.\"",
                exception.getMessage());

        verify(clienteGateway).findByUsuarioEmail(email);

        verifyNoInteractions(
                ordemServicoGateway,
                orcamentoGateway,
                historicoStatusOsGateway,
                acompanhamentoMapper);
    }

    @Test
    void deveRetornarListaVaziaQuandoClienteNaoPossuirOrdensServico() {

        String email = "cliente@email.com";

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);

        when(clienteGateway.findByUsuarioEmail(email))
                .thenReturn(Optional.of(cliente));

        when(ordemServicoGateway.findByClienteIdOrderByDataAberturaDesc(1L))
                .thenReturn(Collections.emptyList());

        List<AcompanhamentoOrdemServicoOutput> resultado =
                useCase.execute(email);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(clienteGateway).findByUsuarioEmail(email);
        verify(ordemServicoGateway)
                .findByClienteIdOrderByDataAberturaDesc(1L);

        verifyNoInteractions(
                orcamentoGateway,
                historicoStatusOsGateway,
                acompanhamentoMapper);
    }

}
