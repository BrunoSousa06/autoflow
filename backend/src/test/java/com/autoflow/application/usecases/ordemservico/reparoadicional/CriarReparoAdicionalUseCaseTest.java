package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.*;
import com.autoflow.application.input.notificacao.OrcamentoNotificacao;
import com.autoflow.application.input.ordemservico.reparoadicional.CriarReparoAdicionalCommand;
import com.autoflow.application.input.ordemservico.reparoadicional.ItemReparoAdicionalCommand;
import com.autoflow.application.input.ordemservico.reparoadicional.ServicoReparoAdicionalCommand;
import com.autoflow.application.output.orcamento.OrcamentoPublicacao;
import com.autoflow.application.port.in.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase;
import com.autoflow.domain.orcamento.ClienteOrcamentoSnapshot;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.servico.Servico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarReparoAdicionalUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-02T15:30:00Z"),
            ZoneOffset.UTC
    );

    @Mock OrdemServicoGateway ordemServicoGateway;
    @Mock UsuarioGateway usuarioGateway;
    @Mock ServicoGateway servicoGateway;
    @Mock ConsultarDisponibilidadeEstoqueUseCase disponibilidadeEstoqueUseCase;
    @Mock ReparoAdicionalGateway reparoAdicionalGateway;
    @Mock OrcamentoComplementarGateway orcamentoComplementarGateway;
    @Mock OrcamentoPublicacaoGateway orcamentoPublicacaoGateway;
    @Mock OrcamentoNotificacaoGateway orcamentoNotificacaoGateway;

    private CriarReparoAdicionalUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CriarReparoAdicionalUseCaseImpl(
                ordemServicoGateway,
                usuarioGateway,
                servicoGateway,
                disponibilidadeEstoqueUseCase,
                reparoAdicionalGateway,
                orcamentoComplementarGateway,
                orcamentoPublicacaoGateway,
                orcamentoNotificacaoGateway,
                CLOCK
        );
    }

    @Test
    void deveCriarReparoComplementarPublicarNotificarEVincularOrcamento() {
        var ordemServico = ordemServico(StatusOrdemServico.EM_EXECUCAO);
        var mecanico = new Usuario();
        mecanico.setId(20L);
        mecanico.setRole(RoleEnum.MECANICO);
        var servicoCatalogo = servicoCatalogo(5L);
        var itemEnriquecido = itemEnriquecido(7L, 2);
        var orcamento = new Orcamento();
        orcamento.setId(30L);
        orcamento.setCliente(new ClienteOrcamentoSnapshot("Cliente", "123", "cliente@autoflow.com", null));

        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123")).thenReturn(Optional.of(ordemServico));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));
        when(servicoGateway.findById(5L)).thenReturn(Optional.of(servicoCatalogo));
        when(disponibilidadeEstoqueUseCase.execute(any())).thenReturn(List.of(itemEnriquecido));
        when(reparoAdicionalGateway.save(any())).thenAnswer(invocation -> {
            ReparoAdicional reparo = invocation.getArgument(0);
            reparo.setId(40L);
            return reparo;
        });
        when(orcamentoComplementarGateway.criarESalvar(
                any(),
                any(),
                any()
        )).thenReturn(orcamento);
        when(orcamentoPublicacaoGateway.publicarComLinks(30L))
                .thenReturn(new OrcamentoPublicacao("https://publicacao/orcamento/30", "https://publicacao/decisao/30"));

        var output = useCase.execute(command(5L, 7L, 2));

        assertEquals(40L, output.reparoAdicionalId());
        assertEquals(30L, output.orcamentoId());
        assertEquals("https://publicacao/orcamento/30", output.publicUrl());

        ArgumentCaptor<ReparoAdicional> reparoCaptor =
                ArgumentCaptor.forClass(ReparoAdicional.class);
        verify(reparoAdicionalGateway, times(2)).save(reparoCaptor.capture());
        ReparoAdicional reparo = reparoCaptor.getAllValues().getLast();
        assertEquals(10L, reparo.getOrdemServicoId());
        assertEquals(20L, reparo.getMecanicoId());
        assertEquals(30L, reparo.getOrcamentoId());
        assertEquals(1, reparo.getServicos().size());
        assertSame(itemEnriquecido, reparo.getServicos().getFirst().getItensNecessarios().getFirst());

        ArgumentCaptor<LocalDateTime> dataCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(orcamentoComplementarGateway).criarESalvar(
                any(),
                any(),
                dataCaptor.capture()
        );
        assertEquals(LocalDateTime.ofInstant(CLOCK.instant(), ZoneId.systemDefault()), dataCaptor.getValue());

        InOrder ordem = inOrder(
                reparoAdicionalGateway,
                orcamentoComplementarGateway,
                orcamentoPublicacaoGateway,
                orcamentoNotificacaoGateway
        );
        ordem.verify(reparoAdicionalGateway).save(any());
        ordem.verify(orcamentoComplementarGateway).criarESalvar(any(), any(), any());
        ordem.verify(orcamentoPublicacaoGateway).publicarComLinks(30L);
        ordem.verify(orcamentoNotificacaoGateway)
                .notificar(any(OrcamentoNotificacao.class));
        ordem.verify(reparoAdicionalGateway).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemServico.class, names = {"FINALIZADA", "ENTREGUE"})
    void deveRejeitarOrdemServicoEmEstadoFinal(StatusOrdemServico status) {
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123"))
                .thenReturn(Optional.of(ordemServico(status)));
        var command = command(5L, 7L, 2);

        assertThrows(IllegalStateException.class, () -> useCase.execute(command));

        verifyNoInteractions(usuarioGateway, servicoGateway, reparoAdicionalGateway);
    }

    @Test
    void deveRejeitarListaDeServicosVazia() {
        var command = new CriarReparoAdicionalCommand(
                "OS-123",
                "mecanico@autoflow.com",
                List.of()
        );

        var erro = assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));

        assertEquals("Reparo adicional deve ter ao menos um servico.", erro.getMessage());
        verifyNoInteractions(ordemServicoGateway);
    }

    @Test
    void deveValidarDadosObrigatoriosDoCommandSemAcessarGateways() {
        var semNumeroOs = new CriarReparoAdicionalCommand(
                " ",
                "mecanico@autoflow.com",
                command(5L, 7L, 2).servicos()
        );
        var semEmail = new CriarReparoAdicionalCommand(
                "OS-123",
                " ",
                command(5L, 7L, 2).servicos()
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(semNumeroOs));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(semEmail));
        verifyNoInteractions(ordemServicoGateway);
    }

    @Test
    void deveRejeitarCommandENovosCamposObrigatoriosNulos() {
        CriarReparoAdicionalCommand commandValido = command(5L, 7L, 2);
        CriarReparoAdicionalCommand semNumero = new CriarReparoAdicionalCommand(
                null, "mecanico@autoflow.com", commandValido.servicos());
        CriarReparoAdicionalCommand semEmail = new CriarReparoAdicionalCommand(
                "OS-123", null, commandValido.servicos());
        CriarReparoAdicionalCommand semServicos = new CriarReparoAdicionalCommand(
                "OS-123", "mecanico@autoflow.com", null);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> useCase.execute(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> useCase.execute(semNumero)),
                () -> assertThrows(IllegalArgumentException.class, () -> useCase.execute(semEmail)),
                () -> assertThrows(IllegalArgumentException.class, () -> useCase.execute(semServicos))
        );

        verifyNoInteractions(ordemServicoGateway);
    }

    @Test
    void deveRejeitarServicoDuplicadoNaPropriaRequisicao() {
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123"))
                .thenReturn(Optional.of(ordemServico(StatusOrdemServico.EM_EXECUCAO)));
        var servico = new ServicoReparoAdicionalCommand(
                5L,
                List.of(new ItemReparoAdicionalCommand(7L, 2))
        );
        var command = new CriarReparoAdicionalCommand(
                "OS-123",
                "mecanico@autoflow.com",
                List.of(servico, servico)
        );

        var erro = assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));

        assertTrue(erro.getMessage().contains("Serviço duplicado"));
        verifyNoInteractions(usuarioGateway, reparoAdicionalGateway);
    }

    @Test
    void deveRejeitarServicoNuloOuSemId() {
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123"))
                .thenReturn(Optional.of(ordemServico(StatusOrdemServico.EM_EXECUCAO)));

        CriarReparoAdicionalCommand servicoNulo = new CriarReparoAdicionalCommand(
                "OS-123", "mecanico@autoflow.com",
                Collections.singletonList((ServicoReparoAdicionalCommand) null));
        CriarReparoAdicionalCommand servicoSemId = new CriarReparoAdicionalCommand(
                "OS-123", "mecanico@autoflow.com",
                List.of(new ServicoReparoAdicionalCommand(null, List.of())));

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> useCase.execute(servicoNulo)),
                () -> assertThrows(IllegalArgumentException.class, () -> useCase.execute(servicoSemId))
        );
        verifyNoInteractions(usuarioGateway, reparoAdicionalGateway);
    }

    @Test
    void deveRejeitarServicoJaPresenteNaOrdemServico() {
        var ordemServico = ordemServico(StatusOrdemServico.EM_EXECUCAO);
        ordemServico.adicionarServicosSolicitados(List.of(new ServicoSolicitado(5L)));
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123")).thenReturn(Optional.of(ordemServico));
        var command = command(5L, 7L, 2);

        var erro = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(command)
        );

        assertTrue(erro.getMessage().contains("já incluído na ordem de serviço"));
        verifyNoInteractions(usuarioGateway, reparoAdicionalGateway);
    }

    @Test
    void deveRejeitarMecanicoNaoAtribuido() {
        var ordemServico = ordemServico(StatusOrdemServico.EM_EXECUCAO);
        var mecanico = new Usuario();
        mecanico.setId(21L);
        mecanico.setRole(RoleEnum.MECANICO);
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123")).thenReturn(Optional.of(ordemServico));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));
        var command = command(5L, 7L, 2);

        var erro = assertThrows(ApplicationException.class,
                () -> useCase.execute(command));

        assertEquals(ApplicationException.ErrorType.FORBIDDEN, erro.type());
        assertEquals("Somente o mecânico atribuído pode criar reparo adicional.", erro.getMessage());
        assertEquals(20L, ordemServico.getDiagnostico().getMecanico().getId());
        assertEquals(21L, mecanico.getId());
        verifyNoInteractions(servicoGateway, disponibilidadeEstoqueUseCase, reparoAdicionalGateway);
    }

    @Test
    void devePermitirAdministradorSemMecanicoAtribuido() {
        var ordemServico = ordemServico(StatusOrdemServico.EM_EXECUCAO);
        ordemServico.setDiagnostico(null);
        var admin = new Usuario();
        admin.setId(1L);
        admin.setRole(RoleEnum.ADMIN);
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123")).thenReturn(Optional.of(ordemServico));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(admin));
        when(servicoGateway.findById(5L)).thenReturn(Optional.of(servicoCatalogo(5L)));
        when(disponibilidadeEstoqueUseCase.execute(any())).thenReturn(List.of(itemEnriquecido(7L, 2)));

        var orcamento = new Orcamento();
        orcamento.setId(30L);
        orcamento.setCliente(new ClienteOrcamentoSnapshot("Cliente", "123", "cliente@autoflow.com", null));
        when(reparoAdicionalGateway.save(any())).thenAnswer(invocation -> {
            ReparoAdicional reparo = invocation.getArgument(0);
            reparo.setId(40L);
            return reparo;
        });
        when(orcamentoComplementarGateway.criarESalvar(any(), any(), any())).thenReturn(orcamento);
        when(orcamentoPublicacaoGateway.publicarComLinks(30L))
                .thenReturn(new OrcamentoPublicacao("https://publicacao/orcamento/30", "https://publicacao/decisao/30"));

        assertEquals(30L, useCase.execute(command(5L, 7L, 2)).orcamentoId());
    }

    @Test
    void deveRejeitarUsuarioQueNaoPossuiPapelPermitido() {
        var ordemServico = ordemServico(StatusOrdemServico.EM_EXECUCAO);
        var usuario = new Usuario();
        usuario.setId(30L);
        usuario.setRole(RoleEnum.ATENDENTE);
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123")).thenReturn(Optional.of(ordemServico));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(usuario));
        var command = command(5L, 7L, 2);

        var erro = assertThrows(ApplicationException.class,
                () -> useCase.execute(command));

        assertEquals(ApplicationException.ErrorType.FORBIDDEN, erro.type());
        verifyNoInteractions(servicoGateway, disponibilidadeEstoqueUseCase, reparoAdicionalGateway);
    }

    @Test
    void deveRejeitarMecanicoQuandoOsNaoPossuirAtribuicao() {
        var ordemServico = ordemServico(StatusOrdemServico.EM_EXECUCAO);
        ordemServico.setDiagnostico(null);
        var mecanico = new Usuario();
        mecanico.setId(20L);
        mecanico.setRole(RoleEnum.MECANICO);
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123")).thenReturn(Optional.of(ordemServico));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));
        var command = command(5L, 7L, 2);

        var erro = assertThrows(ApplicationException.class,
                () -> useCase.execute(command));

        assertEquals(ApplicationException.ErrorType.BAD_REQUEST, erro.type());
        verifyNoInteractions(servicoGateway, disponibilidadeEstoqueUseCase, reparoAdicionalGateway);
    }

    @Test
    void deveRejeitarMecanicoQuandoDiagnosticoNaoPossuirMecanico() {
        var ordemServico = ordemServico(StatusOrdemServico.EM_EXECUCAO);
        ordemServico.setDiagnostico(new Diagnostico());
        var mecanico = new Usuario();
        mecanico.setId(20L);
        mecanico.setRole(RoleEnum.MECANICO);
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123"))
                .thenReturn(Optional.of(ordemServico));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));

        ApplicationException erro = assertThrows(ApplicationException.class,
                () -> useCase.execute(command(5L, 7L, 2)));

        assertEquals(ApplicationException.ErrorType.BAD_REQUEST, erro.type());
        verifyNoInteractions(servicoGateway, disponibilidadeEstoqueUseCase, reparoAdicionalGateway);
    }

    @Test
    void deveRejeitarPecaRepetidaEntreServicosDoMesmoReparo() {
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123"))
                .thenReturn(Optional.of(ordemServico(StatusOrdemServico.EM_EXECUCAO)));
        var servico1 = new ServicoReparoAdicionalCommand(5L,
                List.of(new ItemReparoAdicionalCommand(7L, 1)));
        var servico2 = new ServicoReparoAdicionalCommand(6L,
                List.of(new ItemReparoAdicionalCommand(7L, 1)));

        var command = new CriarReparoAdicionalCommand(
                "OS-123", "mecanico@autoflow.com", List.of(servico1, servico2));

        var erro = assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));

        assertTrue(erro.getMessage().contains("Peça/Insumo duplicado"));
        verifyNoInteractions(usuarioGateway, servicoGateway, reparoAdicionalGateway);
    }

    @Test
    void deveRejeitarServicoSemItens() {
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123"))
                .thenReturn(Optional.of(ordemServico(StatusOrdemServico.EM_EXECUCAO)));
        var mecanico = new Usuario();
        mecanico.setId(20L);
        mecanico.setRole(RoleEnum.MECANICO);
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));
        var command = new CriarReparoAdicionalCommand(
                "OS-123",
                "mecanico@autoflow.com",
                List.of(new ServicoReparoAdicionalCommand(5L, List.of()))
        );

        var erro = assertThrows(IllegalArgumentException.class, () -> useCase.execute(command));

        assertEquals("Servico do reparo adicional deve ter ao menos um item necessario.", erro.getMessage());
        verifyNoInteractions(reparoAdicionalGateway);
    }

    @Test
    void deveRejeitarQuantidadeDeItemNaoPositiva() {
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123"))
                .thenReturn(Optional.of(ordemServico(StatusOrdemServico.EM_EXECUCAO)));
        var mecanico = new Usuario();
        mecanico.setId(20L);
        mecanico.setRole(RoleEnum.MECANICO);
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));
        when(servicoGateway.findById(5L)).thenReturn(Optional.of(servicoCatalogo(5L)));
        var command = command(5L, 7L, 0);

        var erro = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(command)
        );

        assertEquals("Quantidade do item deve ser maior que zero.", erro.getMessage());
        verifyNoInteractions(disponibilidadeEstoqueUseCase, reparoAdicionalGateway);
    }

    @Test
    void deveRejeitarItensNulosOuIncompletos() {
        var ordemServico = ordemServico(StatusOrdemServico.EM_EXECUCAO);
        var mecanico = new Usuario();
        mecanico.setId(20L);
        mecanico.setRole(RoleEnum.MECANICO);
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123"))
                .thenReturn(Optional.of(ordemServico));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));
        when(servicoGateway.findById(5L)).thenReturn(Optional.of(servicoCatalogo(5L)));

        List<ItemReparoAdicionalCommand> itemNulo =
                Collections.singletonList((ItemReparoAdicionalCommand) null);
        List<ItemReparoAdicionalCommand> pecaNula =
                List.of(new ItemReparoAdicionalCommand(null, 1));
        List<ItemReparoAdicionalCommand> quantidadeNula =
                List.of(new ItemReparoAdicionalCommand(7L, null));
        CriarReparoAdicionalCommand itensNulos = new CriarReparoAdicionalCommand(
                "OS-123", "mecanico@autoflow.com",
                List.of(new ServicoReparoAdicionalCommand(5L, null)));
        CriarReparoAdicionalCommand itemNuloCommand = new CriarReparoAdicionalCommand(
                "OS-123", "mecanico@autoflow.com",
                List.of(new ServicoReparoAdicionalCommand(5L, itemNulo)));
        CriarReparoAdicionalCommand pecaNulaCommand = new CriarReparoAdicionalCommand(
                "OS-123", "mecanico@autoflow.com",
                List.of(new ServicoReparoAdicionalCommand(5L, pecaNula)));
        CriarReparoAdicionalCommand quantidadeNulaCommand = new CriarReparoAdicionalCommand(
                "OS-123", "mecanico@autoflow.com",
                List.of(new ServicoReparoAdicionalCommand(5L, quantidadeNula)));

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> useCase.execute(itensNulos)),
                () -> assertThrows(IllegalArgumentException.class, () -> useCase.execute(itemNuloCommand)),
                () -> assertThrows(IllegalArgumentException.class, () -> useCase.execute(pecaNulaCommand)),
                () -> assertThrows(IllegalArgumentException.class, () -> useCase.execute(quantidadeNulaCommand))
        );
        verifyNoInteractions(disponibilidadeEstoqueUseCase, reparoAdicionalGateway);
    }

    @Test
    void deveInformarOrdemServicoMecanicoEServicoInexistentes() {
        var command = command(5L, 7L, 2);
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123")).thenReturn(Optional.empty());
        var erroOs = assertThrows(ApplicationException.class, () -> useCase.execute(command));
        assertEquals(ApplicationException.ErrorType.NOT_FOUND, erroOs.type());

        var ordemServico = ordemServico(StatusOrdemServico.EM_EXECUCAO);
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123")).thenReturn(Optional.of(ordemServico));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.empty());
        var erroUsuario = assertThrows(ApplicationException.class, () -> useCase.execute(command));
        assertEquals(ApplicationException.ErrorType.NOT_FOUND, erroUsuario.type());

        var mecanico = new Usuario();
        mecanico.setId(20L);
        mecanico.setRole(RoleEnum.MECANICO);
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));
        when(servicoGateway.findById(5L)).thenReturn(Optional.empty());
        var erroServico = assertThrows(ApplicationException.class, () -> useCase.execute(command));
        assertEquals(ApplicationException.ErrorType.NOT_FOUND, erroServico.type());
    }

    @Test
    void falhaDeNotificacaoNaoDeveDesfazerCriacao() {
        var ordemServico = ordemServico(StatusOrdemServico.EM_EXECUCAO);
        var mecanico = new Usuario();
        mecanico.setId(20L);
        mecanico.setRole(RoleEnum.MECANICO);
        var orcamento = new Orcamento();
        orcamento.setId(30L);
        orcamento.setCliente(new ClienteOrcamentoSnapshot("Cliente", "123", "cliente@autoflow.com", null));
        when(ordemServicoGateway.findByNumeroOsForUpdate("OS-123")).thenReturn(Optional.of(ordemServico));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));
        when(servicoGateway.findById(5L)).thenReturn(Optional.of(servicoCatalogo(5L)));
        when(disponibilidadeEstoqueUseCase.execute(any())).thenReturn(List.of(itemEnriquecido(7L, 2)));
        when(reparoAdicionalGateway.save(any())).thenAnswer(invocation -> {
            ReparoAdicional reparo = invocation.getArgument(0);
            reparo.setId(40L);
            return reparo;
        });
        when(orcamentoComplementarGateway.criarESalvar(any(), any(), any())).thenReturn(orcamento);
        when(orcamentoPublicacaoGateway.publicarComLinks(30L))
                .thenReturn(new OrcamentoPublicacao("https://publicacao/orcamento/30", "https://publicacao/decisao/30"));
        doThrow(new RuntimeException("smtp indisponivel"))
                .when(orcamentoNotificacaoGateway)
                .notificar(any(OrcamentoNotificacao.class));

        var output = useCase.execute(command(5L, 7L, 2));

        assertEquals(30L, output.orcamentoId());
        verify(reparoAdicionalGateway, times(2)).save(any());
    }

    private CriarReparoAdicionalCommand command(Long servicoId, Long itemId, int quantidade) {
        return new CriarReparoAdicionalCommand(
                "OS-123",
                "mecanico@autoflow.com",
                List.of(new ServicoReparoAdicionalCommand(
                        servicoId,
                        List.of(new ItemReparoAdicionalCommand(itemId, quantidade))
                ))
        );
    }

    private OrdemServico ordemServico(StatusOrdemServico status) {
        var ordemServico = new OrdemServico();
        ordemServico.setId(10L);
        ordemServico.setNumeroOs("OS-123");
        ordemServico.setStatus(status);
        var diagnostico = new Diagnostico();
        var mecanicoAtribuido = new Usuario();
        mecanicoAtribuido.setId(20L);
        diagnostico.setMecanico(mecanicoAtribuido);
        ordemServico.setDiagnostico(diagnostico);
        return ordemServico;
    }

    private Servico servicoCatalogo(Long id) {
        return Servico.reconstituir(id, "Troca de pastilha", "Descricao", new BigDecimal("120.00"), true);
    }

    private ItemNecessario itemEnriquecido(Long id, int quantidade) {
        return ItemNecessario.criar(
                id,
                "Pastilha",
                CategoriaPecaInsumo.PECA,
                new BigDecimal("15.00"),
                quantidade,
                StatusItemNecessario.DISPONIVEL
        );
    }
}
