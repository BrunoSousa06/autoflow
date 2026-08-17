package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.port.in.orcamento.RecusarOrcamentoUseCase;
import com.autoflow.application.usecases.ordemservico.reparoadicional.RecusarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecusarOrcamentoUseCaseTest {
    @Mock OrcamentoGateway orcamentoGateway;
    @Mock OrdemServicoGateway ordemServicoGateway;
    @Mock RecusarReparoAdicionalPorOrcamentoUseCase reparoUseCase;
    @InjectMocks RecusarOrcamentoUseCaseImpl useCase;

    @Test
    void deveRecusarEFinalizarOsQuandoNaoHaReparoAdicional() {
        OrcamentoEntity orcamento = orcamentoDisponivel();
        OrdemServico os = osAguardandoAprovacao();
        when(orcamentoGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reparoUseCase.executeSeExistir(10L, "Nao quero")).thenReturn(false);
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));

        OrcamentoEntity resultado = useCase.execute(orcamento, "Nao quero", "Maria");

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
        OrcamentoEntity orcamento = orcamentoDisponivel();
        when(orcamentoGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reparoUseCase.executeSeExistir(10L, "Motivo")).thenReturn(true);

        useCase.execute(orcamento, "Motivo", "Maria");

        verifyNoInteractions(ordemServicoGateway);
    }

    @Test
    void deveAceitarRecusaSemMotivo() {
        OrcamentoEntity orcamento = orcamentoDisponivel();
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
        OrcamentoEntity orcamento = orcamentoDisponivel();
        orcamento.setStatus(StatusOrcamento.REPROVADO);

        assertSame(orcamento, useCase.execute(orcamento, "Novo motivo", "Maria"));

        verifyNoInteractions(orcamentoGateway, ordemServicoGateway, reparoUseCase);
    }

    @Test
    void deveBloquearRecusaConflitanteDepoisDaAprovacao() {
        OrcamentoEntity orcamento = orcamentoDisponivel();
        orcamento.setStatus(StatusOrcamento.APROVADO);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> useCase.execute(orcamento, null, "Maria"));

        assertEquals(ApplicationException.ErrorType.BAD_REQUEST, exception.type());
        verifyNoInteractions(orcamentoGateway, ordemServicoGateway, reparoUseCase);
    }

    @Test
    void deveRejeitarMotivoMaiorQueLimiteDaColuna() {
        OrcamentoEntity orcamento = orcamentoDisponivel();
        String motivo = "x".repeat(501);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> useCase.execute(orcamento, motivo, "Maria"));

        assertEquals(ApplicationException.ErrorType.BAD_REQUEST, exception.type());

        verifyNoInteractions(orcamentoGateway, ordemServicoGateway, reparoUseCase);
    }

    private OrcamentoEntity orcamentoDisponivel() {
        OrcamentoEntity orcamento = new OrcamentoEntity();
        orcamento.setId(10L); orcamento.setNumeroOs("OS-123"); orcamento.setStatus(StatusOrcamento.DISPONIVEL);
        return orcamento;
    }

    private OrdemServico osAguardandoAprovacao() {
        OrdemServico os = new OrdemServico();
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        return os;
    }
}
