package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.dto.veiculo.PageInput;
import com.autoflow.application.dto.veiculo.PageOutput;
import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.exception.VeiculoDuplicadoException;
import com.autoflow.application.exception.VeiculoNaoEncontradoException;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.security.AuthorizationService;
import com.autoflow.application.security.ClienteAutenticadoService;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import com.autoflow.service.ordemservico.BuscarOuCadastrarVeiculoForOrdemServicoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        ClienteNaoEncontradoException exception = assertThrows(
                ClienteNaoEncontradoException.class,
                () -> cadastrarVeiculoUseCase.execute(cadastro));

        verify(veiculoGateway, never()).save(any(CadastrarVeiculoInput.class), any());
    }

    @Test
    void deveRetornar409QuandoPlacaJaExistir() {
        when(clienteGateway.findIdByCpfCnpj(cadastro.cpfCnpj())).thenReturn(Optional.of(10L));
        when(veiculoGateway.existsByPlaca("ABC1234")).thenReturn(true);

        VeiculoDuplicadoException exception = assertThrows(
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

        VeiculoDuplicadoException exception = assertThrows(
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
    void deveRetornarVeiculoExistenteParaOS() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(10L);
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setCliente(cliente);
        veiculo.setPlaca("ABC1234");
        VeiculoOrdemServicoInput input = new VeiculoOrdemServicoInput("abc-1234", null, null, null);
        when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.of(veiculo));

        assertSame(veiculo, buscarOuCadastrarVeiculoService.execute(cliente, input));
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveRecusarPlacaDeOutroClienteNaOS() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(10L);
        ClienteEntity outroCliente = new ClienteEntity();
        outroCliente.setId(20L);
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setCliente(outroCliente);
        when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.of(veiculo));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> buscarOuCadastrarVeiculoService.execute(
                        cliente,
                        new VeiculoOrdemServicoInput("ABC1234", null, null, null)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void deveCadastrarVeiculoNovoParaOS() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(10L);
        when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.empty());
        when(veiculoRepository.save(any(VeiculoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VeiculoEntity resultado = buscarOuCadastrarVeiculoService.execute(
                cliente,
                new VeiculoOrdemServicoInput("abc-1234", "Honda", "Civic", 2023));

        assertEquals("ABC1234", resultado.getPlaca());
        assertEquals(cliente, resultado.getCliente());
        verify(veiculoRepository).save(any(VeiculoEntity.class));
    }
}
