package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.security.AuthorizationService;
import com.autoflow.application.security.ClienteAutenticadoService;
import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.repository.ClienteRepository;
import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import com.autoflow.infrastructure.persistence.mapper.VeiculoMapper;
import com.autoflow.presentation.veiculo.request.VeiculoRequest;
import com.autoflow.presentation.veiculo.request.VeiculoUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class VeiculoUseCasesTest {

    @InjectMocks
    private CadastrarVeiculoUseCase cadastrarVeiculoUseCase;
    @InjectMocks
    private BuscarVeiculoUseCase buscarVeiculoUseCase;
    @InjectMocks
    private ListarVeiculosUseCase listarVeiculosUseCase;
    @InjectMocks
    private AtualizarVeiculoUseCase atualizarVeiculoUseCase;
    @InjectMocks
    private DeletarVeiculoUseCase deletarVeiculoUseCase;
    
    @InjectMocks
    private BuscarOuCadastrarVeiculoUseCase buscarOuCadastrarVeiculoUseCase;

    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private VeiculoMapper veiculoMapper;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private ClienteAutenticadoService clienteAutenticadoService;

    private VeiculoRequest cadastroRequest;
    private VeiculoUpdateRequest updateRequest;
    private ClienteEntity clienteEntity;
    private VeiculoEntity veiculoEntity;
    private VeiculoOutput veiculoOutput;
    private CadastrarVeiculoInput cadastrarVeiculoInput;
    private VeiculoInput veiculoInput;


    @BeforeEach
    void setup() {
        cadastroRequest = new VeiculoRequest("12345632451", "Honda", 2020, "HXS5345", "Civic");
        updateRequest = new VeiculoUpdateRequest("Honda", 2020, "HXS5345", "Civic");

        clienteEntity = new ClienteEntity();
        clienteEntity.setId(1L);

        veiculoEntity = new VeiculoEntity();
        veiculoEntity.setId(1L);
        veiculoEntity.setCliente(clienteEntity);
        veiculoEntity.setPlaca("HXS5345");

        veiculoOutput = new VeiculoOutput(1L, "HXS5345", "Honda", "Civic", 2020, null);
        cadastrarVeiculoInput = new CadastrarVeiculoInput("12345678901", "HXS5345", "Honda", "Civic", 2020);
        veiculoInput = new VeiculoInput("Honda", 2020, "HXS5345", "Civic");


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

    // ── CadastrarVeiculoUseCase ─────────────────────────────────────────────────────────────

    @Test
    void deveCadastrarVeiculoComSucesso() {
        when(clienteRepository.findByCpfCnpj(cadastrarVeiculoInput.cpfCnpj())).thenReturn(Optional.of(clienteEntity));
        when(veiculoMapper.mapToEntity(cadastrarVeiculoInput, clienteEntity)).thenReturn(veiculoEntity);
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToOutput(veiculoEntity)).thenReturn(veiculoOutput);

        VeiculoOutput resultado = cadastrarVeiculoUseCase.execute(cadastrarVeiculoInput);

        assertNotNull(resultado);
        verify(clienteRepository).findByCpfCnpj(cadastrarVeiculoInput.cpfCnpj());
        verify(veiculoRepository).save(veiculoEntity);
        verify(veiculoMapper).mapToOutput(veiculoEntity);
    }

    @Test
    void deveLancarConflictQuandoJaExistirVeiculoComMesmaPlaca() {
        when(clienteRepository.findByCpfCnpj(cadastrarVeiculoInput.cpfCnpj())).thenReturn(Optional.of(clienteEntity));
        when(veiculoRepository.existsByPlaca(cadastrarVeiculoInput.placa())).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cadastrarVeiculoUseCase.execute(cadastrarVeiculoInput));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoCadastrarVeiculoComClienteInexistente() {
        when(clienteRepository.findByCpfCnpj(cadastrarVeiculoInput.cpfCnpj())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cadastrarVeiculoUseCase.execute(cadastrarVeiculoInput));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }


    @Test
    void deveListarVeiculoPorIdComSucesso() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        doNothing().when(authorizationService).validarPermissao(veiculoEntity);
        when(veiculoMapper.mapToOutput(veiculoEntity)).thenReturn(veiculoOutput);

        VeiculoOutput resultado = buscarVeiculoUseCase.execute(1L);

        assertNotNull(resultado);
        verify(veiculoRepository).findById(1L);
        verify(authorizationService).validarPermissao(veiculoEntity);
        verify(veiculoMapper).mapToOutput(veiculoEntity);
    }

    @Test
    void deveLancarExcecaoAoListarIdInexistente() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> buscarVeiculoUseCase.execute(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(authorizationService);
    }

    @Test
    void devePermitirClienteVerSeuPropioVeiculoPorId() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("cliente@test.com");
        clienteEntity.setUsuario(usuario);

        autenticarComo("cliente@test.com", "ROLE_CLIENTE");

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        // Removed unnecessary stubbing: when(clienteRepository.findByUsuarioEmail("cliente@test.com")).thenReturn(Optional.of(clienteEntity));
        doNothing().when(authorizationService).validarPermissao(veiculoEntity);
        when(veiculoMapper.mapToOutput(veiculoEntity)).thenReturn(veiculoOutput);

        VeiculoOutput resultado = buscarVeiculoUseCase.execute(1L);

        assertNotNull(resultado);
        verify(veiculoRepository).findById(1L);
        // verify(clienteRepository).findByUsuarioEmail("cliente@test.com"); // Removed unnecessary verification
        verify(authorizationService).validarPermissao(veiculoEntity);
        verify(veiculoMapper).mapToOutput(veiculoEntity);
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
        // Removed unnecessary stubbing: when(clienteRepository.findByUsuarioEmail("cliente@test.com")).thenReturn(Optional.of(clienteEntity));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(authorizationService).validarPermissao(veiculoEntity);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> buscarVeiculoUseCase.execute(1L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(authorizationService).validarPermissao(veiculoEntity);
    }

    // ── ListarVeiculosUseCase ──────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void deveListarTodosQuandoFiltroVazioComoAdmin() {
        Pageable pageable = PageRequest.of(0, 20);
        VeiculoInput filtroVazio = new VeiculoInput(null, null, null, null);
        when(clienteAutenticadoService.getClienteId()).thenReturn(null);
        when(veiculoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(veiculoEntity)));
        when(veiculoMapper.mapToOutput(veiculoEntity)).thenReturn(veiculoOutput);

        var resultado = listarVeiculosUseCase.execute(filtroVazio, pageable);

        assertEquals(1, resultado.getContent().size());
        verify(clienteAutenticadoService).getClienteId();
        verify(veiculoRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(veiculoMapper).mapToOutput(veiculoEntity);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveRepassarFiltroAoRepositoryComoAdmin() {
        Pageable pageable = PageRequest.of(0, 20);
        VeiculoInput filtro = new VeiculoInput("ABC1234", null, null, "Honda");
        when(clienteAutenticadoService.getClienteId()).thenReturn(null);
        when(veiculoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        listarVeiculosUseCase.execute(filtro, pageable);

        verify(clienteAutenticadoService).getClienteId();
        verify(veiculoRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void deveAdicionarClienteIdImplicitoQuandoListarComoCliente() {
        Pageable pageable = PageRequest.of(0, 20);
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("cliente@test.com");
        clienteEntity.setUsuario(usuario);

        autenticarComo("cliente@test.com", "ROLE_CLIENTE");

        when(clienteAutenticadoService.getClienteId()).thenReturn(clienteEntity.getId());
        // Removed unnecessary stubbing: when(clienteRepository.findByUsuarioEmail("cliente@test.com")).thenReturn(Optional.of(clienteEntity));
        when(veiculoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(veiculoEntity)));
        when(veiculoMapper.mapToOutput(veiculoEntity)).thenReturn(veiculoOutput);

        listarVeiculosUseCase.execute(new VeiculoInput(null, null, null, null), pageable);

        verify(clienteAutenticadoService).getClienteId();
        verify(veiculoRepository).findAll(any(Specification.class), any(Pageable.class));
        verify(veiculoMapper).mapToOutput(veiculoEntity);
    }

    // ── AtualizarVeiculoUseCase ──────────────────────────────────

    @Test
    void deveLancarConflictQuandoAtualizarComPlacaJaCadastradaEmOutroVeiculo() {
        VeiculoEntity outroVeiculo = new VeiculoEntity();
        outroVeiculo.setId(2L);
        outroVeiculo.setCliente(clienteEntity);
        outroVeiculo.setPlaca(veiculoInput.placa()); // Use modelo as per the error message

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        doNothing().when(authorizationService).validarPermissao(veiculoEntity);
        when(veiculoRepository.findByPlaca(veiculoInput.placa())).thenReturn(Optional.of(outroVeiculo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> atualizarVeiculoUseCase.execute(1L, veiculoInput));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(authorizationService).validarPermissao(veiculoEntity);
        verify(veiculoRepository).findByPlaca(veiculoInput.placa());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveAtualizarQuandoPlacaPertencerAoMesmoVeiculo() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        doNothing().when(authorizationService).validarPermissao(veiculoEntity);
        when(veiculoRepository.findByPlaca(veiculoInput.placa())).thenReturn(Optional.of(veiculoEntity)); // Use modelo as per the error message
        Mockito.doNothing().when(veiculoMapper).updateEntity(veiculoInput, veiculoEntity);
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToOutput(veiculoEntity)).thenReturn(veiculoOutput);

        VeiculoOutput resultado = atualizarVeiculoUseCase.execute(1L, veiculoInput);

        assertNotNull(resultado);
        verify(authorizationService).validarPermissao(veiculoEntity);
        verify(veiculoRepository).findByPlaca(veiculoInput.placa());
        verify(veiculoRepository).save(veiculoEntity);
        verify(veiculoMapper).updateEntity(veiculoInput, veiculoEntity);
        verify(veiculoMapper).mapToOutput(veiculoEntity);
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> atualizarVeiculoUseCase.execute(1L, veiculoInput));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(authorizationService);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void devePermitirClienteAtualizarSeuProprioVeiculo() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("cliente@test.com");
        clienteEntity.setUsuario(usuario);

        autenticarComo("cliente@test.com", "ROLE_CLIENTE");

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        // Removed unnecessary stubbing: when(clienteRepository.findByUsuarioEmail("cliente@test.com")).thenReturn(Optional.of(clienteEntity));
        doNothing().when(authorizationService).validarPermissao(veiculoEntity);
        when(veiculoRepository.findByPlaca(veiculoInput.placa())).thenReturn(Optional.empty()); // Use modelo as per the error message
        Mockito.doNothing().when(veiculoMapper).updateEntity(veiculoInput, veiculoEntity);
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToOutput(veiculoEntity)).thenReturn(veiculoOutput);

        VeiculoOutput resultado = atualizarVeiculoUseCase.execute(1L, veiculoInput);

        assertNotNull(resultado);
        // verify(clienteRepository).findByUsuarioEmail("cliente@test.com"); // Removed unnecessary verification
        verify(authorizationService).validarPermissao(veiculoEntity);
        verify(veiculoRepository).findByPlaca(veiculoInput.placa());
        verify(veiculoRepository).save(veiculoEntity);
        verify(veiculoMapper).updateEntity(veiculoInput, veiculoEntity);
        verify(veiculoMapper).mapToOutput(veiculoEntity);
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
        // Removed unnecessary stubbing: when(clienteRepository.findByUsuarioEmail("cliente@test.com")).thenReturn(Optional.of(clienteEntity));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(authorizationService).validarPermissao(veiculoEntity);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> atualizarVeiculoUseCase.execute(1L, veiculoInput));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(authorizationService).validarPermissao(veiculoEntity);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveLancarForbiddenQuandoClienteAutenticadoNaoForEncontradoNoBanco() {
        autenticarComo("fantasma@test.com", "ROLE_CLIENTE");

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        // Removed unnecessary stubbing: when(clienteRepository.findByUsuarioEmail("fantasma@test.com")).thenReturn(Optional.empty());
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(authorizationService).validarPermissao(veiculoEntity);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> atualizarVeiculoUseCase.execute(1L, veiculoInput));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(authorizationService).validarPermissao(veiculoEntity);
    }

    @Test
    void devePermitirAdminAtualizarQualquerVeiculoSemVerificarDono() {
        ClienteEntity outroCliente = new ClienteEntity();
        outroCliente.setId(99L);
        veiculoEntity.setCliente(outroCliente);

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoEntity));
        doNothing().when(authorizationService).validarPermissao(veiculoEntity);
        when(veiculoRepository.findByPlaca(veiculoInput.placa())).thenReturn(Optional.empty()); // Use modelo as per the error message
        Mockito.doNothing().when(veiculoMapper).updateEntity(veiculoInput, veiculoEntity);
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToOutput(veiculoEntity)).thenReturn(veiculoOutput);

        VeiculoOutput resultado = atualizarVeiculoUseCase.execute(1L, veiculoInput);

        assertNotNull(resultado);
        verify(authorizationService).validarPermissao(veiculoEntity);
        verifyNoInteractions(clienteRepository);
        verify(veiculoRepository).findByPlaca(veiculoInput.placa());
        verify(veiculoRepository).save(veiculoEntity);
        verify(veiculoMapper).updateEntity(veiculoInput, veiculoEntity);
        verify(veiculoMapper).mapToOutput(veiculoEntity);
    }

    // ── DeletarVeiculoUseCase ───────────────────────────────────────────────────────────────

    @Test
    void deveDeletarVeiculoComSucesso() {
        when(veiculoRepository.existsById(1L)).thenReturn(true);

        deletarVeiculoUseCase.execute(1L);

        verify(veiculoRepository).existsById(1L);
        verify(veiculoRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoDeletarIdInexistente() {
        when(veiculoRepository.existsById(1L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> deletarVeiculoUseCase.execute(1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(veiculoRepository, never()).deleteById(any());
        verifyNoInteractions(authorizationService);
    }

    @Test
    void deveRetornarVeiculoExistenteQuandoPertencerAoCliente() {

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);

        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setCliente(cliente);
        veiculo.setPlaca("ABC1234");

        VeiculoOrdemServicoRequest request =
                new VeiculoOrdemServicoRequest(
                        "abc-1234",
                        null,
                        null,
                        null
                );

        when(veiculoRepository.findByPlaca("ABC1234"))
                .thenReturn(Optional.of(veiculo));

        VeiculoEntity resultado = buscarOuCadastrarVeiculoUseCase.execute(cliente, request);

        assertSame(veiculo, resultado);
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveLancarConflitoQuandoPlacaPertencerOutroCliente() {

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);

        ClienteEntity outroCliente = new ClienteEntity();
        outroCliente.setId(2L);

        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setCliente(outroCliente);

        VeiculoOrdemServicoRequest request =
                new VeiculoOrdemServicoRequest(
                        "ABC1234",
                        null,
                        null,
                        null
                );

        when(veiculoRepository.findByPlaca("ABC1234"))
                .thenReturn(Optional.of(veiculo));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> buscarOuCadastrarVeiculoUseCase.execute(cliente, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "409 CONFLICT \"Placa já cadastrada para outro cliente.\"",
                exception.getMessage()
        );

        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveLancarBadRequestQuandoDadosObrigatoriosNaoForemInformados() {

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);

        VeiculoOrdemServicoRequest request =
                new VeiculoOrdemServicoRequest(
                        "ABC1234",
                        "",
                        "",
                        null
                );

        when(veiculoRepository.findByPlaca("ABC1234"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> buscarOuCadastrarVeiculoUseCase.execute(cliente, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(
                "400 BAD_REQUEST \"Marca, modelo e ano são obrigatórios para cadastrar um novo veículo.\"",
                exception.getMessage()
        );

        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveCadastrarNovoVeiculoQuandoNaoExistir() {

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);

        VeiculoOrdemServicoRequest request =
                new VeiculoOrdemServicoRequest(
                        "abc-1234",
                        "Toyota",
                        "Corolla",
                        2023
                );

        VeiculoEntity salvo = new VeiculoEntity();
        salvo.setCliente(cliente);
        salvo.setPlaca("ABC1234");
        salvo.setMarca("Toyota");
        salvo.setModelo("Corolla");
        salvo.setAno(2023);

        when(veiculoRepository.findByPlaca("ABC1234"))
                .thenReturn(Optional.empty());

        when(veiculoRepository.save(any(VeiculoEntity.class)))
                .thenReturn(salvo);

        VeiculoEntity resultado = buscarOuCadastrarVeiculoUseCase.execute(cliente, request);

        assertEquals("ABC1234", resultado.getPlaca());
        assertEquals("Toyota", resultado.getMarca());
        assertEquals("Corolla", resultado.getModelo());
        assertEquals(2023, resultado.getAno());
        assertEquals(cliente, resultado.getCliente());

        verify(veiculoRepository).save(any(VeiculoEntity.class));
    }

    @Test
    void deveNormalizarPlacaAntesDeBuscarESalvar() {

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);

        VeiculoOrdemServicoRequest request =
                new VeiculoOrdemServicoRequest(
                        "abc-1@23 4",
                        "Honda",
                        "Civic",
                        2022
                );

        when(veiculoRepository.findByPlaca("ABC1234"))
                .thenReturn(Optional.empty());

        when(veiculoRepository.save(any(VeiculoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VeiculoEntity resultado = buscarOuCadastrarVeiculoUseCase.execute(cliente, request);

        assertEquals("ABC1234", resultado.getPlaca());

        verify(veiculoRepository).findByPlaca("ABC1234");
    }
}