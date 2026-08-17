package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.*;
import com.autoflow.application.exception.*;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.security.AuthorizationService;
import com.autoflow.application.security.ClienteAutenticadoService;
import com.autoflow.application.usecases.ordemservico.BuscarOuCadastrarVeiculoForOrdemServicoUseCase;
import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import com.autoflow.application.dto.cliente.ClienteOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoUseCasesTest {

    @InjectMocks
    private CadastrarVeiculoUseCaseImpl cadastrarVeiculoUseCase;
    @InjectMocks
    private BuscarVeiculoUseCaseImpl buscarVeiculoUseCase;
    @InjectMocks
    private ListarVeiculosUseCaseImpl listarVeiculosUseCase;
    @InjectMocks
    private AtualizarVeiculoUseCaseImpl atualizarVeiculoUseCase;
    @InjectMocks
    private DeletarVeiculoUseCaseImpl deletarVeiculoUseCase;
    @InjectMocks
    private BuscarOuCadastrarVeiculoUseCaseImpl buscarOuCadastrarVeiculoUseCase;
    @InjectMocks
    private BuscarOuCadastrarVeiculoForOrdemServicoUseCase buscarOuCadastrarVeiculoService;

    @Mock
    private VeiculoGateway veiculoGateway;
    @Mock
    private VeiculoClienteGateway clienteGateway;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private ClienteAutenticadoService clienteAutenticadoService;
    @Mock
    private VeiculoRepository veiculoRepository;

    private final CadastrarVeiculoInput cadastro =
            new CadastrarVeiculoInput("12345678901", "ABC-1234", "Honda", "Civic", 2020);
    private final VeiculoInput atualizacao =
            new VeiculoInput("Honda", 2021, "ABC-1234", "Civic Touring");
    private final VeiculoOutput output =
            new VeiculoOutput(1L, "ABC1234", "Honda", "Civic", 2020, 10L);

    @Test
    void deveCadastrarVeiculoComPlacaNormalizada() {
        when(clienteGateway.findIdByCpfCnpj(cadastro.cpfCnpj())).thenReturn(Optional.of(10L));
        when(veiculoGateway.existsByPlaca("ABC1234")).thenReturn(false);
        when(veiculoGateway.save(any(CadastrarVeiculoInput.class), org.mockito.ArgumentMatchers.eq(10L)))
                .thenReturn(output);

        VeiculoOutput resultado = cadastrarVeiculoUseCase.execute(cadastro);

        assertEquals(output, resultado);
        verify(veiculoGateway).existsByPlaca("ABC1234");
        verify(veiculoGateway).save(any(CadastrarVeiculoInput.class), org.mockito.ArgumentMatchers.eq(10L));
    }

    @Test
    void deveRetornar404QuandoClienteNaoExistir() {
        when(clienteGateway.findIdByCpfCnpj(cadastro.cpfCnpj())).thenReturn(Optional.empty());

        assertThrows(
                ClienteNaoEncontradoException.class,
                () -> cadastrarVeiculoUseCase.execute(cadastro));

        verify(veiculoGateway, never()).save(any(CadastrarVeiculoInput.class), any());
    }

    @Test
    void deveRetornar409QuandoPlacaJaExistir() {
        when(clienteGateway.findIdByCpfCnpj(cadastro.cpfCnpj())).thenReturn(Optional.of(10L));
        when(veiculoGateway.existsByPlaca("ABC1234")).thenReturn(true);

        assertThrows(
                VeiculoDuplicadoException.class,
                () -> cadastrarVeiculoUseCase.execute(cadastro));

        verify(veiculoGateway, never()).save(any(CadastrarVeiculoInput.class), any());
    }

    @Test
    void deveBuscarVeiculoQuandoClientePossuirAcesso() {
        when(veiculoGateway.findById(1L)).thenReturn(Optional.of(output));

        VeiculoOutput resultado = buscarVeiculoUseCase.execute(1L);

        assertSame(output, resultado);
        verify(authorizationService).validarPermissao(output);
    }

    @Test
    void deveBloquearClienteSemPermissao() {
        when(veiculoGateway.findById(1L)).thenReturn(Optional.of(output));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(authorizationService).validarPermissao(output);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> buscarVeiculoUseCase.execute(1L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void deveRetornar404AoBuscarVeiculoInexistente() {
        when(veiculoGateway.findById(1L)).thenReturn(Optional.empty());

        VeiculoNaoEncontradoException exception = assertThrows(
                VeiculoNaoEncontradoException.class,
                () -> buscarVeiculoUseCase.execute(1L));

        assertEquals("Veículo não encontrado com o ID: 1", exception.getMessage());
    }

    @Test
    void deveListarVeiculosComFiltroDoClienteAutenticado() {
        PageOutput<VeiculoOutput> page = new PageOutput<>(List.of(output), 0, 20, 1);
        when(clienteAutenticadoService.getClienteId()).thenReturn(Optional.of(10L));
        when(veiculoGateway.findAll(any(), any())).thenReturn(page);

        PageOutput<VeiculoOutput> resultado = listarVeiculosUseCase.execute(
                new VeiculoInput(null, null, null, null),
                new PageInput(0, 20));

        assertEquals(page, resultado);
        verify(veiculoGateway).findAll(any(), any());
    }

    @Test
    void deveAtualizarVeiculoComPlacaNormalizada() {
        when(veiculoGateway.findById(1L)).thenReturn(Optional.of(output));
        when(veiculoGateway.findByPlaca("ABC1234")).thenReturn(Optional.of(output));
        when(veiculoGateway.update(org.mockito.ArgumentMatchers.eq(1L), any(VeiculoInput.class)))
                .thenReturn(output);

        VeiculoOutput resultado = atualizarVeiculoUseCase.execute(1L, atualizacao);

        assertEquals(output, resultado);
        verify(authorizationService).validarPermissao(output);
        verify(veiculoGateway).update(org.mockito.ArgumentMatchers.eq(1L), any(VeiculoInput.class));
    }

    @Test
    void deveRetornar409AoAtualizarComPlacaDeOutroVeiculo() {
        VeiculoOutput outro = new VeiculoOutput(2L, "ABC1234", "Honda", "Fit", 2020, 10L);
        when(veiculoGateway.findById(1L)).thenReturn(Optional.of(output));
        when(veiculoGateway.findByPlaca("ABC1234")).thenReturn(Optional.of(outro));

        assertThrows(
                VeiculoDuplicadoException.class,
                () -> atualizarVeiculoUseCase.execute(1L, atualizacao));

        verify(veiculoGateway, never()).update(any(), any());
    }

    @Test
    void deveRetornar404AoAtualizarVeiculoInexistente() {
        when(veiculoGateway.findById(1L)).thenReturn(Optional.empty());

        VeiculoNaoEncontradoException exception = assertThrows(
                VeiculoNaoEncontradoException.class,
                () -> atualizarVeiculoUseCase.execute(1L, atualizacao));

        assertEquals("Veículo não encontrado com o ID: 1", exception.getMessage());
    }

    @Test
    void deveDeletarVeiculoExistente() {
        when(veiculoGateway.existsById(1L)).thenReturn(true);

        deletarVeiculoUseCase.execute(1L);

        verify(veiculoGateway).deleteById(1L);
    }

    @Test
    void deveRetornar404AoDeletarVeiculoInexistente() {
        when(veiculoGateway.existsById(1L)).thenReturn(false);

        VeiculoNaoEncontradoException exception = assertThrows(
                VeiculoNaoEncontradoException.class,
                () -> deletarVeiculoUseCase.execute(1L));

        assertEquals("Veículo não encontrado com o ID: 1", exception.getMessage());
    }

    @Test
    void deveRetornarVeiculoExistenteDoMesmoClienteAoBuscarOuCadastrar() {
        VeiculoOrdemServicoInput input = new VeiculoOrdemServicoInput("abc-1234", null, null, null);
        VeiculoOutput existente = new VeiculoOutput(2L, "ABC1234", "Honda", "Civic", 2020, 10L);
        when(veiculoGateway.findByPlaca("ABC1234")).thenReturn(Optional.of(existente));

        assertSame(existente, buscarOuCadastrarVeiculoUseCase.execute(10L, input));
        verify(veiculoGateway, never()).save(any(VeiculoOrdemServicoInput.class), any());
    }

    @Test
    void deveRecusarVeiculoExistenteDeOutroClienteAoBuscarOuCadastrar() {
        VeiculoOrdemServicoInput input = new VeiculoOrdemServicoInput("ABC1234", null, null, null);
        when(veiculoGateway.findByPlaca("ABC1234"))
                .thenReturn(Optional.of(new VeiculoOutput(2L, "ABC1234", "Honda", "Civic", 2020, 20L)));

        assertThrows(VeiculoDuplicadoException.class,
                () -> buscarOuCadastrarVeiculoUseCase.execute(10L, input));
        verify(veiculoGateway, never()).save(any(VeiculoOrdemServicoInput.class), any());
    }

    @Test
    void deveCadastrarVeiculoNovoAoBuscarOuCadastrar() {
        VeiculoOrdemServicoInput input = new VeiculoOrdemServicoInput("abc-1234", "Honda", "Civic", 2023);
        when(veiculoGateway.findByPlaca("ABC1234")).thenReturn(Optional.empty());
        when(veiculoGateway.save(any(VeiculoOrdemServicoInput.class), eq(10L))).thenReturn(output);

        assertSame(output, buscarOuCadastrarVeiculoUseCase.execute(10L, input));
        verify(veiculoGateway).save(
                new VeiculoOrdemServicoInput("ABC1234", "Honda", "Civic", 2023), 10L);
    }

    @Test
    void deveRejeitarDadosIncompletosAoCadastrarVeiculoNovo() {
        when(veiculoGateway.findByPlaca("ABC1234")).thenReturn(Optional.empty());

        List<VeiculoOrdemServicoInput> entradasInvalidas = List.of(
                new VeiculoOrdemServicoInput("ABC1234", null, "Civic", 2023),
                new VeiculoOrdemServicoInput("ABC1234", " ", "Civic", 2023),
                new VeiculoOrdemServicoInput("ABC1234", "Honda", null, 2023),
                new VeiculoOrdemServicoInput("ABC1234", "Honda", " ", 2023),
                new VeiculoOrdemServicoInput("ABC1234", "Honda", "Civic", null)
        );

        for (VeiculoOrdemServicoInput entrada : entradasInvalidas) {
            assertThrows(VeiculoDadosInvalidosException.class,
                    () -> buscarOuCadastrarVeiculoUseCase.execute(10L, entrada));
        }
        verify(veiculoGateway, never()).save(any(VeiculoOrdemServicoInput.class), any());
    }

    @Test
    void deveRetornarVeiculoExistenteParaOS() {
        ClienteOutput cliente = ClienteOutput.builder().id(10L).nome("Cliente").cpfCnpj("123").email("cliente@email.com").build();
        VeiculoOutput veiculo = new VeiculoOutput(1L, "ABC1234", "Honda", "Civic", 2020, 10L);
        VeiculoOrdemServicoInput input = new VeiculoOrdemServicoInput("abc-1234", null, null, null);
        when(veiculoGateway.findByPlaca("ABC1234")).thenReturn(Optional.of(veiculo));

        assertSame(veiculo, buscarOuCadastrarVeiculoService.execute(cliente, input));
        verify(veiculoGateway, never()).save(any(VeiculoOrdemServicoInput.class), any());
    }

    @Test
    void deveRecusarPlacaDeOutroClienteNaOS() {
        ClienteOutput cliente = ClienteOutput.builder().id(10L).nome("Cliente").cpfCnpj("123").email("cliente@email.com").build();
        VeiculoOutput veiculo = new VeiculoOutput(1L, "ABC1234", "Honda", "Civic", 2020, 20L);
        when(veiculoGateway.findByPlaca("ABC1234")).thenReturn(Optional.of(veiculo));
        var veiculoInput = new VeiculoOrdemServicoInput("ABC1234", null, null, null);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> buscarOuCadastrarVeiculoService.execute(cliente, veiculoInput));

        assertEquals(ApplicationException.ErrorType.CONFLICT, exception.type());
    }

    @Test
    void deveCadastrarVeiculoNovoParaOS() {
        ClienteOutput cliente = ClienteOutput.builder().id(10L).nome("Cliente").cpfCnpj("123").email("cliente@email.com").build();
        when(veiculoGateway.findByPlaca("ABC1234")).thenReturn(Optional.empty());
        when(veiculoGateway.save(any(VeiculoOrdemServicoInput.class), eq(10L))).thenReturn(output);

        VeiculoOutput resultado = buscarOuCadastrarVeiculoService.execute(
                cliente,
                new VeiculoOrdemServicoInput("abc-1234", "Honda", "Civic", 2023));

        assertEquals("ABC1234", resultado.placa());
        assertEquals(10L, resultado.clienteId());
        verify(veiculoGateway).save(any(VeiculoOrdemServicoInput.class), eq(10L));
    }

    @Test
    void deveRejeitarDadosIncompletosAoCadastrarVeiculoNovoParaOS() {
        ClienteOutput cliente = ClienteOutput.builder().id(10L).nome("Cliente").cpfCnpj("123").email("cliente@email.com").build();
        when(veiculoGateway.findByPlaca("ABC1234")).thenReturn(Optional.empty());

        List<VeiculoOrdemServicoInput> entradasInvalidas = List.of(
                new VeiculoOrdemServicoInput("ABC1234", null, "Civic", 2023),
                new VeiculoOrdemServicoInput("ABC1234", " ", "Civic", 2023),
                new VeiculoOrdemServicoInput("ABC1234", "Honda", null, 2023),
                new VeiculoOrdemServicoInput("ABC1234", "Honda", " ", 2023),
                new VeiculoOrdemServicoInput("ABC1234", "Honda", "Civic", null)
        );

        for (VeiculoOrdemServicoInput entrada : entradasInvalidas) {
            ApplicationException exception = assertThrows(ApplicationException.class,
                    () -> buscarOuCadastrarVeiculoService.execute(cliente, entrada));
            assertEquals(ApplicationException.ErrorType.BAD_REQUEST, exception.type());
        }
        verify(veiculoGateway, never()).save(any(VeiculoOrdemServicoInput.class), any());
    }
}
