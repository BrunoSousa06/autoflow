package com.autoflow.service.veiculo;

import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.controller.veiculo.request.VeiculoRequest;
import com.autoflow.controller.veiculo.request.VeiculoUpdateRequest;
import com.autoflow.controller.veiculo.response.VeiculoResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.mapper.VeiculoMapper;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.repository.veiculo.VeiculoRepository;
import com.autoflow.service.veiculo.dto.VeiculoFiltro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @InjectMocks
    private VeiculoService veiculoService;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private VeiculoMapper veiculoMapper;

    private VeiculoRequest cadastroRequest;
    private VeiculoUpdateRequest updateRequest;
    private ClienteEntity clienteEntity;
    private VeiculoEntity veiculoEntity;
    private VeiculoResponse response;

    @BeforeEach
    void setup() {
        cadastroRequest = new VeiculoRequest("12345632451", "Honda", 2020, "HXS5345", "Civic");
        updateRequest = new VeiculoUpdateRequest("Honda", 2020, "HXS5345", "Civic");

        clienteEntity = new ClienteEntity();
        clienteEntity.setId(1L);

        veiculoEntity = new VeiculoEntity();
        veiculoEntity.setId(1L);
        veiculoEntity.setCliente(clienteEntity);
        veiculoEntity.setPlaca("ABC1D23");

        response = new VeiculoResponse(1L, "Honda", 2020, "HXS5345", "Civic", null);

        autenticarComo("admin@test.com", "ROLE_ADMIN");
    }

    @AfterEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String email, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ── cadastrar ─────────────────────────────────────────────────────────────

    @Test
    void deveCadastrarVeiculoComSucesso() {
        when(clienteRepository.findByCpfCnpj(cadastroRequest.cpfCnpj())).thenReturn(Optional.of(clienteEntity));
        when(veiculoMapper.mapToEntity(cadastroRequest, clienteEntity)).thenReturn(veiculoEntity);
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToResponse(veiculoEntity)).thenReturn(response);

        VeiculoResponse resultado = veiculoService.cadastrar(cadastroRequest);

        assertNotNull(resultado);
        verify(clienteRepository).findByCpfCnpj(cadastroRequest.cpfCnpj());
        verify(veiculoRepository).save(veiculoEntity);
    }

    @Test
    void deveLancarConflictQuandoJaExistirVeiculoComMesmaPlaca() {
        when(clienteRepository.findByCpfCnpj(cadastroRequest.cpfCnpj())).thenReturn(Optional.of(clienteEntity));
        when(veiculoRepository.existsByPlaca(cadastroRequest.placa())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.cadastrar(cadastroRequest));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoCadastrarVeiculoComClienteInexistente() {
        when(clienteRepository.findByCpfCnpj(cadastroRequest.cpfCnpj())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.cadastrar(cadastroRequest));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }

    // ── listar ────────────────────────────────────────────────────────────────

    @Test
    void deveListarVeiculoPorIdComSucesso() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        when(veiculoMapper.mapToResponse(veiculoEntity)).thenReturn(response);

        VeiculoResponse resultado = veiculoService.listar(1L);

        assertNotNull(resultado);
        verify(veiculoRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoAoListarIdInexistente() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.listar(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // ── listarComFiltros ──────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void deveListarTodosQuandoFiltroVazioComoAdmin() {
        var filtroVazio = new VeiculoFiltro(null, null, null, null, null);
        when(veiculoRepository.findAll(any(Specification.class))).thenReturn(List.of(veiculoEntity));
        when(veiculoMapper.mapToList(List.of(veiculoEntity))).thenReturn(List.of(response));

        List<VeiculoResponse> resultado = veiculoService.listarComFiltros(filtroVazio);

        assertEquals(1, resultado.size());
        verify(veiculoRepository).findAll(any(Specification.class));
        verifyNoInteractions(clienteRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveRepassarFiltroAoRepositoryComoAdmin() {
        var filtro = new VeiculoFiltro("ABC1234", "Honda", null, null, null);
        when(veiculoRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(veiculoMapper.mapToList(List.of())).thenReturn(List.of());

        veiculoService.listarComFiltros(filtro);

        verify(veiculoRepository).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveAdicionarClienteIdImplicitoQuandoListarComoCliente() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("cliente@test.com");
        clienteEntity.setUsuario(usuario);

        autenticarComo("cliente@test.com", "ROLE_CLIENTE");

        when(clienteRepository.findByUsuarioEmail("cliente@test.com")).thenReturn(Optional.of(clienteEntity));
        when(veiculoRepository.findAll(any(Specification.class))).thenReturn(List.of(veiculoEntity));
        when(veiculoMapper.mapToList(List.of(veiculoEntity))).thenReturn(List.of(response));

        veiculoService.listarComFiltros(new VeiculoFiltro(null, null, null, null, null));

        verify(clienteRepository).findByUsuarioEmail("cliente@test.com");
        verify(veiculoRepository).findAll(any(Specification.class));
    }

    @Test
    void devePermitirClienteVerSeuPropioVeiculoPorId() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("cliente@test.com");
        clienteEntity.setUsuario(usuario);

        autenticarComo("cliente@test.com", "ROLE_CLIENTE");

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        when(clienteRepository.findByUsuarioEmail("cliente@test.com")).thenReturn(Optional.of(clienteEntity));
        when(veiculoMapper.mapToResponse(veiculoEntity)).thenReturn(response);

        VeiculoResponse resultado = veiculoService.listar(1L);

        assertNotNull(resultado);
    }

    @Test
    void deveLancarForbiddenQuandoClienteTentarVerVeiculoDeOutroClientePorId() {
        ClienteEntity outroCliente = new ClienteEntity();
        outroCliente.setId(99L);
        veiculoEntity.setCliente(outroCliente);

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("cliente@test.com");
        clienteEntity.setUsuario(usuario);

        autenticarComo("cliente@test.com", "ROLE_CLIENTE");

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        when(clienteRepository.findByUsuarioEmail("cliente@test.com")).thenReturn(Optional.of(clienteEntity));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.listar(1L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ── atualizar — regra de placa duplicada ──────────────────────────────────

    @Test
    void deveLancarConflictQuandoAtualizarComPlacaJaCadastradaEmOutroVeiculo() {
        VeiculoEntity outroVeiculo = new VeiculoEntity();
        outroVeiculo.setId(2L);
        outroVeiculo.setCliente(clienteEntity);

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        when(veiculoRepository.findByPlaca(updateRequest.placa())).thenReturn(Optional.of(outroVeiculo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.atualizar(updateRequest, 1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveAtualizarQuandoPlacaPertencerAoMesmoVeiculo() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        when(veiculoRepository.findByPlaca(updateRequest.placa())).thenReturn(Optional.of(veiculoEntity));
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToResponse(veiculoEntity)).thenReturn(response);

        VeiculoResponse resultado = veiculoService.atualizar(updateRequest, 1L);

        assertNotNull(resultado);
        verify(veiculoRepository).save(veiculoEntity);
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.atualizar(updateRequest, 1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }

    // ── atualizar — verificação de dono (CLIENTE) ─────────────────────────────

    @Test
    void devePermitirClienteAtualizarSeuProprioVeiculo() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("cliente@test.com");
        clienteEntity.setUsuario(usuario);

        autenticarComo("cliente@test.com", "ROLE_CLIENTE");

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        when(clienteRepository.findByUsuarioEmail("cliente@test.com")).thenReturn(Optional.of(clienteEntity));
        when(veiculoRepository.findByPlaca(updateRequest.placa())).thenReturn(Optional.empty());
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToResponse(veiculoEntity)).thenReturn(response);

        VeiculoResponse resultado = veiculoService.atualizar(updateRequest, 1L);

        assertNotNull(resultado);
        verify(clienteRepository).findByUsuarioEmail("cliente@test.com");
    }

    @Test
    void deveLancarForbiddenQuandoClienteTentarAtualizarVeiculoDeOutroCliente() {
        ClienteEntity outroCliente = new ClienteEntity();
        outroCliente.setId(99L);
        veiculoEntity.setCliente(outroCliente);

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("cliente@test.com");
        clienteEntity.setUsuario(usuario);

        autenticarComo("cliente@test.com", "ROLE_CLIENTE");

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        when(clienteRepository.findByUsuarioEmail("cliente@test.com")).thenReturn(Optional.of(clienteEntity));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.atualizar(updateRequest, 1L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveLancarForbiddenQuandoClienteAutenticadoNaoForEncontradoNoBanco() {
        autenticarComo("fantasma@test.com", "ROLE_CLIENTE");

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        when(clienteRepository.findByUsuarioEmail("fantasma@test.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.atualizar(updateRequest, 1L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void devePermitirAdminAtualizarQualquerVeiculoSemVerificarDono() {
        ClienteEntity outroCliente = new ClienteEntity();
        outroCliente.setId(99L);
        veiculoEntity.setCliente(outroCliente);

        // admin já configurado no @BeforeEach
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        when(veiculoRepository.findByPlaca(updateRequest.placa())).thenReturn(Optional.empty());
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToResponse(veiculoEntity)).thenReturn(response);

        VeiculoResponse resultado = veiculoService.atualizar(updateRequest, 1L);

        assertNotNull(resultado);
        verifyNoInteractions(clienteRepository);
    }

    // ── deletar ───────────────────────────────────────────────────────────────

    @Test
    void deveDeletarVeiculoComSucesso() {
        when(veiculoRepository.existsById(1L)).thenReturn(true);

        veiculoService.deletar(1L);

        verify(veiculoRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoDeletarIdInexistente() {
        when(veiculoRepository.existsById(1L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.deletar(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(veiculoRepository, never()).deleteById(any());
    }

    // ── buscarOuCadastrarPorPlacaParaCliente ──────────────────────────────────

    @Test
    void deveUsarVeiculoExistentePorPlacaQuandoPertencerAoCliente() {
        VeiculoOrdemServicoRequest requestOs = new VeiculoOrdemServicoRequest("abc-1d23", null, null, null);
        when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculoEntity));

        VeiculoEntity resultado = veiculoService.buscarOuCadastrarPorPlacaParaCliente(clienteEntity, requestOs);

        assertEquals(veiculoEntity, resultado);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveCadastrarVeiculoQuandoPlacaNaoExistir() {
        VeiculoOrdemServicoRequest requestOs = new VeiculoOrdemServicoRequest("abc-1d23", "Honda", "Civic", 2020);
        when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.empty());
        when(veiculoRepository.save(any(VeiculoEntity.class))).thenAnswer(i -> i.getArgument(0));

        VeiculoEntity resultado = veiculoService.buscarOuCadastrarPorPlacaParaCliente(clienteEntity, requestOs);

        assertEquals("ABC1D23", resultado.getPlaca());
        assertEquals(clienteEntity, resultado.getCliente());
        verify(veiculoRepository).save(any(VeiculoEntity.class));
    }

    @Test
    void deveRetornarConflictQuandoPlacaPertencerAOutroCliente() {
        ClienteEntity outroCliente = new ClienteEntity();
        outroCliente.setId(99L);
        veiculoEntity.setCliente(outroCliente);

        VeiculoOrdemServicoRequest requestOs = new VeiculoOrdemServicoRequest("ABC1D23", "Honda", "Civic", 2020);
        when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculoEntity));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.buscarOuCadastrarPorPlacaParaCliente(clienteEntity, requestOs));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveRetornarBadRequestQuandoCadastrarVeiculoNovoSemDadosObrigatorios() {
        VeiculoOrdemServicoRequest requestOs = new VeiculoOrdemServicoRequest("ABC1D23", null, "Civic", 2020);
        when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> veiculoService.buscarOuCadastrarPorPlacaParaCliente(clienteEntity, requestOs));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }
}