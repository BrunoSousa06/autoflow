package com.autoflow.service.ordemservico;

import com.autoflow.application.dto.ordemservico.acompanhamento.TokenAcompanhamentoOutput;
import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.usecases.cliente.BuscarClientePorCpfCnpjUseCase;
import com.autoflow.application.usecases.pecainsumo.BaixarEstoqueUseCase;
import com.autoflow.application.usecases.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase;
import com.autoflow.application.usecases.ordemservico.acompanhamento.GerarTokenAcompanhamentoUseCase;
import com.autoflow.application.usecases.ordemservico.acompanhamento.EnviarLinkAcompanhamentoUseCase;
import com.autoflow.application.usecases.ordemservico.acompanhamento.AcompanharOrdemServicoUseCase;
import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.service.ordemservico.BuscarOuCadastrarVeiculoForOrdemServicoUseCase;
import com.autoflow.application.usecases.usuario.BuscarMecanicoPorIdUseCase;
import com.autoflow.application.usecases.usuario.BuscarUsuarioPorEmailUseCase;
import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.ClienteOrcamentoSnapshot;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoNotificacaoGateway;
import com.autoflow.application.gateway.OrcamentoVersioningGateway;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.repository.ordemservico.historico.HistoricoStatusOsRepository;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import com.autoflow.service.ordemservico.dto.OrdemServicoCriada;
import com.autoflow.service.ordemservico.dto.OrdemServicoFiltro;
import com.autoflow.service.ordemservico.impl.OrdemServicoAccessPolicy;
import com.autoflow.service.ordemservico.impl.OrdemServicoServiceImpl;
import com.autoflow.service.servico.ServicoService;
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
    BuscarOuCadastrarVeiculoForOrdemServicoUseCase buscarOuCadastrarVeiculoUseCase;
    @Mock
    ServicoService servicoService;
    @Mock
    BuscarUsuarioPorEmailUseCase buscarUsuarioPorEmailUseCase;
    @Mock
    BuscarMecanicoPorIdUseCase buscarMecanicoPorIdUseCase;
    @Mock
    BaixarEstoqueUseCase baixarEstoqueUseCase;
    @Mock
    ConsultarDisponibilidadeEstoqueUseCase consultarDisponibilidadeEstoqueUseCase;
    @Mock
    OrdemServicoAccessPolicy ordemServicoAccessPolicy;
    @Mock
    OrcamentoFactory orcamentoFactoryImpl;
    @Mock
    OrcamentoVersioningGateway orcamentoVersioningGateway;
    @Mock
    OrcamentoGateway orcamentoGateway;
    @Mock
    OrcamentoPublicacaoGateway orcamentoPublicacaoGateway;
    @Mock
    HistoricoStatusOsRepository historicoStatusOsRepository;
    @Mock
    OrcamentoNotificacaoGateway orcamentoNotificacaoGateway;
    @Mock
    BuscarClientePorCpfCnpjUseCase buscarClientePorCpfCnpjUseCase;

    @Mock
    private GerarTokenAcompanhamentoUseCase
            gerarTokenAcompanhamentoUseCase;
    @Mock
    private EnviarLinkAcompanhamentoUseCase enviarLinkAcompanhamentoUseCase;
    @Mock
    private AcompanharOrdemServicoUseCase acompanharOrdemServicoUseCase;

    @Test
    void deveCriarOrdemServicoComServicosVinculadosETokenDeAcompanhamento() {
        Long ordemServicoId = 100L;
        String tokenOriginal = "token-publico-original";
        String tokenHash = "token-hash";

        ClienteEntity cliente = criarCliente(1L);
        ClienteOutput clienteOutput = toClienteOutput(cliente);

        VeiculoEntity veiculo =
                criarVeiculo(1L, cliente);

        VeiculoOrdemServicoRequest veiculoRequest =
                criarVeiculoRequestCompleto();

        ServicoSolicitadoEntity solicitado =
                new ServicoSolicitadoEntity(10L);

        ServicoEntity servicoCatalogo = criarServico(
                10L,
                "Revisao",
                new BigDecimal("100.00")
        );

        TokenAcompanhamentoOutput tokenGerado =
                new TokenAcompanhamentoOutput(
                        tokenOriginal,
                        tokenHash
                );

        when(
                buscarClientePorCpfCnpjUseCase.execute(
                        "12345678901"
                )
        ).thenReturn(clienteOutput);

        when(
                buscarOuCadastrarVeiculoUseCase.execute(
                        clienteOutput,
                        toVeiculoInput(veiculoRequest)
                )
        ).thenReturn(veiculo);

        when(servicoService.buscarEntityPorId(10L))
                .thenReturn(servicoCatalogo);

        when(repository.save(any(OrdemServicoEntity.class)))
                .thenAnswer(invocation -> {
                    OrdemServicoEntity ordemServico =
                            invocation.getArgument(0);

                    ordemServico.setId(ordemServicoId);

                    return ordemServico;
                });

        when(
                gerarTokenAcompanhamentoUseCase.execute(
                        ordemServicoId
                )
        ).thenReturn(tokenGerado);

        OrdemServicoCriada resultado = service.criar(
                "12345678901",
                toVeiculoInput(veiculoRequest),
                List.of(solicitado)
        );

        OrdemServicoEntity ordemServicoCriada =
                resultado.ordemServico();

        verify(enviarLinkAcompanhamentoUseCase).execute(
                ordemServicoCriada,
                tokenOriginal
        );

        assertAll(
                () -> assertNotNull(resultado),
                () -> assertNotNull(ordemServicoCriada),
                () -> assertEquals(
                        tokenOriginal,
                        resultado.tokenAcompanhamento()
                ),
                () -> assertEquals(
                        ordemServicoId,
                        ordemServicoCriada.getId()
                ),
                () -> assertEquals(
                        StatusOrdemServico.RECEBIDA,
                        ordemServicoCriada.getStatus()
                ),
                () -> assertNotNull(
                        ordemServicoCriada.getUltimaAtualizacao()
                ),
                () -> assertEquals(
                        "12345678901",
                        ordemServicoCriada
                                .getCliente()
                                .getCpfCnpj()
                ),
                () -> assertEquals(
                        veiculo,
                        ordemServicoCriada.getVeiculo()
                ),
                () -> assertEquals(
                        1,
                        ordemServicoCriada
                                .getServicosSolicitados()
                                .size()
                )
        );

        ServicoSolicitadoEntity servicoOs =
                ordemServicoCriada
                        .getServicosSolicitados()
                        .getFirst();

        assertAll(
                () -> assertEquals(
                        10L,
                        servicoOs.getServicoId()
                ),
                () -> assertEquals(
                        "Revisao",
                        servicoOs.getNome()
                ),
                () -> assertEquals(
                        new BigDecimal("100.00"),
                        servicoOs.getValor()
                ),
                () -> assertEquals(
                        StatusServicoOs.AGUARDANDO,
                        servicoOs.getStatus()
                ),
                () -> assertSame(
                        ordemServicoCriada,
                        servicoOs.getOrdemServico()
                )
        );

        verify(repository).save(
                any(OrdemServicoEntity.class)
        );

        verify(gerarTokenAcompanhamentoUseCase)
                .execute(ordemServicoId);

        verify(historicoStatusOsRepository).save(
                argThat(historico ->
                        StatusOrdemServico.RECEBIDA.equals(
                                historico.getStatus()
                        )
                                && historico.getMensagemCliente() != null
                )
        );
    }

    @Test
    void deveLancarIllegalArgumentQuandoCriarSemServicos() {
        ClienteEntity cliente = criarCliente(1L);
        ClienteOutput clienteOutput = toClienteOutput(cliente);
        VeiculoEntity veiculo =
                criarVeiculo(1L, cliente);

        VeiculoOrdemServicoRequest veiculoRequest =
                criarVeiculoRequestCompleto();

        when(
                buscarClientePorCpfCnpjUseCase.execute(
                        "12345678901"
                )
        ).thenReturn(clienteOutput);

        when(
                buscarOuCadastrarVeiculoUseCase.execute(
                        clienteOutput,
                        toVeiculoInput(veiculoRequest)
                )
        ).thenReturn(veiculo);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.criar(
                        "12345678901",
                        toVeiculoInput(veiculoRequest),
                        null
                )
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(gerarTokenAcompanhamentoUseCase);
    }

    @Test
    void devePropagarNotFoundQuandoClienteNaoExistirAoCriarOrdemServico() {
        ResponseStatusException erro = new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado");
        VeiculoOrdemServicoRequest veiculoRequest = criarVeiculoRequestCompleto();
        List<ServicoSolicitadoEntity> servicosSolicitados = List.of(new ServicoSolicitadoEntity(10L));
        when(buscarClientePorCpfCnpjUseCase.execute("12345678901")).thenThrow(erro);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar("12345678901", toVeiculoInput(veiculoRequest), servicosSolicitados)
        );

        assertSame(erro, exception);
        verifyNoInteractions(
                buscarOuCadastrarVeiculoUseCase,
                servicoService,
                repository,
                gerarTokenAcompanhamentoUseCase
        );
    }

    @Test
    void devePropagarConflictQuandoPlacaPertencerAOutroCliente() {
        ClienteEntity cliente = criarCliente(1L);
        ClienteOutput clienteOutput = toClienteOutput(cliente);
        VeiculoOrdemServicoRequest veiculoRequest = criarVeiculoRequestCompleto();
        ResponseStatusException erro = new ResponseStatusException(HttpStatus.CONFLICT, "Placa ja cadastrada para outro cliente.");

        when(buscarClientePorCpfCnpjUseCase.execute("12345678901")).thenReturn(clienteOutput);
        when(buscarOuCadastrarVeiculoUseCase.execute(clienteOutput, toVeiculoInput(veiculoRequest))).thenThrow(erro);
        List<ServicoSolicitadoEntity> servicosSolicitados = List.of(new ServicoSolicitadoEntity(10L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar("12345678901", toVeiculoInput(veiculoRequest), servicosSolicitados)
        );

        assertSame(erro, exception);
        verifyNoInteractions(
                servicoService,
                repository,
                gerarTokenAcompanhamentoUseCase
        );
    }

    @Test
    void deveIncluirServicosNaOrdemServico() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        ServicoSolicitadoEntity solicitado = new ServicoSolicitadoEntity(20L);
        ServicoEntity servicoCatalogo = criarServico(20L, "Troca oleo", new BigDecimal("80.00"));

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(servicoService.buscarEntityPorId(20L)).thenReturn(servicoCatalogo);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.incluirServicos("OS-123", List.of(solicitado), "atendente@autoflow.com");

        assertEquals(2, resultado.getServicosSolicitados().size());
        ServicoSolicitadoEntity servicoIncluido = resultado.getServicosSolicitados().get(1);
        assertEquals(20L, servicoIncluido.getServicoId());
        assertEquals("Troca oleo", servicoIncluido.getNome());
        assertEquals(os, servicoIncluido.getOrdemServico());
        verify(buscarUsuarioPorEmailUseCase, never()).execute(anyString());
    }

    @Test
    void deveIncluirServicosNaOrdemServicoEmDiagnosticoComoMecanicoAtribuido() {
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(mecanico);
        os.setDiagnostico(diagnostico);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        ServicoSolicitadoEntity solicitado = new ServicoSolicitadoEntity(20L);
        ServicoEntity servicoCatalogo = criarServico(20L, "Troca oleo", new BigDecimal("80.00"));

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(buscarUsuarioPorEmailUseCase.execute("mecanico@autoflow.com")).thenReturn(mecanico);
        when(servicoService.buscarEntityPorId(20L)).thenReturn(servicoCatalogo);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.incluirServicos("OS-123", List.of(solicitado), "mecanico@autoflow.com");

        assertEquals(2, resultado.getServicosSolicitados().size());
        verify(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
    }

    @Test
    void deveIncluirServicosEmDiagnosticoComoAdminSemValidarMecanico() {
        UsuarioEntity admin = criarUsuario(1L, "Admin", "admin@autoflow.com", RoleEnum.ADMIN);
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        ServicoSolicitadoEntity solicitado = new ServicoSolicitadoEntity(20L);
        ServicoEntity servicoCatalogo = criarServico(20L, "Troca oleo", new BigDecimal("80.00"));

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(buscarUsuarioPorEmailUseCase.execute("admin@autoflow.com")).thenReturn(admin);
        when(servicoService.buscarEntityPorId(20L)).thenReturn(servicoCatalogo);
        when(repository.save(os)).thenReturn(os);

        service.incluirServicos("OS-123", List.of(solicitado), "admin@autoflow.com");

        verify(ordemServicoAccessPolicy, never()).validarPodeAlterarDiagnostico(any(), any());
    }

    @Test
    void deveBloquearInclusaoDeServicosEmDiagnosticoParaMecanicoNaoAtribuido() {
        UsuarioEntity mecanicoAtribuido = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);
        UsuarioEntity outroMecanico = criarUsuario(3L, "Outro", "outro@autoflow.com", RoleEnum.MECANICO);
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(mecanicoAtribuido);
        os.setDiagnostico(diagnostico);
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(buscarUsuarioPorEmailUseCase.execute("outro@autoflow.com")).thenReturn(outroMecanico);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente o mecânico atribuído pode alterar o diagnóstico."))
                .when(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, outroMecanico);

        List<ServicoSolicitadoEntity> novosServicos = List.of(new ServicoSolicitadoEntity(20L));
        assertThrows(ResponseStatusException.class,
                () -> service.incluirServicos("OS-123", novosServicos, "outro@autoflow.com"));

        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarIllegalArgumentQuandoIncluirServicosComListaVazia() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        List<ServicoSolicitadoEntity> servicosVazios = List.of();
        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));

        assertThrows(IllegalArgumentException.class, () -> service.incluirServicos("OS-123", servicosVazios, "atendente@autoflow.com"));
        verify(repository, never()).save(any());
    }

    @Test
    void deveAtribuirMecanicoCriandoDiagnosticoQuandoNaoExistir() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(buscarMecanicoPorIdUseCase.execute(2L)).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.atribuirMecanico("OS-123", 2L, null);

        assertNotNull(resultado.getDiagnostico());
        assertEquals(mecanico, resultado.getDiagnostico().getMecanico());
        verify(buscarMecanicoPorIdUseCase).execute(2L);
        verify(buscarUsuarioPorEmailUseCase, never()).execute(anyString());
    }

    @Test
    void deveAtribuirMecanicoMantendoDiagnosticoExistente() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        os.setDiagnostico(diagnostico);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(buscarMecanicoPorIdUseCase.execute(2L)).thenReturn(mecanico);
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
        when(buscarUsuarioPorEmailUseCase.execute("mecanico@autoflow.com")).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.atribuirMecanico("OS-123", null, "mecanico@autoflow.com");

        assertNotNull(resultado.getDiagnostico());
        assertEquals(mecanico, resultado.getDiagnostico().getMecanico());
        verify(buscarMecanicoPorIdUseCase, never()).execute(anyLong());
        verify(buscarUsuarioPorEmailUseCase).execute("mecanico@autoflow.com");
    }

    @Test
    void devePriorizarIdQuandoIdEEmailForemInformados() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(buscarMecanicoPorIdUseCase.execute(2L)).thenReturn(mecanico);
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.atribuirMecanico("OS-123", 2L, "outro@autoflow.com");

        assertEquals(mecanico, resultado.getDiagnostico().getMecanico());
        verify(buscarMecanicoPorIdUseCase).execute(2L);
        verify(buscarUsuarioPorEmailUseCase, never()).execute(anyString());
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
        verify(buscarMecanicoPorIdUseCase, never()).execute(anyLong());
        verify(buscarUsuarioPorEmailUseCase, never()).execute(anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarBadRequestQuandoIdEEmailDoMecanicoForemNulos() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.atribuirMecanico("OS-123", null, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(buscarMecanicoPorIdUseCase, buscarUsuarioPorEmailUseCase);
        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarBadRequestQuandoEmailNaoForDeMecanico() {
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        UsuarioEntity atendente = criarUsuario(3L, "Atendente", "atendente@autoflow.com", RoleEnum.ATENDENTE);

        when(repository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(buscarUsuarioPorEmailUseCase.execute("atendente@autoflow.com")).thenReturn(atendente);

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
        when(buscarUsuarioPorEmailUseCase.execute("admin@autoflow.com")).thenReturn(admin);
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
        when(buscarUsuarioPorEmailUseCase.execute("mecanico@autoflow.com")).thenReturn(mecanico);
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
        when(buscarUsuarioPorEmailUseCase.execute("mecanico@autoflow.com")).thenReturn(mecanico);
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
        ItemNecessarioEntity solicitado = criarItemNecessarioSolicitado(pecaInsumoId, 2);
        ItemNecessarioEntity itemComDisponibilidade = criarItemComDisponibilidade(estoque, 2);

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(buscarUsuarioPorEmailUseCase.execute(emailAdmin)).thenReturn(admin);
        when(consultarDisponibilidadeEstoqueUseCase.execute(List.of(solicitado)))
                .thenReturn(List.of(itemComDisponibilidade));
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.registrarItemNecessario(numeroOs,
                servicoOsId,
                emailAdmin,
                List.of(solicitado));

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
        ItemNecessarioEntity solicitado = criarItemNecessarioSolicitado(pecaInsumoId, 2);
        ItemNecessarioEntity itemComDisponibilidade = criarItemComDisponibilidade(estoque, 2);

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(buscarUsuarioPorEmailUseCase.execute(emailAdmin)).thenReturn(admin);
        when(consultarDisponibilidadeEstoqueUseCase.execute(List.of(solicitado)))
                .thenReturn(List.of(itemComDisponibilidade));
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.registrarItemNecessario(numeroOs,
                servicoOsId,
                emailAdmin,
                List.of(solicitado)
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
        when(buscarUsuarioPorEmailUseCase.execute(emailAdmin)).thenReturn(admin);
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
        ItemNecessarioEntity solicitado = criarItemNecessarioSolicitado(pecaInsumoId, 1);
        ItemNecessarioEntity itemComDisponibilidade = criarItemComDisponibilidade(estoque, 1);

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(buscarUsuarioPorEmailUseCase.execute(emailMecanico)).thenReturn(mecanico);
        when(consultarDisponibilidadeEstoqueUseCase.execute(List.of(solicitado)))
                .thenReturn(List.of(itemComDisponibilidade));
        when(repository.save(os)).thenReturn(os);

        service.registrarItemNecessario(numeroOs, servicoOsId, emailMecanico,
                List.of(solicitado));

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
        when(buscarUsuarioPorEmailUseCase.execute(emailAdmin)).thenReturn(admin);
        List<ItemNecessarioEntity> itensVazios = List.of();

        assertThrows(IllegalArgumentException.class,
                () -> service.registrarItemNecessario(numeroOs, servicoInexistenteId, emailAdmin, itensVazios));
    }

    @Test
    void naoDeveIniciarServicoAntesDaAprovacaoDoOrcamento() {
        String numeroOs = "OS-123";
        Long servicoOsId = 55L;
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        when(repository.findByNumeroOsForUpdate(numeroOs)).thenReturn(Optional.of(os));

        assertThrows(IllegalStateException.class, () -> service.iniciarServico(numeroOs, servicoOsId));

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, os.getStatus());
        assertEquals(StatusServicoOs.AGUARDANDO, os.buscarServicoSolicitado(servicoOsId).getStatus());
        verifyNoInteractions(baixarEstoqueUseCase);
        verify(repository, never()).save(any());
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

        when(repository.findByNumeroOsForUpdate(numeroOs)).thenReturn(Optional.of(os));
        when(baixarEstoqueUseCase.execute(List.of(itemOriginal)))
                .thenReturn(List.of(itemAtualizado));
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
        when(repository.findByNumeroOsForUpdate(numeroOs)).thenReturn(Optional.of(os));

        assertThrows(IllegalStateException.class, () -> service.iniciarServico(numeroOs, servicoOsId));

        verifyNoInteractions(baixarEstoqueUseCase);
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
    void deveFinalizarServicoSemFinalizarOsQuandoAindaHouverServicoPendente() {
        String numeroOs = "OS-123";
        Long servicoOsId = 55L;
        OrdemServicoEntity os = criarOrdemServicoComServico(numeroOs, servicoOsId);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        os.iniciarExecucao();
        os.buscarServicoSolicitado(servicoOsId).iniciar(List.of());
        os.adicionarServicosSolicitados(List.of(
                ServicoSolicitadoEntity.criar(56L, "Servico pendente", BigDecimal.TEN)
        ));

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(repository.save(os)).thenReturn(os);

        OrdemServicoEntity resultado = service.finalizarServico(numeroOs, servicoOsId);

        assertEquals(StatusOrdemServico.EM_EXECUCAO, resultado.getStatus());
        verify(historicoStatusOsRepository, never()).save(any());
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
        orcamento.setCliente(new ClienteOrcamentoSnapshot("Cliente", "123", "cliente@autoflow.com", null));

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(buscarUsuarioPorEmailUseCase.execute(emailAdmin)).thenReturn(admin);
        when(orcamentoVersioningGateway.proximaVersaoPorNumeroOs("OS-123", com.autoflow.domain.orcamento.TipoOrcamento.PRINCIPAL)).thenReturn(1);
        when(orcamentoFactoryImpl.criarPrincipalDisponivel(eq(os), eq(1), any())).thenReturn(orcamento);
        when(orcamentoGateway.save(orcamento)).thenAnswer(invocation -> {
            orcamento.setId(10L);
            return orcamento;
        });
        when(orcamentoPublicacaoGateway.publicar(10L))
                .thenReturn("http://localhost/orcamento");
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
        orcamento.setCliente(new ClienteOrcamentoSnapshot("Cliente", "123", "cliente@autoflow.com", null));

        when(repository.findByNumeroOs(numerOs)).thenReturn(Optional.of(os));
        when(buscarUsuarioPorEmailUseCase.execute(emailAdmin)).thenReturn(admin);
        when(orcamentoVersioningGateway.proximaVersaoPorNumeroOs(
                numerOs, com.autoflow.domain.orcamento.TipoOrcamento.PRINCIPAL)).thenReturn(1);
        when(orcamentoFactoryImpl.criarPrincipalDisponivel(eq(os), eq(1), any())).thenReturn(orcamento);
        when(orcamentoGateway.save(orcamento)).thenAnswer(invocation -> {
            orcamento.setId(10L);
            return orcamento;
        });
        when(orcamentoPublicacaoGateway.publicar(10L))
                .thenReturn("http://localhost/orcamento");
        doThrow(new RuntimeException("smtp indisponivel"))
                .when(orcamentoNotificacaoGateway)
                .notificar(any());
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
        orcamento.setCliente(new ClienteOrcamentoSnapshot("Cliente", "123", "cliente@autoflow.com", null));

        when(repository.findByNumeroOs(numeroOs)).thenReturn(Optional.of(os));
        when(buscarUsuarioPorEmailUseCase.execute(emailMecanico)).thenReturn(mecanico);
        when(orcamentoVersioningGateway.proximaVersaoPorNumeroOs("OS-123", com.autoflow.domain.orcamento.TipoOrcamento.PRINCIPAL)).thenReturn(1);
        when(orcamentoFactoryImpl.criarPrincipalDisponivel(eq(os), eq(1), any())).thenReturn(orcamento);
        when(orcamentoGateway.save(orcamento)).thenAnswer(invocation -> {
            orcamento.setId(10L);
            return orcamento;
        });
        when(orcamentoPublicacaoGateway.publicar(10L))
                .thenReturn("http://localhost/orcamento");
        when(repository.save(os)).thenReturn(os);

        FinalizarDiagnosticoResult resultado = service.finalizarDiagnostico(numeroOs, emailMecanico);

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, resultado.ordemServico().getStatus());
        verify(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
        verify(historicoStatusOsRepository).save(argThat(historico ->
                StatusOrdemServico.AGUARDANDO_APROVACAO.equals(historico.getStatus())
        ));
    }

    @Test
    void deveDelegarAcompanhamentoParaUseCaseEDevolverOutputInterno() {
        AcompanhamentoOrdemServicoOutput output = mock(AcompanhamentoOrdemServicoOutput.class);
        when(acompanharOrdemServicoUseCase.execute("cliente@autoflow.com"))
                .thenReturn(List.of(output));

        List<AcompanhamentoOrdemServicoOutput> resultado =
                service.listarAcompanhamentoCliente("cliente@autoflow.com");

        assertSame(output, resultado.getFirst());
        verify(acompanharOrdemServicoUseCase).execute("cliente@autoflow.com");
        verifyNoInteractions(repository, historicoStatusOsRepository);
    }

    @Test
    void deveListarOrdensServicoComFiltroVazio() {
        UsuarioEntity admin = criarUsuario(1L, "Admin", "admin@autoflow.com", RoleEnum.ADMIN);
        OrdemServicoEntity primeiraOrdem = criarOrdemServicoComServico("OS-123", 55L);
        OrdemServicoEntity segundaOrdem = criarOrdemServicoComServico("OS-456", 66L);
        PageImpl<OrdemServicoEntity> page = new PageImpl<>(List.of(primeiraOrdem, segundaOrdem));
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataAbertura"));

        when(buscarUsuarioPorEmailUseCase.execute("admin@autoflow.com")).thenReturn(admin);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<OrdemServicoEntity> resultado = service.listar(new OrdemServicoFiltro(null, null, null), pageable, "admin@autoflow.com");

        assertEquals(2, resultado.getTotalElements());
        assertEquals(List.of(primeiraOrdem, segundaOrdem), resultado.getContent());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void deveListarOrdensServicoFiltrandoPorStatus() {
        UsuarioEntity admin = criarUsuario(1L, "Admin", "admin@autoflow.com", RoleEnum.ADMIN);
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-789", 77L);
        PageImpl<OrdemServicoEntity> page = new PageImpl<>(List.of(os));
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataAbertura"));

        when(buscarUsuarioPorEmailUseCase.execute("admin@autoflow.com")).thenReturn(admin);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<OrdemServicoEntity> resultado = service.listar(
                new OrdemServicoFiltro(null, null, StatusOrdemServico.RECEBIDA), pageable, "admin@autoflow.com");

        assertEquals(1, resultado.getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void deveListarApenasOsAtribuidasAoMecanicoLogado() {
        UsuarioEntity mecanico = criarUsuario(2L, "Mecanico", "mecanico@autoflow.com", RoleEnum.MECANICO);
        OrdemServicoEntity os = criarOrdemServicoComServico("OS-123", 55L);
        PageImpl<OrdemServicoEntity> page = new PageImpl<>(List.of(os));
        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "dataAbertura"));

        when(buscarUsuarioPorEmailUseCase.execute("mecanico@autoflow.com")).thenReturn(mecanico);
        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<OrdemServicoEntity> resultado = service.listar(new OrdemServicoFiltro(null, null, null), pageable, "mecanico@autoflow.com");

        assertEquals(1, resultado.getTotalElements());
        verify(buscarUsuarioPorEmailUseCase).execute("mecanico@autoflow.com");
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void deveBuscarOrcamentoAtualDisponivel() {
        OrcamentoEntity orcamentoDisponivel = criarOrcamento(99L, "OS-123");
        when(orcamentoGateway.findByNumeroOsAndStatus(
                "OS-123",
                com.autoflow.domain.orcamento.StatusOrcamento.DISPONIVEL
        )).thenReturn(Optional.of(orcamentoDisponivel));

        OrcamentoEntity resultado = service.buscarOrcamentoAtual("OS-123");

        assertSame(orcamentoDisponivel, resultado);
        verify(orcamentoGateway).findByNumeroOsAndStatus(
                "OS-123",
                com.autoflow.domain.orcamento.StatusOrcamento.DISPONIVEL
        );
        verify(orcamentoGateway, never()).findTopByNumeroOsOrderByVersaoDesc(anyString());
    }

    @Test
    void deveBuscarUltimoOrcamentoQuandoNaoHouverDisponivel() {
        OrcamentoEntity ultimoOrcamento = criarOrcamento(100L, "OS-123");
        ultimoOrcamento.setStatus(com.autoflow.domain.orcamento.StatusOrcamento.APROVADO);

        when(orcamentoGateway.findByNumeroOsAndStatus(
                "OS-123",
                com.autoflow.domain.orcamento.StatusOrcamento.DISPONIVEL
        )).thenReturn(Optional.empty());
        when(orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc("OS-123"))
                .thenReturn(Optional.of(ultimoOrcamento));

        OrcamentoEntity resultado = service.buscarOrcamentoAtual("OS-123");

        assertSame(ultimoOrcamento, resultado);
        verify(orcamentoGateway).findTopByNumeroOsOrderByVersaoDesc("OS-123");
    }

    @Test
    void deveLancarNotFoundQuandoClienteAutenticadoNaoExistirNoAcompanhamento() {
        ResponseStatusException erro = new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Cliente autenticado nao encontrado.");
        when(acompanharOrdemServicoUseCase.execute("cliente@autoflow.com")).thenThrow(erro);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.listarAcompanhamentoCliente("cliente@autoflow.com")
        );

        assertSame(erro, exception);
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
        when(buscarUsuarioPorEmailUseCase.execute(email)).thenReturn(mecanico);
        doThrow(erro).when(ordemServicoAccessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
        List<ItemNecessarioEntity> itensVazios = List.of();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.registrarItemNecessario(numeroOs, servicoOsId, email, itensVazios)
        );

        assertEquals(erro, exception);
        verify(repository, never()).save(any());
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

    private ClienteOutput toClienteOutput(ClienteEntity cliente) {
        return ClienteOutput.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .cpfCnpj(cliente.getCpfCnpj())
                .telefone(cliente.getTelefone())
                .email(cliente.getEmail())
                .build();
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

    private VeiculoOrdemServicoInput toVeiculoInput(VeiculoOrdemServicoRequest request) {
        return new VeiculoOrdemServicoInput(
                request.placa(),
                request.marca(),
                request.modelo(),
                request.ano());
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

    private ItemNecessarioEntity criarItemComDisponibilidade(
            PecaInsumoEntity estoque,
            int quantidadeSolicitada
    ) {
        boolean disponivel = estoque.getQuantidade() >= quantidadeSolicitada;
        return ItemNecessarioEntity.criar(
                estoque.getId(),
                estoque.getNome(),
                estoque.getTipo(),
                estoque.getValor(),
                quantidadeSolicitada,
                disponivel ? StatusItemNecessario.DISPONIVEL : StatusItemNecessario.PENDENTE,
                new SituacaoEstoque(
                        estoque.getQuantidade(),
                        disponivel ? null : MotivoPendenciaItem.ESTOQUE_INSUFICIENTE
                )
        );
    }
}
