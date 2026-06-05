package com.autoflow.service.ordemservico;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.service.orcamento.OrcamentoFactory;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import com.autoflow.service.orcamento.dto.PublicacaoOrcamentoResult;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
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

    @Test
    void deveCriarOrdemServicoComServicosVinculados() {
        VeiculoEntity veiculo = criarVeiculo(1L, criarCliente(1L));
        ServicoSolicitadoEntity solicitado = new ServicoSolicitadoEntity(10L);
        ServicoEntity servicoCatalogo = criarServico(10L, "Revisao", new BigDecimal("100.00"));

        when(veiculoService.buscarPorId(1L)).thenReturn(veiculo);
        when(servicoService.buscarEntityPorId(10L)).thenReturn(servicoCatalogo);
        when(repository.save(any(OrdemServicoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoEntity resultado = service.criar(1L, List.of(solicitado));

        assertEquals(StatusOrdemServico.RECEBIDA, resultado.getStatus());
        assertEquals(1, resultado.getServicosSolicitados().size());
        ServicoSolicitadoEntity servicoOs = resultado.getServicosSolicitados().getFirst();
        assertEquals(10L, servicoOs.getServicoId());
        assertEquals("Revisao", servicoOs.getNome());
        assertEquals(new BigDecimal("100.00"), servicoOs.getValor());
        assertEquals(StatusServicoOs.AGUARDANDO, servicoOs.getStatus());
        assertEquals(resultado, servicoOs.getOrdemServico());
    }

    @Test
    void deveLancarIllegalArgumentQuandoCriarSemServicos() {
        VeiculoEntity veiculo = criarVeiculo(1L, criarCliente(1L));
        when(veiculoService.buscarPorId(1L)).thenReturn(veiculo);

        assertThrows(IllegalArgumentException.class, () -> service.criar(1L, null));
        verify(repository, never()).save(any());
    }

    @Test
    void deveIncluirServicosNaOrdemServico() {
        OrdemServicoEntity os = criarOrdemServicoComServico(1L, 55L);
        ServicoSolicitadoEntity solicitado = new ServicoSolicitadoEntity(20L);
        ServicoEntity servicoCatalogo = criarServico(20L, "Troca oleo", new BigDecimal("80.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(os));
        when(servicoService.buscarEntityPorId(20L)).thenReturn(servicoCatalogo);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.incluirServicos(1L, List.of(solicitado));

        assertEquals(2, resultado.getServicosSolicitados().size());
        ServicoSolicitadoEntity servicoIncluido = resultado.getServicosSolicitados().get(1);
        assertEquals(20L, servicoIncluido.getServicoId());
        assertEquals("Troca oleo", servicoIncluido.getNome());
        assertEquals(os, servicoIncluido.getOrdemServico());
    }

    @Test
    void deveLancarIllegalArgumentQuandoIncluirServicosComListaVazia() {
        OrdemServicoEntity os = criarOrdemServicoComServico(1L, 55L);
        List<ServicoSolicitadoEntity> servicosVazios = List.of();
        when(repository.findById(1L)).thenReturn(Optional.of(os));

        assertThrows(IllegalArgumentException.class, () -> service.incluirServicos(1L, servicosVazios));
        verify(repository, never()).save(any());
    }

    @Test
    void deveAtribuirMecanicoCriandoDiagnosticoQuandoNaoExistir() {
        OrdemServicoEntity os = criarOrdemServicoComServico(1L, 55L);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findById(1L)).thenReturn(Optional.of(os));
        when(usuarioService.buscarMecanicoPorId(2L)).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.atribuirMecanico(1L, 2L);

        assertNotNull(resultado.getDiagnostico());
        assertEquals(mecanico, resultado.getDiagnostico().getMecanico());
    }

    @Test
    void deveAtribuirMecanicoMantendoDiagnosticoExistente() {
        OrdemServicoEntity os = criarOrdemServicoComServico(1L, 55L);
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        os.setDiagnostico(diagnostico);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findById(1L)).thenReturn(Optional.of(os));
        when(usuarioService.buscarMecanicoPorId(2L)).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.atribuirMecanico(1L, 2L);

        assertSame(diagnostico, resultado.getDiagnostico());
        assertEquals(mecanico, resultado.getDiagnostico().getMecanico());
    }

    @Test
    void deveIniciarDiagnosticoComoAdminSemValidarMecanicoAtribuido() {
        OrdemServicoEntity os = criarOrdemServicoComServico(1L, 55L);
        os.setDiagnostico(new DiagnosticoEntity());
        UsuarioEntity admin = criarUsuario(1L, "Admin", "admin@autoflow.com", RoleEnum.ADMIN);

        when(repository.findById(1L)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail("admin@autoflow.com")).thenReturn(admin);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.iniciarDiagnostico(1L, "admin@autoflow.com");

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, resultado.getStatus());
        assertNotNull(resultado.getDiagnostico().getIniciadoEm());
        verify(ordemServicoAccessPolicy, never()).validarPodeAlterarDiagnostico(any(), any());
    }

    @Test
    void deveIniciarDiagnosticoComoMecanicoValidandoPermissao() {
        OrdemServicoEntity os = criarOrdemServicoComServico(1L, 55L);
        os.setDiagnostico(new DiagnosticoEntity());
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findById(1L)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail("mecanico@autoflow.com")).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.iniciarDiagnostico(1L, "mecanico@autoflow.com");

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, resultado.getStatus());
        verify(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
    }

    @Test
    void deveRegistrarLaudo() {
        OrdemServicoEntity os = criarOrdemServicoComServico(1L, 55L);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        os.setDiagnostico(new DiagnosticoEntity());
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findById(1L)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail("mecanico@autoflow.com")).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.registrarLaudo(1L, "mecanico@autoflow.com", "Laudo");

        assertEquals("Laudo", resultado.getDiagnostico().getLaudo());
        verify(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
    }

    @Test
    void deveRegistrarItensNecessariosNoServicoDaOs() {
        Long ordemServicoId = 1L;
        Long servicoOsId = 55L;
        Long pecaInsumoId = 10L;
        String emailAdmin = "admin@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(ordemServicoId, servicoOsId);
        UsuarioEntity admin = criarUsuario(1L, "Admin", emailAdmin, RoleEnum.ADMIN);
        PecaInsumoEntity estoque = criarPecaInsumo(pecaInsumoId, "Filtro", CategoriaPecaInsumo.PECA, new BigDecimal("50.00"), 5);

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailAdmin)).thenReturn(admin);
        when(pecaInsumoService.buscarEntityPorId(pecaInsumoId)).thenReturn(estoque);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.registrarItemNecessario(
                ordemServicoId,
                servicoOsId,
                emailAdmin,
                List.of(criarItemNecessarioSolicitado(pecaInsumoId, 2))
        );

        ServicoSolicitadoEntity servico = resultado.buscarServicoSolicitado(servicoOsId);
        assertEquals(1, servico.getItensNecessarios().size());
        ItemNecessarioEntity item = servico.getItensNecessarios().getFirst();
        assertEquals("Filtro", item.getNome());
        assertEquals(StatusItemNecessario.DISPONIVEL, item.getStatus());
        verify(repository).save(os);
    }

    @Test
    void deveIniciarServicoEBaixarEstoqueDosItensDoServico() {
        Long ordemServicoId = 1L;
        Long servicoOsId = 55L;
        OrdemServicoEntity os = criarOrdemServicoComServico(ordemServicoId, servicoOsId);
        ItemNecessarioEntity itemOriginal = criarItemNecessarioSolicitado(10L, 2);
        os.buscarServicoSolicitado(servicoOsId).registrarItensNecessarios(List.of(itemOriginal));
        ItemNecessarioEntity itemAtualizado = ItemNecessarioEntity.criar(
                10L, "Filtro", CategoriaPecaInsumo.PECA, new BigDecimal("50.00"), 2, StatusItemNecessario.DISPONIVEL
        );

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(os));
        when(pecaInsumoService.verificarDisponibilidadeEBaixar(List.of(itemOriginal)))
                .thenReturn(new BaixaEstoqueResult(List.of(itemAtualizado)));
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.iniciarServico(ordemServicoId, servicoOsId);

        ServicoSolicitadoEntity servico = resultado.buscarServicoSolicitado(servicoOsId);
        assertEquals(StatusServicoOs.EM_EXECUCAO, servico.getStatus());
        assertNotNull(servico.getIniciadoEm());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, resultado.getStatus());
        assertNotNull(resultado.getExecucaoIniciadaEm());
        assertEquals(List.of(itemAtualizado), servico.getItensNecessarios());
        verify(repository).save(os);
    }

    @Test
    void deveFinalizarServicoEFinalizarOsQuandoTodosServicosFinalizados() {
        Long ordemServicoId = 1L;
        Long servicoOsId = 55L;
        OrdemServicoEntity os = criarOrdemServicoComServico(ordemServicoId, servicoOsId);
        os.buscarServicoSolicitado(servicoOsId).iniciar(List.of());
        os.iniciarExecucaoSeNecessario();

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(os));
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.finalizarServico(ordemServicoId, servicoOsId);

        ServicoSolicitadoEntity servico = resultado.buscarServicoSolicitado(servicoOsId);
        assertEquals(StatusServicoOs.FINALIZADO, servico.getStatus());
        assertNotNull(servico.getFinalizadoEm());
        assertEquals(StatusOrdemServico.FINALIZADA, resultado.getStatus());
        assertNotNull(resultado.getFinalizadaEm());
    }

    @Test
    void deveFinalizarDiagnosticoEGerarOrcamento() {
        Long ordemServicoId = 1L;
        String emailAdmin = "admin@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(ordemServicoId, 55L);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setLaudo("Laudo");
        os.setDiagnostico(diagnostico);
        UsuarioEntity admin = criarUsuario(1L, "Admin", emailAdmin, RoleEnum.ADMIN);
        OrcamentoEntity orcamento = new OrcamentoEntity();

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailAdmin)).thenReturn(admin);
        when(orcamentoVersioningServiceImpl.proximaVersaoPrincipal(ordemServicoId)).thenReturn(1);
        when(orcamentoFactoryImpl.criarPrincipalDisponivel(eq(os), eq(1), any())).thenReturn(orcamento);
        when(orcamentoRepository.save(orcamento)).thenAnswer(invocation -> {
            orcamento.setId(10L);
            return orcamento;
        });
        when(orcamentoPublicacaoServiceImpl.publicar(10L))
                .thenReturn(new PublicacaoOrcamentoResult(10L, "http://localhost/orcamento"));
        when(repository.save(os)).thenReturn(os);

        FinalizarDiagnosticoResult resultado = service.finalizarDiagnostico(ordemServicoId, emailAdmin);

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, resultado.ordemServico().getStatus());
        assertEquals(10L, resultado.orcamentoId());
        assertEquals("http://localhost/orcamento", resultado.publicUrl());
    }

    @Test
    void deveFinalizarDiagnosticoComoMecanicoValidandoPermissao() {
        Long ordemServicoId = 1L;
        String emailMecanico = "mecanico@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(ordemServicoId, 55L);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setLaudo("Laudo");
        os.setDiagnostico(diagnostico);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", emailMecanico, RoleEnum.MECANICO);
        OrcamentoEntity orcamento = new OrcamentoEntity();

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(emailMecanico)).thenReturn(mecanico);
        when(orcamentoVersioningServiceImpl.proximaVersaoPrincipal(ordemServicoId)).thenReturn(1);
        when(orcamentoFactoryImpl.criarPrincipalDisponivel(eq(os), eq(1), any())).thenReturn(orcamento);
        when(orcamentoRepository.save(orcamento)).thenAnswer(invocation -> {
            orcamento.setId(10L);
            return orcamento;
        });
        when(orcamentoPublicacaoServiceImpl.publicar(10L))
                .thenReturn(new PublicacaoOrcamentoResult(10L, "http://localhost/orcamento"));
        when(repository.save(os)).thenReturn(os);

        FinalizarDiagnosticoResult resultado = service.finalizarDiagnostico(ordemServicoId, emailMecanico);

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, resultado.ordemServico().getStatus());
        verify(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
    }

    @Test
    void deveLancarNotFoundQuandoOrdemServicoNaoExistir() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.buscaOrdemServicoPorId(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deveValidarPermissaoAoRegistrarItensComoMecanico() {
        Long ordemServicoId = 1L;
        Long servicoOsId = 55L;
        String email = "mecanico@autoflow.com";
        OrdemServicoEntity os = criarOrdemServicoComServico(ordemServicoId, servicoOsId);
        UsuarioEntity mecanico = criarUsuario(1L, "Mecanico", email, RoleEnum.MECANICO);
        RuntimeException erro = new ResponseStatusException(HttpStatus.FORBIDDEN, "sem permissao");

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(os));
        when(usuarioService.buscarPorEmail(email)).thenReturn(mecanico);
        doThrow(erro).when(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
        List<ItemNecessarioEntity> itensVazios = List.of();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.registrarItemNecessario(ordemServicoId, servicoOsId, email, itensVazios)
        );

        assertEquals(erro, exception);
        verify(repository, never()).save(any());
    }

    private OrdemServicoEntity criarOrdemServicoComServico(Long ordemServicoId, Long servicoOsId) {
        OrdemServicoEntity os = OrdemServicoEntity.criar(criarVeiculo(1L, criarCliente(1L)));
        os.setId(ordemServicoId);
        ServicoSolicitadoEntity servico = ServicoSolicitadoEntity.criar(10L, "Revisao", new BigDecimal("100.00"));
        servico.setId(servicoOsId);
        os.adicionarServicos(List.of(servico));
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
