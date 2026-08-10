package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.usecases.ordemservico.reparoadicional.AprovarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
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
class AprovarOrcamentoUseCaseTest {
    @Mock OrcamentoGateway orcamentoGateway;
    @Mock OrdemServicoGateway ordemServicoGateway;
    @Mock AprovarReparoAdicionalPorOrcamentoUseCase reparoUseCase;
    @InjectMocks AprovarOrcamentoUseCase useCase;

    @Test
    void deveAprovarEIniciarOsQuandoNaoHaReparoAdicional() {
        OrcamentoEntity orcamento = orcamentoDisponivel();
        OrdemServicoEntity os = osAguardandoAprovacao();
        when(orcamentoGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reparoUseCase.executeSeExistir(10L)).thenReturn(false);
        when(ordemServicoGateway.findById(1L)).thenReturn(Optional.of(os));

        OrcamentoEntity resultado = useCase.execute(orcamento, "Maria");

        assertSame(orcamento, resultado);
        assertEquals(StatusOrcamento.APROVADO, resultado.getStatus());
        assertEquals("Maria", resultado.getAssinaturaNome());
        assertNotNull(resultado.getAprovadoEm());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, os.getStatus());
        verify(ordemServicoGateway).save(os);
    }

    @Test
    void naoDeveIniciarOsQuandoAprovacaoForDeReparoAdicional() {
        OrcamentoEntity orcamento = orcamentoDisponivel();
        when(orcamentoGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reparoUseCase.executeSeExistir(10L)).thenReturn(true);

        useCase.execute(orcamento, "Maria");

        verifyNoInteractions(ordemServicoGateway);
    }

    @Test
    void deveFalharParaStatusNaoDisponivel() {
        OrcamentoEntity orcamento = orcamentoDisponivel();
        orcamento.setStatus(StatusOrcamento.SUBSTITUIDO);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> useCase.execute(orcamento, "Maria"));

        assertEquals(ApplicationException.ErrorType.BAD_REQUEST, exception.type());
        verifyNoInteractions(orcamentoGateway, ordemServicoGateway, reparoUseCase);
    }

    @Test
    void deveSerIdempotenteQuandoOrcamentoJaEstiverAprovado() {
        OrcamentoEntity orcamento = orcamentoDisponivel();
        orcamento.setStatus(StatusOrcamento.APROVADO);

        assertSame(orcamento, useCase.execute(orcamento, "Maria"));

        verifyNoInteractions(orcamentoGateway, ordemServicoGateway, reparoUseCase);
    }

    @Test
    void deveBloquearAprovacaoConflitanteDepoisDaRecusa() {
        OrcamentoEntity orcamento = orcamentoDisponivel();
        orcamento.setStatus(StatusOrcamento.REPROVADO);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> useCase.execute(orcamento, "Maria"));

        assertEquals(ApplicationException.ErrorType.BAD_REQUEST, exception.type());
        verifyNoInteractions(orcamentoGateway, ordemServicoGateway, reparoUseCase);
    }

    private OrcamentoEntity orcamentoDisponivel() {
        OrcamentoEntity orcamento = new OrcamentoEntity();
        orcamento.setId(10L); orcamento.setOrdemServicoId(1L); orcamento.setStatus(StatusOrcamento.DISPONIVEL);
        return orcamento;
    }

    private OrdemServicoEntity osAguardandoAprovacao() {
        OrdemServicoEntity os = new OrdemServicoEntity();
        os.setId(1L); os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        return os;
    }
}
