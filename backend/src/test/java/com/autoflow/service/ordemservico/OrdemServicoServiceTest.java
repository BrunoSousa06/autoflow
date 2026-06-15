package com.autoflow.service.ordemservico;

import com.autoflow.controller.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.controller.ordemservico.response.TempoMedioOrdemServicoResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.repository.ordemservico.TempoMedioOrdemServicoProjection;
import com.autoflow.repository.ordemservico.historico.HistoricoStatusOsRepository;
import com.autoflow.service.cliente.ClienteService;
import com.autoflow.service.orcamento.OrcamentoFactory;
import com.autoflow.service.orcamento.OrcamentoNotificacaoService;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import com.autoflow.service.orcamento.dto.PublicacaoOrcamentoResult;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import com.autoflow.service.ordemservico.dto.OrdemServicoFiltro;
import com.autoflow.service.ordemservico.impl.OrdemServicoAccessPolicy;
import com.autoflow.service.ordemservico.impl.OrdemServicoServiceImpl;
import com.autoflow.service.pecainsumo.BaixaEstoqueResult;
import com.autoflow.service.pecainsumo.PecaInsumoService;
import com.autoflow.service.servico.ServicoService;
import com.autoflow.service.usuario.UsuarioService;
import com.autoflow.service.veiculo.VeiculoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @InjectMocks
    OrdemServicoServiceImpl service;

    @Mock
    OrdemServicoRepository repository;
    @Mock
    VeiculoService veiculoService;
    @Mock
    ServicoService servicoService;
    @Mock
    UsuarioService usuarioService;
    @Mock
    PecaInsumoService pecaInsumoService;
    @Mock
    OrdemServicoAccessPolicy ordemServicoAccessPolicy;
    @Mock
    OrcamentoFactory orcamentoFactoryImpl;
    @Mock
    OrcamentoVersioningService orcamentoVersioningServiceImpl;
    @Mock
    OrcamentoRepository orcamentoRepository;
    @Mock
    OrcamentoPublicacaoService orcamentoPublicacaoServiceImpl;
    @Mock
    ClienteService clienteService;
    @Mock
    ClienteRepository clienteRepository;
    @Mock
    HistoricoStatusOsRepository historicoStatusOsRepository;
    @Mock
    OrcamentoNotificacaoService orcamentoNotificacaoService;

    @Test
    void deveCriarOrdemServicoComServicosVinculados() {
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        VeiculoOrdemServicoRequest veiculoRequest = criarVeiculoRequestCompleto();
        ServicoSolicitadoEntity solicitado = new ServicoSolicitadoEntity(10L);
        ServicoEntity servicoCatalogo = criarServico(10L, "Revisao", new BigDecimal("100.00"));

        when(clienteService.buscarPorCpfCnpj("12345678901")).thenReturn(cliente);
        when(veiculoService.buscarOuCadastrarPorPlacaParaCliente(cliente, veiculoRequest)).thenReturn(veiculo);
        when(servicoService.buscarEntityPorId(10L)).thenReturn(servicoCatalogo);
        when(repository.save(any(OrdemServicoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoEntity resultado = service.criar("12345678901", veiculoRequest, List.of(solicitado));

        assertEquals(StatusOrdemServico.RECEBIDA, resultado.getStatus());
        assertNotNull(resultado.getUltimaAtualizacao());
        assertEquals("12345678901", resultado.getCliente().getCpfCnpj());
        assertEquals(veiculo, resultado.getVeiculo());
        assertEquals(1, resultado.getServicosSolicitados().size());
        ServicoSolicitadoEntity servicoOs = resultado.getServicosSolicitados().getFirst();
        assertEquals(10L, servicoOs.getServicoId());
        assertEquals("Revisao", servicoOs.getNome());
        assertEquals(new BigDecimal("100.00"), servicoOs.getValor());
        assertEquals(StatusServicoOs.AGUARDANDO, servicoOs.getStatus());
        assertEquals(resultado, servicoOs.getOrdemServico());
        verify(historicoStatusOsRepository).save(argThat(historico ->
                StatusOrdemServico.RECEBIDA.equals(historico.getStatus())
                        && historico.getMensagemCliente() != null
        ));
    }

    @Test
    void deveLancarIllegalArgumentQuandoCriarSemServicos() {
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        VeiculoOrdemServicoRequest veiculoRequest = criarVeiculoRequestCompleto();
        when(clienteService.buscarPorCpfCnpj("12345678901")).thenReturn(cliente);
        when(veiculoService.buscarOuCadastrarPorPlacaParaCliente(cliente, veiculoRequest)).thenReturn(veiculo);

        assertThrows(IllegalArgumentException.class, () -> service.criar("12345678901", veiculoRequest, null));
        verify(repository, never()).save(any());
    }

    @Test
    void devePropagarNotFoundQuandoClienteNaoExistirAoCriarOrdemServico() {
        ResponseStatusException erro = new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado");
        VeiculoOrdemServicoRequest veiculoRequest = criarVeiculoRequestCompleto();
        List<ServicoSolicitadoEntity> servicosSolicitados = List.of(new ServicoSolicitadoEntity(10L));
        when(clienteService.buscarPorCpfCnpj("12345678901")).thenThrow(erro);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar("12345678901", veiculoRequest, servicosSolicitados)
        );

        assertSame(erro, exception);
        verifyNoInteractions(veiculoService, servicoService, repository);
    }

    @Test
    void devePropagarConflictQuandoPlacaPertencerAOutroCliente() {
        ClienteEntity cliente = criarCliente(1L);
        VeiculoOrdemServicoRequest veiculoRequest = criarVeiculoRequestCompleto();
        ResponseStatusException erro = new ResponseStatusException(HttpStatus.CONFLICT, "Placa ja cadastrada para outro cliente.");

        when(clienteService.buscarPorCpfCnpj("12345678901")).thenReturn(cliente);
        when(veiculoService.buscarOuCadastrarPorPlacaParaCliente(cliente, veiculoRequest)).thenThrow(erro);
        List<ServicoSolicitadoEntity> servicosSolicitados = List.of(new ServicoSolicitadoEntity(10L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar("12345678901", veiculoRequest, servicosSolicitados)
        );

        assertSame(erro, exception);
        verifyNoInteractions(servicoService, repository);
    }

    @Test
    void deveIncluirServicosNaOrdemServico() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        ServicoSolicitadoEntity solicitado = new ServicoSolicitadoEntity(20L);
        ServicoEntity servicoCatalogo = criarServico(20L, "Troca oleo", new BigDecimal("80.00"));

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(servicoService.buscarEntityPorId(20L)).thenReturn(servicoCatalogo);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.incluirServicos("OS-123", List.of(solicitado));

        assertEquals(2, resultado.getServicosSolicitados().size());
        ServicoSolicitadoEntity servicoIncluido = resultado.getServicosSolicitados().get(1);
        assertEquals(20L, servicoIncluido.getServicoId());
        assertEquals("Troca oleo", servicoIncluido.getNome());
        assertEquals(os, servicoIncluido.getOrdemServico());
    }

    @Test
    void deveLancarIllegalArgumentQuandoIncluirServicosComListaVazia() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        List<ServicoSolicitadoEntity> servicosVazios = List.of();
        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));

        assertThrows(IllegalArgumentException.class, () -> service.incluirServicos("OS-123", servicosVazios));
        verify(repository, never()).save(any());
    }

    @Test
    void deveAtribuirMecanicoCriandoDiagnosticoQuandoNaoExistir() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(usuarioService.buscarMecanicoPorId(2L)).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.atribuirMecanico("OS-123", 2L, null);

        assertNotNull(resultado.getDiagnostico());
        assertEquals(mecanico, resultado.getDiagnostico().getMecanico());
        verify(usuarioService).buscarMecanicoPorId(2L);
        verify(usuarioService, never()).buscarPorEmail(anyString());
    }

    @Test
    void deveAtribuirMecanicoMantendoDiagnosticoExistente() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        os.setDiagnostico(diagnostico);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(usuarioService.buscarMecanicoPorId(2L)).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.atribuirMecanico("OS-123", 2L, null);

        assertSame(diagnostico, resultado.getDiagnostico());
        assertEquals(mecanico, resultado.getDiagnostico().getMecanico());
    }

    @Test
    void deveAtribuirMecanicoPorEmailQuandoIdNaoForInformado() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail("mecanico@autoflow.com")).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.atribuirMecanico("OS-123", null, "mecanico@autoflow.com");

        assertNotNull(resultado.getDiagnostico());
        assertEquals(mecanico, resultado.getDiagnostico().getMecanico());
        verify(usuarioService, never()).buscarMecanicoPorId(anyLong());
        verify(usuarioService).buscarPorEmail("mecanico@autoflow.com");
    }

    @Test
    void devePriorizarIdQuandoIdEEmailForemInformados() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(usuarioService.buscarMecanicoPorId(2L)).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.atribuirMecanico("OS-123", 2L, "outro@autoflow.com");

        assertEquals(mecanico, resultado.getDiagnostico().getMecanico());
        verify(usuarioService).buscarMecanicoPorId(2L);
        verify(usuarioService, never()).buscarPorEmail(anyString());
    }

    @Test
    void deveLancarBadRequestQuandoNaoInformarIdentificadorDoMecanico() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.atribuirMecanico("OS-123", null, " ")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioService, never()).buscarMecanicoPorId(anyLong());
        verify(usuarioService, never()).buscarPorEmail(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarBadRequestQuandoEmailNaoForDeMecanico() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        UsuarioEntity atendente = criarUsuario(3L, "Atendente", "atendente@autoflow.com", RoleEnum.ATENDENTE);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail("atendente@autoflow.com")).thenReturn(atendente);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.atribuirMecanico("OS-123", null, "atendente@autoflow.com")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void deveIniciarDiagnosticoComoAdminSemValidarMecanicoAtribuido() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        os.setDiagnostico(new DiagnosticoEntity());
        UsuarioEntity admin = criarUsuario(1L, "Admin", "admin@autoflow.com", RoleEnum.ADMIN);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail("admin@autoflow.com")).thenReturn(admin);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.iniciarDiagnostico("OS-123", "admin@autoflow.com");

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, resultado.getStatus());
        assertNotNull(resultado.getDiagnostico().getIniciadoEm());
        assertNotNull(resultado.getUltimaAtualizacao());
        verify(ordemServicoAccessPolicy, never()).validarPodeAlterarDiagnostico(any(), any());
        verify(historicoStatusOsRepository).save(argThat(historico ->
                StatusOrdemServico.EM_DIAGNOSTICO.equals(historico.getStatus())
        ));
    }

    @Test
    void deveIniciarDiagnosticoComoMecanicoValidandoPermissao() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        os.setDiagnostico(new DiagnosticoEntity());
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail("mecanico@autoflow.com")).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.iniciarDiagnostico("OS-123", "mecanico@autoflow.com");

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, resultado.getStatus());
        verify(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
    }

    @Test
    void deveRegistrarLaudo() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        os.setDiagnostico(new DiagnosticoEntity());
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail("mecanico@autoflow.com")).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.registrarLaudo("OS-123", "mecanico@autoflow.com", "Laudo");

        assertEquals("Laudo", resultado.getDiagnostico().getLaudo());
        verify(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
    }

    @Test
    void deveRegistrarItensNecessariosNoServicoDaOs() {
        String numeroOs = "abc-123";
        Long servicoOsId = 55L;
        Long pecaInsumoId = 10L;
        String emailAdmin = "admin@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        UsuarioEntity admin = criarUsuario(1L, "Admin", emailAdmin, RoleEnum.ADMIN);
        PecaInsumoEntity estoque = criarPecaInsumo(pecaInsumoId, "Filtro", CategoriaPecaInsumo.PECA, new BigDecimal("50.00"), 5);

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailAdmin)).thenReturn(admin);
        when(pecaInsumoService.buscarEntityPorId(pecaInsumoId)).thenReturn(estoque);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.registrarItemNecessario(numeroOs,
                servicoOsId,
                emailAdmin,
                List.of(criarItemNecessarioSolicitado(pecaInsumoId, 2)));

        ServicoSolicitadoEntity servico = resultado.buscarServicoSolicitado(servicoOsId);
        assertEquals(1, servico.getItensNecessarios().size());
        ItemNecessarioEntity item = servico.getItensNecessarios().getFirst();
        assertEquals("Filtro", item.getNome());
        assertEquals(StatusItemNecessario.DISPONIVEL, item.getStatus());
        assertEquals(5, item.getQuantidadeDisponivel());
        assertNull(item.getMotivoPendencia());
        assertNull(item.getMensagemStatus());
        verify(repository).save(os);
    }

    @Test
    void deveRegistrarItemNecessarioPendenteComMotivoQuandoEstoqueForInsuficiente() {
        String numeroOs = "abc-123";
        Long servicoOsId = 55L;
        Long pecaInsumoId = 10L;
        String emailAdmin = "admin@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        UsuarioEntity admin = criarUsuario(1L, "Admin", emailAdmin, RoleEnum.ADMIN);
        PecaInsumoEntity estoque = criarPecaInsumo(pecaInsumoId, "Filtro", CategoriaPecaInsumo.PECA, new BigDecimal("50.00"), 1);

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailAdmin)).thenReturn(admin);
        when(pecaInsumoService.buscarEntityPorId(pecaInsumoId)).thenReturn(estoque);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.registrarItemNecessario(numeroOs,
                servicoOsId,
                emailAdmin,
                List.of(criarItemNecessarioSolicitado(pecaInsumoId, 2))
        );

        ItemNecessarioEntity item = resultado.buscarServicoSolicitado(servicoOsId)
                .getItensNecessarios()
                .getFirst();
        assertAll(
                () -> assertEquals(StatusItemNecessario.PENDENTE, item.getStatus()),
                () -> assertEquals(MotivoPendenciaItem.ESTOQUE_INSUFICIENTE, item.getMotivoPendencia()),
                () -> assertEquals(1, item.getQuantidadeDisponivel()),
                () -> assertEquals(
                        "Estoque insuficiente. Solicitado: 2, disponivel: 1.",
                        item.getMensagemStatus()
                )
        );
        verify(repository).save(os);
    }

    @Test
    void deveLancarExcecaoAoRegistrarItemNecessarioQuandoOsNaoEstaEmDiagnostico() {
        String numeroOs = "abc-123";
        Long servicoOsId = 55L;
        String emailAdmin = "admin@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        UsuarioEntity admin = criarUsuario(1L, "Admin", emailAdmin, RoleEnum.ADMIN);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailAdmin)).thenReturn(admin);
        List<ItemNecessarioEntity> itensVazios = List.of();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.registrarItemNecessario(numeroOs, servicoOsId, emailAdmin, itensVazios));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void deveChamarValidacaoDeAcessoAoRegistrarItemNecessarioComUsuarioNaoAdmin() {
        String numeroOs = "abc-123";
        Long servicoOsId = 55L;
        Long pecaInsumoId = 10L;
        String emailMecanico = "mecanico@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", emailMecanico, RoleEnum.MECANICO);
        PecaInsumoEntity estoque = criarPecaInsumo(pecaInsumoId, "Filtro", CategoriaPecaInsumo.PECA, new BigDecimal("50.00"), 5);

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailMecanico)).thenReturn(mecanico);
        when(pecaInsumoService.buscarEntityPorId(pecaInsumoId)).thenReturn(estoque);
        when(repository.save(os)).thenReturn(os);

        service.registrarItemNecessario(numeroOs, servicoOsId, emailMecanico,
                List.of(criarItemNecessarioSolicitado(pecaInsumoId, 1)));

        verify(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
    }

    @Test
    void deveLancarExcecaoAoRegistrarItemNecessarioQuandoOsNaoExiste() {
        String numeroOs = "os-inexistente";
        Long servicoOsId = 55L;
        String emailAdmin = "admin@autoflow.com";

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.empty());
        List<ItemNecessarioEntity> itensVazios = List.of();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.registrarItemNecessario(numeroOs, servicoOsId, emailAdmin, itensVazios));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deveLancarExcecaoAoRegistrarItemNecessarioQuandoServicoNaoExisteNaOs() {
        String numeroOs = "abc-123";
        Long servicoOsId = 55L;
        Long servicoInexistenteId = 99L;
        String emailAdmin = "admin@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        UsuarioEntity admin = criarUsuario(1L, "Admin", emailAdmin, RoleEnum.ADMIN);

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailAdmin)).thenReturn(admin);
        List<ItemNecessarioEntity> itensVazios = List.of();

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarItemNecessario(numeroOs, servicoInexistenteId, emailAdmin, itensVazios));
    }

    @Test
    void deveIniciarServicoEBaixarEstoqueDosItensDoServico() {
        String numeroOs = "OS-123";
        Long servicoOsId = 55L;
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        ItemNecessarioEntity itemOriginal = criarItemNecessarioSolicitado(10L, 2);
        os.buscarServicoSolicitado(servicoOsId).registrarItensNecessarios(List.of(itemOriginal));
        ItemNecessarioEntity itemAtualizado = ItemNecessarioEntity.criar(
                10L, "Filtro", CategoriaPecaInsumo.PECA, new BigDecimal("50.00"), 2, StatusItemNecessario.DISPONIVEL
        );

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(pecaInsumoService.verificarDisponibilidadeEBaixar(List.of(itemOriginal)))
                .thenReturn(new BaixaEstoqueResult(List.of(itemAtualizado)));
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.iniciarServico(numeroOs, servicoOsId);

        ServicoSolicitadoEntity servico = resultado.buscarServicoSolicitado(servicoOsId);
        assertEquals(StatusServicoOs.EM_EXECUCAO, servico.getStatus());
        assertNotNull(servico.getIniciadoEm());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, resultado.getStatus());
        assertNotNull(resultado.getExecucaoIniciadaEm());
        assertNotNull(resultado.getUltimaAtualizacao());
        assertEquals(List.of(itemAtualizado), servico.getItensNecessarios());
        verify(repository).save(os);
        verify(historicoStatusOsRepository).save(argThat(historico ->
                StatusOrdemServico.EM_EXECUCAO.equals(historico.getStatus())
        ));
    }

    @Test
    void deveIniciarServicoSemRegistrarHistoricoQuandoOsJaEstaEmExecucao() {
        Long servicoOsId = 55L;
        String numeroOs = "OS-123";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        os.iniciarExecucao();
        ItemNecessarioEntity itemOriginal = criarItemNecessarioSolicitado(10L, 2);
        os.buscarServicoSolicitado(servicoOsId).registrarItensNecessarios(List.of(itemOriginal));
        ItemNecessarioEntity itemAtualizado = ItemNecessarioEntity.criar(
                10L, "Filtro", CategoriaPecaInsumo.PECA, new BigDecimal("50.00"), 2, StatusItemNecessario.DISPONIVEL
        );

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(pecaInsumoService.verificarDisponibilidadeEBaixar(List.of(itemOriginal)))
                .thenReturn(new BaixaEstoqueResult(List.of(itemAtualizado)));
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.iniciarServico(os.getNumeroOs(), servicoOsId);

        assertEquals(StatusOrdemServico.EM_EXECUCAO, resultado.getStatus());
        assertEquals(StatusServicoOs.EM_EXECUCAO, resultado.buscarServicoSolicitado(servicoOsId).getStatus());
        verify(repository).save(os);
        verify(historicoStatusOsRepository, never()).save(any());
    }

    @Test
    void deveLancarErroQuandoIniciarServicoComStatusInvalido() {

        Long servicoOsId = 55L;
        String numeroOs = "OS-123";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        os.setStatus(StatusOrdemServico.RECEBIDA);
        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));

        assertThrows(IllegalStateException.class, () -> service.iniciarServico(numeroOs, servicoOsId));

        verifyNoInteractions(pecaInsumoService);
        verify(repository, never()).save(any());
    }

    @Test
    void deveFinalizarServicoEFinalizarOsQuandoTodosServicosFinalizados() {
        String numeroOs = "OS-123";
        Long servicoOsId = 55L;
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        os.iniciarExecucao();
        os.buscarServicoSolicitado(servicoOsId).iniciar(List.of());

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.finalizarServico(numeroOs, servicoOsId);

        ServicoSolicitadoEntity servico = resultado.buscarServicoSolicitado(servicoOsId);
        assertEquals(StatusServicoOs.FINALIZADO, servico.getStatus());
        assertNotNull(servico.getFinalizadoEm());
        assertEquals(StatusOrdemServico.FINALIZADA, resultado.getStatus());
        assertNotNull(resultado.getFinalizadaEm());
        assertNotNull(resultado.getUltimaAtualizacao());
        verify(historicoStatusOsRepository).save(argThat(historico ->
                StatusOrdemServico.FINALIZADA.equals(historico.getStatus())
        ));
    }

    @Test
    void deveEntregarOrdemServico() {
        String numeroOs = "OS-123";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, 55L);
        os.setStatus(StatusOrdemServico.FINALIZADA);

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.entregar(numeroOs);

        assertEquals(StatusOrdemServico.ENTREGUE, resultado.getStatus());
        assertNotNull(resultado.getEntregueEm());
        assertNotNull(resultado.getUltimaAtualizacao());
        verify(repository).save(os);
        verify(historicoStatusOsRepository).save(argThat(historico ->
                StatusOrdemServico.ENTREGUE.equals(historico.getStatus())
        ));
    }

    @Test
    void deveFinalizarDiagnosticoEGerarOrcamento() {
        String numeroOs = "OS-123";

        String emailAdmin = "admin@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, 55L);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setLaudo("Laudo");
        os.setDiagnostico(diagnostico);
        UsuarioEntity admin = criarUsuario(1L, "Admin", emailAdmin, RoleEnum.ADMIN);
        OrcamentoEntity orcamento = new OrcamentoEntity();

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailAdmin)).thenReturn(admin);
        when(orcamentoVersioningServiceImpl.proximaVersaoPrincipalNumeroOs("OS-123")).thenReturn(1);
        when(orcamentoFactoryImpl.criarPrincipalDisponivel(eq(os), eq(1), any())).thenReturn(orcamento);
        when(orcamentoRepository.save(orcamento)).thenAnswer(invocation -> {
            orcamento.setId(10L);
            return orcamento;
        });
        when(orcamentoPublicacaoServiceImpl.publicar(10L))
                .thenReturn(new PublicacaoOrcamentoResult(10L, "http://localhost/orcamento"));
        when(repository.save(os)).thenReturn(os);

        FinalizarDiagnosticoResult resultado = service.finalizarDiagnostico(numeroOs, emailAdmin);

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, resultado.ordemServico().getStatus());
        assertEquals(10L, resultado.orcamentoId());
        assertEquals("http://localhost/orcamento", resultado.publicUrl());
        verify(historicoStatusOsRepository).save(argThat(historico ->
                StatusOrdemServico.AGUARDANDO_APROVACAO.equals(historico.getStatus())
        ));
    }

    @Test
    void deveFinalizarDiagnosticoMesmoQuandoNotificacaoFalhar() {
        String emailAdmin = "admin@autoflow.com";
        String numerOs = "OS-123";
        OrdemServicoEntity os = criarOrdemServicoComServico(numerOs, 55L);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setLaudo("Laudo");
        os.setDiagnostico(diagnostico);
        UsuarioEntity admin = criarUsuario(1L, "Admin", emailAdmin, RoleEnum.ADMIN);
        OrcamentoEntity orcamento = new OrcamentoEntity();

        when(repository.findByNumeroOs(numerOs)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailAdmin)).thenReturn(admin);
        when(orcamentoVersioningServiceImpl.proximaVersaoPrincipalNumeroOs(numerOs)).thenReturn(1);
        when(orcamentoFactoryImpl.criarPrincipalDisponivel(eq(os), eq(1), any())).thenReturn(orcamento);
        when(orcamentoRepository.save(orcamento)).thenAnswer(invocation -> {
            orcamento.setId(10L);
            return orcamento;
        });
        when(orcamentoPublicacaoServiceImpl.publicar(10L))
                .thenReturn(new PublicacaoOrcamentoResult(10L, "http://localhost/orcamento"));
        doThrow(new RuntimeException("smtp indisponivel"))
                .when(orcamentoNotificacaoService)
                .enviarLinkOrcamentoParaCliente(orcamento, os, "http://localhost/orcamento");
        when(repository.save(os)).thenReturn(os);

        FinalizarDiagnosticoResult resultado = service.finalizarDiagnostico(numerOs, emailAdmin);

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, resultado.ordemServico().getStatus());
        assertEquals(10L, resultado.orcamentoId());
        verify(repository).save(os);
    }

    @Test
    void deveFinalizarDiagnosticoComoMecanicoValidandoPermissao() {
        String numeroOs = "OS-123";

        String emailMecanico = "mecanico@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, 55L);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setLaudo("Laudo");
        os.setDiagnostico(diagnostico);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", emailMecanico, RoleEnum.MECANICO);
        OrcamentoEntity orcamento = new OrcamentoEntity();

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailMecanico)).thenReturn(mecanico);
        when(orcamentoVersioningServiceImpl.proximaVersaoPrincipalNumeroOs("OS-123")).thenReturn(1);
        when(orcamentoFactoryImpl.criarPrincipalDisponivel(eq(os), eq(1), any())).thenReturn(orcamento);
        when(orcamentoRepository.save(orcamento)).thenAnswer(invocation -> {
            orcamento.setId(10L);
            return orcamento;
        });
        when(orcamentoPublicacaoServiceImpl.publicar(10L))
                .thenReturn(new PublicacaoOrcamentoResult(10L, "http://localhost/orcamento"));
        when(repository.save(os)).thenReturn(os);

        FinalizarDiagnosticoResult resultado = service.finalizarDiagnostico(numeroOs, emailMecanico);

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, resultado.ordemServico().getStatus());
        verify(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
        verify(historicoStatusOsRepository).save(argThat(historico ->
                StatusOrdemServico.AGUARDANDO_APROVACAO.equals(historico.getStatus())
        ));
    }

    @Test
    void deveListarAcompanhamentoDoClienteAutenticado() {
        ClienteEntity cliente = criarCliente(1L);
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        OrcamentoEntity orcamento = criarOrcamento(99L, os.getNumeroOs());
        HistoricoStatusOsEntity historico = HistoricoStatusOsEntity.criar(
                os.getId(),
                StatusOrdemServico.AGUARDANDO_APROVACAO,
                "O orÃ§amento estÃ¡ disponÃ­vel e aguardando sua aprovaÃ§Ã£o.",
                os.getNumeroOs()
        );

        when(clienteRepository.findByUsuarioEmail("cliente@autoflow.com")).thenReturn(Optional.of(cliente));
        when(repository.findByCliente_IdOrderByDataAberturaDesc(cliente.getId())).thenReturn(List.of(os));
        when(orcamentoRepository.findByNumeroOsAndStatus(os.getNumeroOs(), com.autoflow.domain.orcamento.StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.of(orcamento));
        when(historicoStatusOsRepository.findByNumeroOsOrderByRegistradoEmAsc(os.getNumeroOs()))
                .thenReturn(List.of(historico));

        var resultado = service.listarAcompanhamentoCliente("cliente@autoflow.com");

        assertEquals(1, resultado.size());
        var acompanhamento = resultado.getFirst();
        assertEquals(os.getNumeroOs(), acompanhamento.numeroOs());
        assertEquals("ABC1D23", acompanhamento.placa());
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, acompanhamento.statusAtual());
        assertEquals(os.getUltimaAtualizacao(), acompanhamento.ultimaAtualizacao());
        assertEquals(
                AcompanhamentoOrdemServicoResponse.mensagemParaCliente(StatusOrdemServico.AGUARDANDO_APROVACAO),
                acompanhamento.mensagemParaCliente()
        );
        assertEquals(99L, acompanhamento.orcamentoAtual().id());
        assertEquals(com.autoflow.domain.orcamento.StatusOrcamento.DISPONIVEL, acompanhamento.situacaoAprovacao());
        assertEquals(1, acompanhamento.historicoStatus().size());
    }

    @Test
    void deveListarOrdensServicoComFiltroVazio() {
        OrdemServicoEntity primeiraOrdem = criarOrdemServicoComServico("OS-123", 55L);
        OrdemServicoEntity segundaOrdem = criarOrdemServicoComServico("OS-456", 66L);
        PageImpl<OrdemServicoEntity> page = new PageImpl<>(List.of(primeiraOrdem, segundaOrdem));
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataAbertura"));

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<OrdemServicoEntity> resultado = service.listar(new OrdemServicoFiltro(null, null, null), pageable);

        assertEquals(2, resultado.getTotalElements());
        assertEquals(List.of(primeiraOrdem, segundaOrdem), resultado.getContent());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void deveListarOrdensServicoFiltrandoPorStatus() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-789", 77L);
        PageImpl<OrdemServicoEntity> page = new PageImpl<>(List.of(os));
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataAbertura"));

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<OrdemServicoEntity> resultado = service.listar(
                new OrdemServicoFiltro(null, null, StatusOrdemServico.RECEBIDA), pageable);

        assertEquals(1, resultado.getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void deveBuscarOrcamentoAtualDisponivel() {
        OrcamentoEntity orcamentoDisponivel = criarOrcamento(99L, "OS-123");
        when(orcamentoRepository.findByNumeroOsAndStatus(
                "OS-123",
                com.autoflow.domain.orcamento.StatusOrcamento.DISPONIVEL
        )).thenReturn(Optional.of(orcamentoDisponivel));

        OrcamentoEntity resultado = service.buscarOrcamentoAtual("OS-123");

        assertSame(orcamentoDisponivel, resultado);
        verify(orcamentoRepository).findByNumeroOsAndStatus(
                "OS-123",
                com.autoflow.domain.orcamento.StatusOrcamento.DISPONIVEL
        );
        verify(orcamentoRepository, never()).findTopByNumeroOsOrderByVersaoDesc(anyString());
    }

    @Test
    void deveBuscarUltimoOrcamentoQuandoNaoHouverDisponivel() {
        OrcamentoEntity ultimoOrcamento = criarOrcamento(100L, "OS-123");
        ultimoOrcamento.setStatus(com.autoflow.domain.orcamento.StatusOrcamento.APROVADO);

        when(orcamentoRepository.findByNumeroOsAndStatus(
                "OS-123",
                com.autoflow.domain.orcamento.StatusOrcamento.DISPONIVEL
        )).thenReturn(Optional.empty());
        when(orcamentoRepository.findTopByNumeroOsOrderByVersaoDesc("OS-123"))
                .thenReturn(Optional.of(ultimoOrcamento));

        OrcamentoEntity resultado = service.buscarOrcamentoAtual("OS-123");

        assertSame(ultimoOrcamento, resultado);
        verify(orcamentoRepository).findTopByNumeroOsOrderByVersaoDesc("OS-123");
    }

    @Test
    void deveLancarNotFoundQuandoClienteAutenticadoNaoExistirNoAcompanhamento() {
        when(clienteRepository.findByUsuarioEmail("cliente@autoflow.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.listarAcompanhamentoCliente("cliente@autoflow.com")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(repository, historicoStatusOsRepository);
    }

    @Test
    void deveLancarNotFoundQuandoOrdemServicoNaoExistir() {
        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.buscaOrdemServicoPorNumeroOs("OS-123")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deveValidarPermissaoAoRegistrarItensComoMecanico() {
        String numeroOs = "abc-123";
        Long servicoOsId = 55L;
        String email = "mecanico@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        UsuarioEntity mecanico = criarUsuario(1L, "Mecanico", email, RoleEnum.MECANICO);
        RuntimeException erro = new ResponseStatusException(HttpStatus.FORBIDDEN, "sem permissao");

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(email)).thenReturn(mecanico);
        doThrow(erro).when(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
        List<ItemNecessarioEntity> itensVazios = List.of();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.registrarItemNecessario(numeroOs, servicoOsId, email, itensVazios)
        );

        assertEquals(erro, exception);
        verify(repository, never()).save(any());
    }

    @Test
    void deveCalcularTempoMedioFinalizacao() {
        TempoMedioOrdemServicoProjection projection = mock(TempoMedioOrdemServicoProjection.class);
        when(projection.getQuantidadeOrdensFinalizadas()).thenReturn(3L);
        when(projection.getTempoMedioSegundos()).thenReturn(7200.0);
        when(repository.calcularTempoMedioFinalizacao()).thenReturn(projection);

        TempoMedioOrdemServicoResponse resultado = service.calcularTempoMedioFinalizacao();

        assertNotNull(resultado);
        assertEquals(3L, resultado.quantidadeOrdensFinalizadas());
        assertEquals(7200.0, resultado.tempoMedioSegundos());
        assertEquals(120.0, resultado.tempoMedioMinutos());
        assertEquals(2.0, resultado.tempoMedioHoras());
        verify(repository).calcularTempoMedioFinalizacao();
    }

    @Test
    void deveRetornarTemposNulosQuandoNaoExistirOrdemFinalizada() {
        TempoMedioOrdemServicoProjection projection = mock(TempoMedioOrdemServicoProjection.class);
        when(projection.getQuantidadeOrdensFinalizadas()).thenReturn(0L);
        when(projection.getTempoMedioSegundos()).thenReturn(null);
        when(repository.calcularTempoMedioFinalizacao()).thenReturn(projection);

        TempoMedioOrdemServicoResponse resultado = service.calcularTempoMedioFinalizacao();

        assertNotNull(resultado);
        assertEquals(0L, resultado.quantidadeOrdensFinalizadas());
        assertNull(resultado.tempoMedioSegundos());
        assertNull(resultado.tempoMedioMinutos());
        assertNull(resultado.tempoMedioHoras());
        verify(repository).calcularTempoMedioFinalizacao();
    }

    private OrdemServicoEntity criarOrdemServicoComServico(String numeroOs, Long servicoOsId) {
        ClienteEntity cliente = criarCliente(1L);
        OrdemServicoEntity os = OrdemServicoEntity.criar(cliente, criarVeiculo(1L, cliente));
        os.setNumeroOs(numeroOs);
        ServicoSolicitadoEntity servico = ServicoSolicitadoEntity.criar(servicoOsId, "Revisao", new BigDecimal("100.00"));
        servico.setId(servicoOsId);
        os.adicionarServicosSolicitados(List.of(servico));
        return os;
    }

    private ClienteEntity criarCliente(Long clienteId) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(clienteId);
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("cliente@autoflow.com");
        cliente.setNome("Cliente");
        return cliente;
    }

    private VeiculoEntity criarVeiculo(Long veiculoId, ClienteEntity cliente) {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(veiculoId);
        veiculo.setAno(2014);
        veiculo.setMarca("marca");
        veiculo.setModelo("modelo");
        veiculo.setPlaca("ABC1D23");
        veiculo.setCliente(cliente);
        return veiculo;
    }

    private ServicoEntity criarServico(Long servicoId, String nome, BigDecimal valor) {
        ServicoEntity servico = new ServicoEntity();
        servico.setId(servicoId);
        servico.setNome(nome);
        servico.setValor(valor);
        return servico;
    }

    private OrcamentoEntity criarOrcamento(Long id, String numeroOs) {
        OrcamentoEntity orcamento = new OrcamentoEntity();
        orcamento.setId(id);
        orcamento.setNumeroOs(numeroOs);
        orcamento.setTipo(com.autoflow.domain.orcamento.TipoOrcamento.PRINCIPAL);
        orcamento.setVersao(1);
        orcamento.setStatus(com.autoflow.domain.orcamento.StatusOrcamento.DISPONIVEL);
        orcamento.setTotalServicos(new BigDecimal("100.00"));
        orcamento.setTotalItens(BigDecimal.ZERO);
        orcamento.setTotalGeral(new BigDecimal("100.00"));
        return orcamento;
    }

    private VeiculoOrdemServicoRequest criarVeiculoRequestCompleto() {
        return new VeiculoOrdemServicoRequest("ABC1D23", "Honda", "Civic", 2020);
    }

    private UsuarioEntity criarUsuario(Long usuarioId, String nome, String email, RoleEnum role) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setRole(role);
        return usuario;
    }

    private PecaInsumoEntity criarPecaInsumo(
            Long pecaInsumoId,
            String nome,
            CategoriaPecaInsumo tipo,
            BigDecimal valor,
            int quantidade
    ) {
        PecaInsumoEntity pecaInsumo = new PecaInsumoEntity();
        pecaInsumo.setId(pecaInsumoId);
        pecaInsumo.setNome(nome);
        pecaInsumo.setTipo(tipo);
        pecaInsumo.setValor(valor);
        pecaInsumo.setQuantidade(quantidade);
        return pecaInsumo;
    }

    private ItemNecessarioEntity criarItemNecessarioSolicitado(Long pecaInsumoId, int quantidade) {
        return ItemNecessarioEntity.criar(
                pecaInsumoId,
                "Item solicitado",
                CategoriaPecaInsumo.PECA,
                BigDecimal.ZERO,
                quantidade,
                null
        );
    }
}
