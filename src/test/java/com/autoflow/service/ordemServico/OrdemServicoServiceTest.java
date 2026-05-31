package com.autoflow.service.ordemServico;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.ordemServico.DiagnosticoEntity;
import com.autoflow.domain.ordemServico.ItemNecessarioEntity;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemServico.StatusItemNecessario;
import com.autoflow.domain.ordemServico.StatusOrdemServico;
import com.autoflow.domain.pecaInsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecaInsumo.PecaInsumoEntity;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.repository.ordemServico.OrdemServicoRepository;
import com.autoflow.service.cliente.ClienteService;
import com.autoflow.service.pecaInsumo.PecaInsumoService;
import com.autoflow.service.servico.ServicoService;
import com.autoflow.service.usuario.UsuarioService;
import com.autoflow.service.veiculo.VeiculoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.lang.Long;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @InjectMocks
    OrdemServicoService service;

    @Mock
    OrdemServicoRepository repository;

    @Mock
    ClienteService clienteService;

    @Mock
    VeiculoService veiculoService;

    @Mock
    ServicoService servicoService;

    @Mock
    UsuarioService usuarioService;

    @Mock
    PecaInsumoService pecaInsumoService;

    @Test
    void deveCriarESalvarOrdemServico() {
        Long clienteId = 1L;
        Long veiculoId = 1L;
        Long servicoId = 1L;
        BigDecimal valor = new BigDecimal("100.00");
        ClienteEntity cliente = criarCliente(clienteId);
        VeiculoEntity veiculo = criarVeiculo(veiculoId, cliente);
        ServicoSolicitadoEntity servicoSolicitado = new ServicoSolicitadoEntity(servicoId);
        ServicoSolicitadoEntity servicoComDados = new ServicoSolicitadoEntity(servicoId, "Revisao", valor);
        ServicoEntity servico = criarServico(servicoId, "Revisao", valor);

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(veiculoService.buscarPorId(veiculoId)).thenReturn(veiculo);
        when(servicoService.buscarEntityPorId(servicoId)).thenReturn(servico);
        when(repository.save(any(OrdemServicoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoEntity ordemServicoEntity = service.criar(clienteId, veiculoId, List.of(servicoSolicitado));

        assertEquals(clienteId, ordemServicoEntity.getClienteId());
        assertEquals(veiculoId, ordemServicoEntity.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, ordemServicoEntity.getStatus());
        assertTrue(ordemServicoEntity.getNumeroOs().startsWith("OS-"));
        assertNotNull(ordemServicoEntity.getDataAbertura());
        assertEquals(List.of(servicoComDados), ordemServicoEntity.getServicosSolicitados());

        ArgumentCaptor<OrdemServicoEntity> captor = ArgumentCaptor.forClass(OrdemServicoEntity.class);
        verify(repository).save(captor.capture());
        OrdemServicoEntity ordemServicoSalva = captor.getValue();

        assertEquals(clienteId, ordemServicoSalva.getClienteId());
        assertEquals(veiculoId, ordemServicoSalva.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, ordemServicoSalva.getStatus());
        assertTrue(ordemServicoSalva.getNumeroOs().startsWith("OS-"));
        assertNotNull(ordemServicoSalva.getDataAbertura());
        assertEquals(List.of(servicoComDados), ordemServicoSalva.getServicosSolicitados());
        verify(clienteService).buscarPorId(clienteId);
        verify(veiculoService).buscarPorId(veiculoId);
        verify(servicoService).buscarEntityPorId(servicoId);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {
        Long clienteId = 1L;
        Long veiculoId = 1L;
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Revisao");
        IllegalArgumentException exception = new IllegalArgumentException("Cliente nao encontrado");

        when(clienteService.buscarPorId(clienteId)).thenThrow(exception);

        IllegalArgumentException resultado = assertThrows(
                IllegalArgumentException.class,
                () -> service.criar(clienteId, veiculoId, List.of(servico))
        );

        assertEquals(exception, resultado);
        verify(clienteService).buscarPorId(clienteId);
        verify(veiculoService, never()).buscarPorId(veiculoId);
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoVeiculoNaoForEncontrado() {
        Long clienteId = 1L;
        Long veiculoId = 1L;
        ClienteEntity cliente = criarCliente(clienteId);
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Revisao");
        IllegalArgumentException exception = new IllegalArgumentException("Veiculo nao encontrado");

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(veiculoService.buscarPorId(veiculoId)).thenThrow(exception);

        IllegalArgumentException resultado = assertThrows(
                IllegalArgumentException.class,
                () -> service.criar(clienteId, veiculoId, List.of(servico))
        );

        assertEquals(exception, resultado);
        verify(clienteService).buscarPorId(clienteId);
        verify(veiculoService).buscarPorId(veiculoId);
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoListaDeServicosEstiverVazia() {
        Long clienteId = 1L;
        Long veiculoId = 1L;
        ClienteEntity cliente = criarCliente(clienteId);
        VeiculoEntity veiculo = criarVeiculo(veiculoId, cliente);

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(veiculoService.buscarPorId(veiculoId)).thenReturn(veiculo);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.criar(clienteId, veiculoId, List.of())
        );

        assertEquals("A ordem de servico deve ter ao menos um servico solicitado.", exception.getMessage());
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoListaDeServicosForNula() {
        Long clienteId = 1L;
        Long veiculoId = 1L;
        ClienteEntity cliente = criarCliente(clienteId);
        VeiculoEntity veiculo = criarVeiculo(veiculoId, cliente);

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(veiculoService.buscarPorId(veiculoId)).thenReturn(veiculo);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.criar(clienteId, veiculoId, null)
        );

        assertEquals("A ordem de servico deve ter ao menos um servico solicitado.", exception.getMessage());
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoVeiculoNaoPertencerAoCliente() {
        Long clienteId = 1L;
        Long veiculoId = 1L;
        ClienteEntity cliente = criarCliente(clienteId);
        ClienteEntity outroCliente = criarCliente(2L);
        VeiculoEntity veiculo = criarVeiculo(veiculoId, outroCliente);
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Revisao");

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(veiculoService.buscarPorId(veiculoId)).thenReturn(veiculo);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.criar(clienteId, veiculoId, List.of(servico))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Veiculo nao pertence ao cliente informado.", exception.getReason());
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveIncluirServicosNaOrdemServico() {
        Long ordemServicoId = 1L;
        Long novoServicoId = 2L;
        BigDecimal valor = new BigDecimal("180.00");
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        ServicoSolicitadoEntity servicoInicial = new ServicoSolicitadoEntity(1L, "Revisao");
        ServicoSolicitadoEntity novoServicoSolicitado = new ServicoSolicitadoEntity(novoServicoId);
        ServicoSolicitadoEntity novoServicoComDados = new ServicoSolicitadoEntity(novoServicoId, "Troca de oleo", valor);
        ServicoEntity servico = criarServico(novoServicoId, "Troca de oleo", valor);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(servicoInicial)
        );

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(servicoService.buscarEntityPorId(novoServicoId)).thenReturn(servico);
        when(repository.save(ordemServicoEntity)).thenReturn(ordemServicoEntity);

        OrdemServicoEntity resultado = service.incluirServicos(ordemServicoId, List.of(novoServicoSolicitado));

        assertEquals(ordemServicoEntity, resultado);
        assertEquals(List.of(servicoInicial, novoServicoComDados), resultado.getServicosSolicitados());
        verify(repository).findById(ordemServicoId);
        verify(servicoService).buscarEntityPorId(novoServicoId);
        verify(repository).save(ordemServicoEntity);
    }

    @Test
    void deveLancarExcecaoQuandoOrdemServicoNaoForEncontradaAoIncluirServicos() {
        Long ordemServicoId = 1L;
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Revisao");

        when(repository.findById(ordemServicoId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.incluirServicos(ordemServicoId, List.of(servico))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository).findById(ordemServicoId);
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoIncluirListaDeServicosVazia() {
        Long ordemServicoId = 1L;
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.incluirServicos(ordemServicoId, List.of())
        );

        assertEquals("A ordem de servico deve ter ao menos um servico solicitado.", exception.getMessage());
        verify(repository).findById(ordemServicoId);
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoIncluirListaDeServicosNula() {
        Long ordemServicoId = 1L;
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.incluirServicos(ordemServicoId, null)
        );

        assertEquals("A ordem de servico deve ter ao menos um servico solicitado.", exception.getMessage());
        verify(repository).findById(ordemServicoId);
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveAtribuirMecanicoNaOrdemServico() {
        Long ordemServicoId = 1L;
        Long mecanicoId = 2L;
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        UsuarioEntity mecanico = criarUsuarioMecanico(mecanicoId, "Maria");
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(usuarioService.buscarMecanicoPorId(mecanicoId)).thenReturn(mecanico);
        when(repository.save(ordemServicoEntity)).thenReturn(ordemServicoEntity);

        OrdemServicoEntity resultado = service.atribuirMecanico(ordemServicoId, mecanicoId);

        assertEquals(ordemServicoEntity, resultado);
        assertNotNull(resultado.getDiagnostico());
        assertEquals(mecanico, resultado.getDiagnostico().getMecanico());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService).buscarMecanicoPorId(mecanicoId);
        verify(repository).save(ordemServicoEntity);
    }

    @Test
    void deveAtualizarMecanicoQuandoOrdemServicoJaPossuiDiagnostico() {
        Long ordemServicoId = 1L;
        Long mecanicoId = 3L;
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        UsuarioEntity mecanicoAntigo = criarUsuarioMecanico(2L, "Joao");
        UsuarioEntity novoMecanico = criarUsuarioMecanico(mecanicoId, "Maria");
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );
        DiagnosticoEntity diagnosticoExistente = new DiagnosticoEntity();
        diagnosticoExistente.setMecanico(mecanicoAntigo);
        diagnosticoExistente.setLaudo("Laudo inicial");
        ordemServicoEntity.setDiagnostico(diagnosticoExistente);

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(usuarioService.buscarMecanicoPorId(mecanicoId)).thenReturn(novoMecanico);
        when(repository.save(ordemServicoEntity)).thenReturn(ordemServicoEntity);

        OrdemServicoEntity resultado = service.atribuirMecanico(ordemServicoId, mecanicoId);

        assertEquals(ordemServicoEntity, resultado);
        assertEquals(diagnosticoExistente, resultado.getDiagnostico());
        assertEquals(novoMecanico, resultado.getDiagnostico().getMecanico());
        assertEquals("Laudo inicial", resultado.getDiagnostico().getLaudo());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService).buscarMecanicoPorId(mecanicoId);
        verify(repository).save(ordemServicoEntity);
    }

    @Test
    void devePropagarExcecaoQuandoMecanicoNaoForValidoAoAtribuirMecanico() {
        Long ordemServicoId = 1L;
        Long mecanicoId = 2L;
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );
        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Usuario informado nao e um mecanico."
        );

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(usuarioService.buscarMecanicoPorId(mecanicoId)).thenThrow(exception);

        ResponseStatusException resultado = assertThrows(
                ResponseStatusException.class,
                () -> service.atribuirMecanico(ordemServicoId, mecanicoId)
        );

        assertEquals(exception, resultado);
        verify(repository).findById(ordemServicoId);
        verify(usuarioService).buscarMecanicoPorId(mecanicoId);
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoOrdemServicoNaoForEncontradaAoAtribuirMecanico() {
        Long ordemServicoId = 1L;
        Long mecanicoId = 2L;

        when(repository.findById(ordemServicoId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.atribuirMecanico(ordemServicoId, mecanicoId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService, never()).buscarMecanicoPorId(any());
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveIniciarDiagnosticoQuandoUsuarioLogadoForAdmin() {
        Long ordemServicoId = 1L;
        String emailAdmin = "admin@autoflow.com";
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        UsuarioEntity mecanico = criarUsuarioMecanico(2L, "Maria");
        UsuarioEntity admin = criarUsuario(3L, "Admin", emailAdmin, RoleEnum.ROLE_ADMIN);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(mecanico);
        ordemServicoEntity.setDiagnostico(diagnostico);

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(usuarioService.buscarPorEmail(emailAdmin)).thenReturn(admin);
        when(repository.save(ordemServicoEntity)).thenReturn(ordemServicoEntity);

        OrdemServicoEntity resultado = service.iniciarDiagnostico(ordemServicoId, emailAdmin);

        assertEquals(ordemServicoEntity, resultado);
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, resultado.getStatus());
        assertNotNull(resultado.getDiagnostico().getIniciadoEm());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService).buscarPorEmail(emailAdmin);
        verify(repository).save(ordemServicoEntity);
    }

    @Test
    void deveIniciarDiagnosticoQuandoUsuarioLogadoForMecanicoAtribuido() {
        Long ordemServicoId = 1L;
        String emailMecanico = "maria@autoflow.com";
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        UsuarioEntity mecanico = criarUsuario(2L, "Maria", emailMecanico, RoleEnum.ROLE_MECANICO);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(mecanico);
        ordemServicoEntity.setDiagnostico(diagnostico);

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(usuarioService.buscarPorEmail(emailMecanico)).thenReturn(mecanico);
        when(repository.save(ordemServicoEntity)).thenReturn(ordemServicoEntity);

        OrdemServicoEntity resultado = service.iniciarDiagnostico(ordemServicoId, emailMecanico);

        assertEquals(ordemServicoEntity, resultado);
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, resultado.getStatus());
        assertNotNull(resultado.getDiagnostico().getIniciadoEm());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService).buscarPorEmail(emailMecanico);
        verify(repository).save(ordemServicoEntity);
    }

    @Test
    void deveLancarForbiddenQuandoUsuarioLogadoNaoForMecanicoAtribuido() {
        Long ordemServicoId = 1L;
        String emailMecanicoLogado = "joao@autoflow.com";
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        UsuarioEntity mecanicoAtribuido = criarUsuarioMecanico(2L, "Maria");
        UsuarioEntity mecanicoLogado = criarUsuario(3L, "Joao", emailMecanicoLogado, RoleEnum.ROLE_MECANICO);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(mecanicoAtribuido);
        ordemServicoEntity.setDiagnostico(diagnostico);

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(usuarioService.buscarPorEmail(emailMecanicoLogado)).thenReturn(mecanicoLogado);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.iniciarDiagnostico(ordemServicoId, emailMecanicoLogado)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService).buscarPorEmail(emailMecanicoLogado);
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveLancarBadRequestQuandoIniciarDiagnosticoSemMecanicoAtribuido() {
        Long ordemServicoId = 1L;
        String emailMecanico = "maria@autoflow.com";
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        UsuarioEntity mecanico = criarUsuario(2L, "Maria", emailMecanico, RoleEnum.ROLE_MECANICO);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(usuarioService.buscarPorEmail(emailMecanico)).thenReturn(mecanico);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.iniciarDiagnostico(ordemServicoId, emailMecanico)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService).buscarPorEmail(emailMecanico);
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoOrdemServicoNaoForEncontradaAoIniciarDiagnostico() {
        Long ordemServicoId = 1L;
        String emailMecanico = "maria@autoflow.com";

        when(repository.findById(ordemServicoId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.iniciarDiagnostico(ordemServicoId, emailMecanico)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService, never()).buscarPorEmail(any());
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    @Test
    void deveRegistrarItemNecessarioDisponivelQuandoHouverEstoque() {
        Long ordemServicoId = 1L;
        String emailAdmin = "admin@autoflow.com";
        Long pecaInsumoId = 10L;
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        UsuarioEntity admin = criarUsuario(3L, "Admin", emailAdmin, RoleEnum.ROLE_ADMIN);
        PecaInsumoEntity itemEstoque = criarPecaInsumo(
                pecaInsumoId,
                "Filtro de Oleo",
                CategoriaPecaInsumo.PECA,
                new BigDecimal("50.00"),
                5
        );
        ItemNecessarioEntity itemSolicitado = criarItemNecessarioSolicitado(pecaInsumoId, 2);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(usuarioService.buscarPorEmail(emailAdmin)).thenReturn(admin);
        when(pecaInsumoService.buscarEntityPorId(pecaInsumoId)).thenReturn(itemEstoque);
        when(repository.save(ordemServicoEntity)).thenReturn(ordemServicoEntity);

        OrdemServicoEntity resultado = service.registrarItemNecessario(
                ordemServicoId,
                emailAdmin,
                List.of(itemSolicitado)
        );

        assertEquals(ordemServicoEntity, resultado);
        assertEquals(1, resultado.getItemNecessario().size());
        ItemNecessarioEntity itemRegistrado = resultado.getItemNecessario().getFirst();
        assertEquals(pecaInsumoId, itemRegistrado.getPecaInsumoId());
        assertEquals("Filtro de Oleo", itemRegistrado.getNome());
        assertEquals(CategoriaPecaInsumo.PECA, itemRegistrado.getTipo());
        assertEquals(new BigDecimal("50.00"), itemRegistrado.getValorUnitario());
        assertEquals(2, itemRegistrado.getQuantidade());
        assertEquals(new BigDecimal("100.00"), itemRegistrado.getValorTotal());
        assertEquals(StatusItemNecessario.DISPONIVEL, itemRegistrado.getStatus());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService).buscarPorEmail(emailAdmin);
        verify(pecaInsumoService).buscarEntityPorId(pecaInsumoId);
        verify(repository).save(ordemServicoEntity);
    }

    @Test
    void deveRegistrarItemNecessarioPendenteQuandoNaoHouverEstoque() {
        Long ordemServicoId = 1L;
        String emailMecanico = "maria@autoflow.com";
        Long pecaInsumoId = 10L;
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        UsuarioEntity mecanico = criarUsuario(2L, "Maria", emailMecanico, RoleEnum.ROLE_MECANICO);
        PecaInsumoEntity itemEstoque = criarPecaInsumo(
                pecaInsumoId,
                "Oleo 5W30",
                CategoriaPecaInsumo.INSUMO,
                new BigDecimal("49.90"),
                1
        );
        ItemNecessarioEntity itemSolicitado = criarItemNecessarioSolicitado(pecaInsumoId, 3);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(mecanico);
        ordemServicoEntity.setDiagnostico(diagnostico);

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(usuarioService.buscarPorEmail(emailMecanico)).thenReturn(mecanico);
        when(pecaInsumoService.buscarEntityPorId(pecaInsumoId)).thenReturn(itemEstoque);
        when(repository.save(ordemServicoEntity)).thenReturn(ordemServicoEntity);

        OrdemServicoEntity resultado = service.registrarItemNecessario(
                ordemServicoId,
                emailMecanico,
                List.of(itemSolicitado)
        );

        assertEquals(ordemServicoEntity, resultado);
        assertEquals(1, resultado.getItemNecessario().size());
        ItemNecessarioEntity itemRegistrado = resultado.getItemNecessario().getFirst();
        assertEquals(pecaInsumoId, itemRegistrado.getPecaInsumoId());
        assertEquals("Oleo 5W30", itemRegistrado.getNome());
        assertEquals(CategoriaPecaInsumo.INSUMO, itemRegistrado.getTipo());
        assertEquals(new BigDecimal("49.90"), itemRegistrado.getValorUnitario());
        assertEquals(3, itemRegistrado.getQuantidade());
        assertEquals(new BigDecimal("149.70"), itemRegistrado.getValorTotal());
        assertEquals(StatusItemNecessario.PENDENTE, itemRegistrado.getStatus());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService).buscarPorEmail(emailMecanico);
        verify(pecaInsumoService).buscarEntityPorId(pecaInsumoId);
        verify(repository).save(ordemServicoEntity);
    }

    @Test
    void deveLancarForbiddenQuandoMecanicoNaoAtribuidoRegistrarItemNecessario() {
        Long ordemServicoId = 1L;
        String emailMecanicoLogado = "joao@autoflow.com";
        Long pecaInsumoId = 10L;
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        UsuarioEntity mecanicoAtribuido = criarUsuarioMecanico(2L, "Maria");
        UsuarioEntity mecanicoLogado = criarUsuario(3L, "Joao", emailMecanicoLogado, RoleEnum.ROLE_MECANICO);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(new ServicoSolicitadoEntity(1L, "Revisao"))
        );
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(mecanicoAtribuido);
        ordemServicoEntity.setDiagnostico(diagnostico);

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));
        when(usuarioService.buscarPorEmail(emailMecanicoLogado)).thenReturn(mecanicoLogado);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.registrarItemNecessario(
                        ordemServicoId,
                        emailMecanicoLogado,
                        List.of(criarItemNecessarioSolicitado(pecaInsumoId, 1))
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(repository).findById(ordemServicoId);
        verify(usuarioService).buscarPorEmail(emailMecanicoLogado);
        verify(pecaInsumoService, never()).buscarEntityPorId(any());
        verify(repository, never()).save(any(OrdemServicoEntity.class));
    }

    private ClienteEntity criarCliente(Long clienteId) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(clienteId);
        cliente.setCpfCnpj("1223321123");
        cliente.setEmail("email");
        cliente.setNome("descricao");
        return cliente;
    }

    private VeiculoEntity criarVeiculo(Long veiculoId, ClienteEntity cliente) {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(veiculoId);
        veiculo.setAno(2014L);
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

    private UsuarioEntity criarUsuarioMecanico(Long usuarioId, String nome) {
        return criarUsuario(usuarioId, nome, nome.toLowerCase() + "@autoflow.com", RoleEnum.ROLE_MECANICO);
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
