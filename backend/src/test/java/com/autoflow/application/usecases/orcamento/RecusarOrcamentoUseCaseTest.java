package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.port.in.orcamento.RecusarOrcamentoUseCase;
import com.autoflow.application.port.in.ordemservico.reparoadicional.RecusarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.application.usecases.ordemservico.RegistrarHistoricoStatusOsService;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecusarOrcamentoUseCaseTest {
    @Mock OrcamentoGateway orcamentoGateway;
    @Mock OrdemServicoGateway ordemServicoGateway;
    @Mock RecusarReparoAdicionalPorOrcamentoUseCase reparoUseCase;
    @Mock RegistrarHistoricoStatusOsService registrarHistoricoStatusOs;
    @org.mockito.Spy Clock clock = Clock.fixed(Instant.parse("2026-08-18T15:30:00Z"), ZoneOffset.UTC);
    @InjectMocks RecusarOrcamentoUseCaseImpl useCase;

    @Test
    void deveRecusarEFinalizarOsQuandoNaoHaReparoAdicional() {
        Orcamento orcamento = orcamentoDisponivel();
        OrdemServico os = osAguardandoAprovacao();
        when(orcamentoGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reparoUseCase.executeSeExistir(10L, "Nao quero")).thenReturn(false);
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));

        Orcamento resultado = useCase.execute(orcamento, "Nao quero", "Maria");

        assertSame(orcamento, resultado);
        assertEquals(StatusOrcamento.REPROVADO, resultado.getStatus());
        assertEquals("Nao quero", resultado.getRecusaMotivo());
        assertEquals("Maria", resultado.getAssinaturaNome());
        assertNotNull(resultado.getReprovadoEm());
        assertEquals(StatusOrdemServico.FINALIZADA, os.getStatus());
        verify(ordemServicoGateway).save(os);
    }

    @Test
    void naoDeveFinalizarOsQuandoRecusaForDeReparoAdicional() {
        Orcamento orcamento = orcamentoDisponivel();
        when(orcamentoGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reparoUseCase.executeSeExistir(10L, "Motivo")).thenReturn(true);

        useCase.execute(orcamento, "Motivo", "Maria");

        verifyNoInteractions(ordemServicoGateway);
    }

    @Test
    void deveAceitarRecusaSemMotivo() {
        Orcamento orcamento = orcamentoDisponivel();
        OrdemServico os = osAguardandoAprovacao();
        when(orcamentoGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reparoUseCase.executeSeExistir(10L, null)).thenReturn(false);
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));

        useCase.execute(orcamento, null, "Maria");

        assertNull(orcamento.getRecusaMotivo());
        assertEquals(StatusOrdemServico.FINALIZADA, os.getStatus());
    }

    @Test
    void deveSerIdempotenteQuandoOrcamentoJaEstiverRecusado() {
        Orcamento orcamento = orcamentoDisponivel();
        orcamento.setStatus(StatusOrcamento.REPROVADO);

        assertSame(orcamento, useCase.execute(orcamento, "Novo motivo", "Maria"));

        verifyNoInteractions(orcamentoGateway, ordemServicoGateway, reparoUseCase);
    }

    @Test
    void deveBloquearRecusaConflitanteDepoisDaAprovacao() {
        Orcamento orcamento = orcamentoDisponivel();
        orcamento.setStatus(StatusOrcamento.APROVADO);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> useCase.execute(orcamento, null, "Maria"));

        assertEquals(ApplicationException.ErrorType.BAD_REQUEST, exception.type());
        verifyNoInteractions(orcamentoGateway, ordemServicoGateway, reparoUseCase);
    }

    @Test
    void deveRejeitarMotivoMaiorQueLimiteDaColuna() {
        Orcamento orcamento = orcamentoDisponivel();
        String motivo = "x".repeat(501);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> useCase.execute(orcamento, motivo, "Maria"));

        assertEquals(ApplicationException.ErrorType.BAD_REQUEST, exception.type());

        verifyNoInteractions(orcamentoGateway, ordemServicoGateway, reparoUseCase);
    }

    private Orcamento orcamentoDisponivel() {
        Orcamento orcamento = new Orcamento();
        orcamento.setId(10L); orcamento.setNumeroOs("OS-123"); orcamento.setStatus(StatusOrcamento.DISPONIVEL);
        return orcamento;
    }

    private OrdemServico osAguardandoAprovacao() {
        OrdemServico os = new OrdemServico();
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        return os;
    }
}
