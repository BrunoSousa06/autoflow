package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.SituacaoEstoque;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.ordemservico.StatusServicoOs;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.domain.ordemservico.reparoadicional.StatusReparoAdicional;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReparoAdicionalEstadoUseCasesTest {

    @Mock ReparoAdicionalGateway reparoAdicionalGateway;
    @Mock OrdemServicoGateway ordemServicoGateway;
    @Mock ConsultarReparoAdicionalPorOrcamentoUseCase consultarPorOrcamentoUseCase;
    @Mock AprovarReparoAdicionalUseCase aprovarReparoAdicionalUseCase;
    @Mock RecusarReparoAdicionalUseCase recusarReparoAdicionalUseCase;

    @Test
    void deveAprovarReparoECopiarProfundamenteServicosEItensParaOs() {
        var reparo = reparoPendente();
        var servicoOriginal = reparo.getServicos().getFirst();
        var itemOriginal = servicoOriginal.getItensNecessarios().getFirst();
        var ordemServico = ordemServico();
        when(reparoAdicionalGateway.findById(40L)).thenReturn(Optional.of(reparo));
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(ordemServico));
        when(reparoAdicionalGateway.save(reparo)).thenReturn(reparo);
        when(ordemServicoGateway.save(ordemServico)).thenReturn(ordemServico);

        var resultado = new AprovarReparoAdicionalUseCase(
                reparoAdicionalGateway,
                ordemServicoGateway
        ).execute(40L);

        assertSame(ordemServico, resultado);
        assertEquals(StatusReparoAdicional.APROVADO, reparo.getStatus());
        assertNotNull(reparo.getAprovadoEm());
        assertEquals(1, ordemServico.getServicosSolicitados().size());

        var servicoCopiado = ordemServico.getServicosSolicitados().getFirst();
        var itemCopiado = servicoCopiado.getItensNecessarios().getFirst();
        assertNotSame(servicoOriginal, servicoCopiado);
        assertNotSame(itemOriginal, itemCopiado);
        assertSame(ordemServico, servicoCopiado.getOrdemServico());
        assertEquals(servicoOriginal.getServicoId(), servicoCopiado.getServicoId());
        assertEquals(itemOriginal.getPecaInsumoId(), itemCopiado.getPecaInsumoId());
        assertEquals(itemOriginal.getQuantidadeDisponivel(), itemCopiado.getQuantidadeDisponivel());
        assertEquals(itemOriginal.getMotivoPendencia(), itemCopiado.getMotivoPendencia());

        InOrder ordem = inOrder(reparoAdicionalGateway, ordemServicoGateway);
        ordem.verify(reparoAdicionalGateway).save(reparo);
        ordem.verify(ordemServicoGateway).save(ordemServico);
    }

    @Test
    void devePropagarFalhaAoSalvarOsParaPermitirRollbackTransacional() {
        var reparo = reparoPendente();
        var ordemServico = ordemServico();
        when(reparoAdicionalGateway.findById(40L)).thenReturn(Optional.of(reparo));
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(ordemServico));
        when(reparoAdicionalGateway.save(reparo)).thenReturn(reparo);
        when(ordemServicoGateway.save(ordemServico)).thenThrow(new RuntimeException("banco indisponível"));

        var useCase = new AprovarReparoAdicionalUseCase(
                reparoAdicionalGateway,
                ordemServicoGateway
        );

        assertThrows(RuntimeException.class, () -> useCase.execute(40L));
        verify(reparoAdicionalGateway).save(reparo);
        verify(ordemServicoGateway).save(ordemServico);
    }

    @Test
    void deveRejeitarAprovacaoForaDoEstadoPendenteSemPersistir() {
        var reparo = reparoPendente();
        reparo.aprovar();
        when(reparoAdicionalGateway.findById(40L)).thenReturn(Optional.of(reparo));
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(ordemServico()));

        var useCase = new AprovarReparoAdicionalUseCase(
                reparoAdicionalGateway,
                ordemServicoGateway
        );

        assertThrows(IllegalStateException.class, () -> useCase.execute(40L));
        verify(reparoAdicionalGateway, never()).save(reparo);
        verify(ordemServicoGateway, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveRecusarReparoSemAlterarOrdemServico() {
        var reparo = reparoPendente();
        when(reparoAdicionalGateway.findById(40L)).thenReturn(Optional.of(reparo));
        when(reparoAdicionalGateway.save(reparo)).thenReturn(reparo);

        var resultado = new RecusarReparoAdicionalUseCase(reparoAdicionalGateway)
                .execute(40L, "Cliente recusou o orçamento complementar");

        assertSame(reparo, resultado);
        assertEquals(StatusReparoAdicional.RECUSADO, reparo.getStatus());
        assertEquals("Cliente recusou o orçamento complementar", reparo.getMotivoRecusa());
        assertNotNull(reparo.getRecusadoEm());
        verify(reparoAdicionalGateway).save(reparo);
        verifyNoInteractions(ordemServicoGateway);
    }

    @Test
    void deveRejeitarRecusaForaDoEstadoPendenteSemPersistir() {
        var reparo = reparoPendente();
        reparo.recusar("primeira recusa");
        when(reparoAdicionalGateway.findById(40L)).thenReturn(Optional.of(reparo));

        var useCase = new RecusarReparoAdicionalUseCase(reparoAdicionalGateway);

        assertThrows(IllegalStateException.class, () -> useCase.execute(40L, "nova recusa"));
        verify(reparoAdicionalGateway, never()).save(reparo);
    }

    @Test
    void deveInformarReparoInexistenteNasOperacoesPorId() {
        when(reparoAdicionalGateway.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> new AprovarReparoAdicionalUseCase(reparoAdicionalGateway, ordemServicoGateway)
                        .execute(99L)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new RecusarReparoAdicionalUseCase(reparoAdicionalGateway)
                        .execute(99L, "motivo")
        );
        verifyNoInteractions(ordemServicoGateway);
    }

    @Test
    void deveConsultarReparoPorOrcamentoComoResultadoOpcional() {
        var reparo = reparoPendente();
        var useCase = new ConsultarReparoAdicionalPorOrcamentoUseCase(reparoAdicionalGateway);
        when(reparoAdicionalGateway.findByOrcamentoId(30L)).thenReturn(Optional.of(reparo));
        when(reparoAdicionalGateway.findByOrcamentoId(31L)).thenReturn(Optional.empty());

        assertSame(reparo, useCase.execute(30L).orElseThrow());
        assertTrue(useCase.execute(31L).isEmpty());
    }

    @Test
    void deveAprovarPorOrcamentoQuandoExistirEIgnorarQuandoAusente() {
        var reparo = reparoPendente();
        var useCase = new AprovarReparoAdicionalPorOrcamentoUseCase(
                consultarPorOrcamentoUseCase,
                aprovarReparoAdicionalUseCase
        );
        when(consultarPorOrcamentoUseCase.execute(30L)).thenReturn(Optional.of(reparo));
        when(consultarPorOrcamentoUseCase.execute(31L)).thenReturn(Optional.empty());

        useCase.executeSeExistir(30L);
        useCase.executeSeExistir(31L);

        verify(aprovarReparoAdicionalUseCase).execute(40L);
    }

    @Test
    void aprovacaoObrigatoriaPorOrcamentoDeveFalharQuandoAusente() {
        var useCase = new AprovarReparoAdicionalPorOrcamentoUseCase(
                consultarPorOrcamentoUseCase,
                aprovarReparoAdicionalUseCase
        );
        when(consultarPorOrcamentoUseCase.execute(30L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.executeObrigatorio(30L));
        verifyNoInteractions(aprovarReparoAdicionalUseCase);
    }

    @Test
    void deveRecusarPorOrcamentoQuandoExistirEIgnorarQuandoAusente() {
        var reparo = reparoPendente();
        var useCase = new RecusarReparoAdicionalPorOrcamentoUseCase(
                consultarPorOrcamentoUseCase,
                recusarReparoAdicionalUseCase
        );
        when(consultarPorOrcamentoUseCase.execute(30L)).thenReturn(Optional.of(reparo));
        when(consultarPorOrcamentoUseCase.execute(31L)).thenReturn(Optional.empty());

        useCase.executeSeExistir(30L, "Cliente recusou");
        useCase.executeSeExistir(31L, "Cliente recusou");

        verify(recusarReparoAdicionalUseCase).execute(40L, "Cliente recusou");
    }

    private ReparoAdicionalEntity reparoPendente() {
        var servico = new ServicoSolicitadoEntity();
        servico.setServicoId(5L);
        servico.setNome("Troca de pastilha");
        servico.setValor(new BigDecimal("120.00"));
        servico.setStatus(StatusServicoOs.AGUARDANDO);
        servico.registrarItensNecessarios(java.util.List.of(ItemNecessarioEntity.criar(
                7L,
                "Pastilha",
                CategoriaPecaInsumo.PECA,
                new BigDecimal("15.00"),
                2,
                StatusItemNecessario.PENDENTE,
                new SituacaoEstoque(1, MotivoPendenciaItem.ESTOQUE_INSUFICIENTE)
        )));

        var reparo = ReparoAdicionalEntity.criar("OS-123", 20L, java.util.List.of(servico));
        reparo.setId(40L);
        reparo.setOrdemServicoId(10L);
        reparo.setOrcamentoId(30L);
        return reparo;
    }

    private OrdemServicoEntity ordemServico() {
        var ordemServico = new OrdemServicoEntity();
        ordemServico.setId(10L);
        ordemServico.setNumeroOs("OS-123");
        return ordemServico;
    }
}
