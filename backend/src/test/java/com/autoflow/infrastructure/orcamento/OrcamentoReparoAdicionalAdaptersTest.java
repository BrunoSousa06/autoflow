package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import com.autoflow.service.orcamento.OrcamentoNotificacaoService;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoReparoAdicionalAdaptersTest {

    @Mock OrcamentoVersioningService versioningService;
    @Mock OrcamentoFactory factory;
    @Mock OrcamentoGateway orcamentoGateway;
    @Mock OrcamentoNotificacaoService notificacaoService;

    @Test
    void deveComporVersaoFactoryEPersistenciaDoOrcamentoComplementar() {
        var adapter = new OrcamentoComplementarAdapter(
                versioningService,
                factory,
                orcamentoGateway
        );
        var ordemServico = new OrdemServicoEntity();
        ordemServico.setNumeroOs("OS-123");
        var reparo = new ReparoAdicionalEntity();
        var criadoEm = LocalDateTime.of(2026, 8, 2, 12, 30);
        var orcamentoCriado = new OrcamentoEntity();
        var orcamentoSalvo = new OrcamentoEntity();

        when(versioningService.proximaVersaoPrincipalNumeroOs("OS-123")).thenReturn(2);
        when(factory.criarAdicionalDisponivel(ordemServico, reparo, 2, criadoEm))
                .thenReturn(orcamentoCriado);
        when(orcamentoGateway.save(orcamentoCriado)).thenReturn(orcamentoSalvo);

        var resultado = adapter.criarESalvar(ordemServico, reparo, criadoEm);

        assertSame(orcamentoSalvo, resultado);
        InOrder ordem = inOrder(versioningService, factory, orcamentoGateway);
        ordem.verify(versioningService).proximaVersaoPrincipalNumeroOs("OS-123");
        ordem.verify(factory).criarAdicionalDisponivel(ordemServico, reparo, 2, criadoEm);
        ordem.verify(orcamentoGateway).save(orcamentoCriado);
    }

    @Test
    void deveDelegarNotificacaoParaAbstracaoExistente() {
        var adapter = new OrcamentoNotificacaoAdapter(notificacaoService);
        var orcamento = new OrcamentoEntity();
        var ordemServico = new OrdemServicoEntity();

        adapter.notificar(orcamento, ordemServico, "https://publicacao/orcamento/30");

        verify(notificacaoService).enviarLinkOrcamentoParaCliente(
                orcamento,
                ordemServico,
                "https://publicacao/orcamento/30"
        );
    }
}
