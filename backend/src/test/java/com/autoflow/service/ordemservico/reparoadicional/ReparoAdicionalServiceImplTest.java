package com.autoflow.service.ordemservico.reparoadicional;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.domain.ordemservico.reparoadicional.StatusReparoAdicional;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.repository.ordemservico.reparoadicional.ReparoAdicionalRepository;
import com.autoflow.service.orcamento.OrcamentoFactory;
import com.autoflow.service.orcamento.OrcamentoNotificacaoService;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import com.autoflow.service.orcamento.dto.PublicacaoOrcamentoResult;
import com.autoflow.service.ordemservico.OrdemServicoService;
import com.autoflow.service.ordemservico.reparoadicional.impl.CriarReparoAdicionalResult;
import com.autoflow.service.ordemservico.reparoadicional.impl.ReparoAdicionalServiceImpl;
import com.autoflow.service.pecainsumo.PecaInsumoService;
import com.autoflow.service.servico.ServicoService;
import com.autoflow.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReparoAdicionalServiceImplTest {

    @Mock
    ReparoAdicionalRepository reparoAdicionalRepository;

    @Mock
    OrdemServicoService ordemServicoService;

    @Mock
    UsuarioService usuarioService;

    @Mock
    OrcamentoFactory orcamentoFactory;

    @Mock
    OrdemServicoRepository ordemServicoRepository;

    @Mock
    OrcamentoVersioningService orcamentoVersioningService;

    @Mock
    OrcamentoRepository orcamentoRepository;

    @Mock
    OrcamentoPublicacaoService orcamentoPublicacaoService;

    @Mock
    OrcamentoNotificacaoService orcamentoNotificacaoService;

    @Mock
    ServicoService servicoService;

    @Mock
    PecaInsumoService pecaInsumoService;

    @InjectMocks
    ReparoAdicionalServiceImpl service;

    @Test
    void criar_deveCriarReparoGerarOrcamentoPublicarLinkERetornarResultado() {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setId(10L);
        ordemServico.setNumeroOs("OS-123");
        UsuarioEntity mecanico = new UsuarioEntity();
        mecanico.setId(20L);
        ServicoSolicitadoEntity servico = servico(1L, "Troca de pastilha", "120.00");
        servico.registrarItensNecessarios(List.of(item(7L, "Pastilha", 2)));
        OrcamentoEntity orcamento = new OrcamentoEntity();
        OrcamentoEntity orcamentoSalvo = new OrcamentoEntity();
        orcamentoSalvo.setId(30L);

        when(ordemServicoService.buscaOrdemServicoPorNumeroOs("OS-123")).thenReturn(ordemServico);
        when(usuarioService.buscarPorEmail("mecanico@autoflow.com")).thenReturn(mecanico);
        when(servicoService.buscarEntityPorId(1L)).thenReturn(servicoCatalogo(1L, "Troca de pastilha", "120.00"));
        when(pecaInsumoService.buscarEntityPorId(7L)).thenReturn(pecaInsumo(7L, "Pastilha", "15.00", 10));
        when(reparoAdicionalRepository.save(any(ReparoAdicionalEntity.class))).thenAnswer(invocation -> {
            ReparoAdicionalEntity reparo = invocation.getArgument(0);
            if (reparo.getId() == null) {
                reparo.setId(40L);
            }
            return reparo;
        });
        when(orcamentoVersioningService.proximaVersaoPrincipalNumeroOs("OS-123")).thenReturn(2);
        when(orcamentoFactory.criarAdicionalDisponivel(eq(ordemServico), any(ReparoAdicionalEntity.class), eq(2), any()))
                .thenReturn(orcamento);
        when(orcamentoRepository.save(orcamento)).thenReturn(orcamentoSalvo);
        when(orcamentoPublicacaoService.publicar(30L))
                .thenReturn(new PublicacaoOrcamentoResult(30L, "http://localhost:8080/public/orcamentos/30?token=abc"));

        CriarReparoAdicionalResult result = service.criar(
                "OS-123",
                "mecanico@autoflow.com",
                List.of(servico)
        );

        assertEquals(40L, result.reparoAdicionalId());
        assertEquals(30L, result.orcamentoId());
        assertEquals("http://localhost:8080/public/orcamentos/30?token=abc", result.publicUrl());

        ArgumentCaptor<ReparoAdicionalEntity> reparoCaptor = ArgumentCaptor.forClass(ReparoAdicionalEntity.class);
        verify(reparoAdicionalRepository, times(2)).save(reparoCaptor.capture());
        ReparoAdicionalEntity reparoPersistido = reparoCaptor.getAllValues().getLast();
        assertEquals("OS-123", reparoPersistido.getNumeroOs());
        assertEquals(20L, reparoPersistido.getMecanicoId());
        assertEquals(30L, reparoPersistido.getOrcamentoId());
        assertEquals(StatusReparoAdicional.PENDENTE_APROVACAO, reparoPersistido.getStatus());
        assertEquals(1, reparoPersistido.getServicos().size());
        assertSame(reparoPersistido, reparoPersistido.getServicos().getFirst().getReparoAdicional());
        assertNull(reparoPersistido.getServicos().getFirst().getOrdemServico());
        assertEquals("Troca de pastilha", reparoPersistido.getServicos().getFirst().getNome());
        verify(orcamentoVersioningService).proximaVersaoPrincipalNumeroOs("OS-123");
        verify(orcamentoFactory).criarAdicionalDisponivel(eq(ordemServico), any(ReparoAdicionalEntity.class), eq(2), any());
        verify(orcamentoPublicacaoService).publicar(30L);
    }

    @Test
    void criar_deveContinuarQuandoEnvioDeNotificacaoFalhar() {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setId(10L);
        ordemServico.setNumeroOs("OS-123");
        UsuarioEntity mecanico = new UsuarioEntity();
        mecanico.setId(20L);
        ServicoSolicitadoEntity servico = servico(1L, "Troca de pastilha", "120.00");
        servico.registrarItensNecessarios(List.of(item(7L, "Pastilha", 2)));
        OrcamentoEntity orcamento = new OrcamentoEntity();
        OrcamentoEntity orcamentoSalvo = new OrcamentoEntity();
        orcamentoSalvo.setId(30L);

        when(ordemServicoService.buscaOrdemServicoPorNumeroOs("OS-123")).thenReturn(ordemServico);
        when(usuarioService.buscarPorEmail("mecanico@autoflow.com")).thenReturn(mecanico);
        when(servicoService.buscarEntityPorId(1L)).thenReturn(servicoCatalogo(1L, "Troca de pastilha", "120.00"));
        when(pecaInsumoService.buscarEntityPorId(7L)).thenReturn(pecaInsumo(7L, "Pastilha", "15.00", 10));
        when(reparoAdicionalRepository.save(any(ReparoAdicionalEntity.class))).thenAnswer(invocation -> {
            ReparoAdicionalEntity reparo = invocation.getArgument(0);
            if (reparo.getId() == null) {
                reparo.setId(40L);
            }
            return reparo;
        });
        when(orcamentoVersioningService.proximaVersaoPrincipalNumeroOs("OS-123")).thenReturn(1);
        when(orcamentoFactory.criarAdicionalDisponivel(eq(ordemServico), any(ReparoAdicionalEntity.class), eq(1), any()))
                .thenReturn(orcamento);
        when(orcamentoRepository.save(orcamento)).thenReturn(orcamentoSalvo);
        when(orcamentoPublicacaoService.publicar(30L))
                .thenReturn(new PublicacaoOrcamentoResult(30L, "http://localhost:8080/public/orcamentos/30?token=abc"));
        doThrow(new RuntimeException("smtp indisponivel"))
                .when(orcamentoNotificacaoService)
                .enviarLinkOrcamentoParaCliente(
                        orcamentoSalvo,
                        ordemServico,
                        "http://localhost:8080/public/orcamentos/30?token=abc"
                );

        CriarReparoAdicionalResult result = service.criar(
                "OS-123",
                "mecanico@autoflow.com",
                List.of(servico)
        );

        assertEquals(40L, result.reparoAdicionalId());
        assertEquals(30L, result.orcamentoId());
        verify(reparoAdicionalRepository, times(2)).save(any(ReparoAdicionalEntity.class));
    }

    @Test
    void criar_deveLancarErroQuandoOsEstiverFinalizada() {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setId(10L);
        ordemServico.setNumeroOs("OS-123");
        ordemServico.setStatus(StatusOrdemServico.FINALIZADA);

        when(ordemServicoService.buscaOrdemServicoPorNumeroOs("OS-123")).thenReturn(ordemServico);
        List<ServicoSolicitadoEntity> servicosVazios = List.of();

        assertThrows(
                IllegalStateException.class,
                () -> service.criar("OS-123", "mecanico@autoflow.com", servicosVazios)
        );

        verify(reparoAdicionalRepository, never()).save(any());
        verifyNoInteractions(orcamentoRepository);
    }

    @Test
    void criar_deveLancarErroQuandoOsEstiverEntregue() {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setId(10L);
        ordemServico.setNumeroOs("OS-123");
        ordemServico.setStatus(StatusOrdemServico.ENTREGUE);

        when(ordemServicoService.buscaOrdemServicoPorNumeroOs("OS-123")).thenReturn(ordemServico);
        List<ServicoSolicitadoEntity> servicosVazios = List.of();

        assertThrows(
                IllegalStateException.class,
                () -> service.criar("OS-123", "mecanico@autoflow.com", servicosVazios)
        );

        verify(reparoAdicionalRepository, never()).save(any());
        verifyNoInteractions(orcamentoRepository);
    }

    @Test
    void criar_deveLancarErroQuandoNaoReceberServicos() {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setId(10L);
        ordemServico.setNumeroOs("OS-123");
        UsuarioEntity mecanico = new UsuarioEntity();
        mecanico.setId(20L);
        when(ordemServicoService.buscaOrdemServicoPorNumeroOs("OS-123")).thenReturn(ordemServico);
        when(usuarioService.buscarPorEmail("mecanico@autoflow.com")).thenReturn(mecanico);
        List<ServicoSolicitadoEntity> servicosVazios = List.of();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.criar("OS-123", "mecanico@autoflow.com", servicosVazios)
        );

        verify(reparoAdicionalRepository, never()).save(any());
        verifyNoInteractions(orcamentoRepository);
    }

    @Test
    void criar_deveLancarErroQuandoServicoNaoTemItemNecessario() {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setId(10L);
        ordemServico.setNumeroOs("OS-123");
        UsuarioEntity mecanico = new UsuarioEntity();
        mecanico.setId(20L);
        ServicoSolicitadoEntity servico = servico(1L, "Troca de pastilha", "120.00");

        when(ordemServicoService.buscaOrdemServicoPorNumeroOs("OS-123")).thenReturn(ordemServico);
        when(usuarioService.buscarPorEmail("mecanico@autoflow.com")).thenReturn(mecanico);
        when(servicoService.buscarEntityPorId(1L)).thenReturn(servicoCatalogo(1L, "Troca de pastilha", "120.00"));
        List<ServicoSolicitadoEntity> servicos = List.of(servico);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.criar("OS-123", "mecanico@autoflow.com", servicos)
        );

        verify(reparoAdicionalRepository, never()).save(any());
    }

    @Test
    void aprovar_deveAprovarReparoAdicionarServicosNaOsESalvar() {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setId(10L);
        ServicoSolicitadoEntity servicoDoReparo = servico(1L, "Troca de pastilha", "120.00");
        servicoDoReparo.setId(55L);
        servicoDoReparo.registrarItensNecessarios(List.of(item(7L, "Pastilha", 2)));
        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of(servicoDoReparo));
        reparo.setId(40L);

        when(reparoAdicionalRepository.findById(40L)).thenReturn(Optional.of(reparo));
        when(ordemServicoService.buscaOrdemServicoPorNumeroOs("OS-123")).thenReturn(ordemServico);
        when(reparoAdicionalRepository.save(reparo)).thenReturn(reparo);
        when(ordemServicoRepository.save(ordemServico)).thenReturn(ordemServico);

        OrdemServicoEntity result = service.aprovar(40L);

        assertSame(ordemServico, result);
        assertEquals(StatusReparoAdicional.APROVADO, reparo.getStatus());
        assertNotNull(reparo.getAprovadoEm());
        assertEquals(1, ordemServico.getServicosSolicitados().size());

        ServicoSolicitadoEntity servicoAdicionado = ordemServico.getServicosSolicitados().getFirst();
        assertNull(servicoAdicionado.getId());
        assertEquals(1L, servicoAdicionado.getServicoId());
        assertEquals("Troca de pastilha", servicoAdicionado.getNome());
        assertEquals(new BigDecimal("120.00"), servicoAdicionado.getValor());
        assertEquals(StatusServicoOs.AGUARDANDO, servicoAdicionado.getStatus());
        assertSame(ordemServico, servicoAdicionado.getOrdemServico());
        assertEquals(1, servicoAdicionado.getItensNecessarios().size());

        verify(reparoAdicionalRepository).save(reparo);
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    void aprovar_deveLancarErroQuandoReparoNaoExiste() {
        when(reparoAdicionalRepository.findById(40L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.aprovar(40L));
    }

    @Test
    void recusar_deveRecusarReparoESalvarMotivo() {
        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of(servico(1L, "Troca", "80.00")));
        reparo.setId(40L);
        when(reparoAdicionalRepository.findById(40L)).thenReturn(Optional.of(reparo));
        when(reparoAdicionalRepository.save(reparo)).thenReturn(reparo);

        ReparoAdicionalEntity result = service.recusar(40L, "Cliente recusou");

        assertSame(reparo, result);
        assertEquals(StatusReparoAdicional.RECUSADO, result.getStatus());
        assertEquals("Cliente recusou", result.getMotivoRecusa());
        assertNotNull(result.getRecusadoEm());
        verify(reparoAdicionalRepository).save(reparo);
    }

    @Test
    void recusar_deveLancarErroQuandoReparoNaoExiste() {
        when(reparoAdicionalRepository.findById(40L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.recusar(40L, "Cliente recusou"));
    }

    @Test
    void aprovarPorOrcamentoId_deveBuscarReparoPeloOrcamentoEAprovar() {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setId(10L);
        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of(servico(1L, "Troca", "80.00")));
        reparo.setId(40L);
        reparo.setOrcamentoId(30L);

        when(reparoAdicionalRepository.findByOrcamentoId(30L)).thenReturn(Optional.of(reparo));
        when(reparoAdicionalRepository.findById(40L)).thenReturn(Optional.of(reparo));
        when(ordemServicoService.buscaOrdemServicoPorNumeroOs("OS-123")).thenReturn(ordemServico);

        service.aprovarPorOrcamentoId(30L);

        assertEquals(StatusReparoAdicional.APROVADO, reparo.getStatus());
        assertEquals(1, ordemServico.getServicosSolicitados().size());
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    void aprovarPorOrcamentoId_deveLancarErroQuandoNaoEncontrarReparo() {
        when(reparoAdicionalRepository.findByOrcamentoId(30L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.aprovarPorOrcamentoId(30L));
    }

    @Test
    void aprovarSeExistirPorOrcamentoId_deveAprovarQuandoEncontrarReparo() {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setId(10L);
        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of(servico(1L, "Troca", "80.00")));
        reparo.setId(40L);
        reparo.setOrcamentoId(30L);

        when(reparoAdicionalRepository.findByOrcamentoId(30L)).thenReturn(Optional.of(reparo));
        when(reparoAdicionalRepository.findById(40L)).thenReturn(Optional.of(reparo));
        when(ordemServicoService.buscaOrdemServicoPorNumeroOs("OS-123")).thenReturn(ordemServico);

        service.aprovarSeExistirPorOrcamentoId(30L);

        assertEquals(StatusReparoAdicional.APROVADO, reparo.getStatus());
        assertEquals(1, ordemServico.getServicosSolicitados().size());
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    void aprovarSeExistirPorOrcamentoId_naoDeveFazerNadaQuandoNaoEncontrarReparo() {
        when(reparoAdicionalRepository.findByOrcamentoId(30L)).thenReturn(Optional.empty());

        service.aprovarSeExistirPorOrcamentoId(30L);

        verify(reparoAdicionalRepository).findByOrcamentoId(30L);
    }

    @Test
    void recusarSeExistirPorOrcamentoId_deveRecusarQuandoEncontrarReparo() {
        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of(servico(1L, "Troca", "80.00")));
        reparo.setId(40L);
        reparo.setOrcamentoId(30L);

        when(reparoAdicionalRepository.findByOrcamentoId(30L)).thenReturn(Optional.of(reparo));
        when(reparoAdicionalRepository.findById(40L)).thenReturn(Optional.of(reparo));
        when(reparoAdicionalRepository.save(reparo)).thenReturn(reparo);

        service.recusarSeExistirPorOrcamentoId(30L, "Cliente recusou adicional");

        assertEquals(StatusReparoAdicional.RECUSADO, reparo.getStatus());
        assertEquals("Cliente recusou adicional", reparo.getMotivoRecusa());
        assertNotNull(reparo.getRecusadoEm());
        verify(reparoAdicionalRepository).save(reparo);
    }

    @Test
    void existePorOrcamentoId_deveRetornarTrueQuandoEncontrarReparo() {
        ReparoAdicionalEntity reparo = new ReparoAdicionalEntity();
        when(reparoAdicionalRepository.findByOrcamentoId(30L)).thenReturn(Optional.of(reparo));

        assertTrue(service.existePorOrcamentoId(30L));
    }

    @Test
    void existePorOrcamentoId_deveRetornarFalseQuandoNaoEncontrarReparo() {
        when(reparoAdicionalRepository.findByOrcamentoId(30L)).thenReturn(Optional.empty());

        assertFalse(service.existePorOrcamentoId(30L));
    }

    @Test
    void buscaItensNecessarios_deveMarcarItemComoPendenteQuandoEstoqueInsuficiente() {
        ItemNecessarioEntity solicitado = item(7L, "Pastilha", 3);
        when(pecaInsumoService.buscarEntityPorId(7L)).thenReturn(pecaInsumo(7L, "Pastilha", "15.00", 1));

        List<ItemNecessarioEntity> resultado = service.buscaItensNecessarios(List.of(solicitado), pecaInsumoService);

        assertEquals(1, resultado.size());
        ItemNecessarioEntity item = resultado.getFirst();
        assertEquals(StatusItemNecessario.PENDENTE, item.getStatus());
        assertEquals(MotivoPendenciaItem.ESTOQUE_INSUFICIENTE, item.getMotivoPendencia());
        assertEquals(1, item.getQuantidadeDisponivel());
        assertEquals("Estoque insuficiente. Solicitado: 3, disponivel: 1.", item.getMensagemStatus());
    }

    private ServicoSolicitadoEntity servico(Long servicoId, String nome, String valor) {
        return new ServicoSolicitadoEntity(servicoId, nome, new BigDecimal(valor));
    }

    private ServicoEntity servicoCatalogo(Long servicoId, String nome, String valor) {
        ServicoEntity servico = new ServicoEntity();
        servico.setId(servicoId);
        servico.setNome(nome);
        servico.setValor(new BigDecimal(valor));
        return servico;
    }

    private ItemNecessarioEntity item(Long id, String nome, int quantidade) {
        return ItemNecessarioEntity.criar(
                id,
                nome,
                CategoriaPecaInsumo.PECA,
                new BigDecimal("15.00"),
                quantidade,
                null
        );
    }

    private PecaInsumoEntity pecaInsumo(Long id, String nome, String valor, int quantidade) {
        PecaInsumoEntity pecaInsumo = new PecaInsumoEntity();
        pecaInsumo.setId(id);
        pecaInsumo.setNome(nome);
        pecaInsumo.setValor(new BigDecimal(valor));
        pecaInsumo.setQuantidade(quantidade);
        pecaInsumo.setTipo(CategoriaPecaInsumo.PECA);
        return pecaInsumo;
    }
}
