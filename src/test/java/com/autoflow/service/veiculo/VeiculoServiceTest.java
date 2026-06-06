package com.autoflow.service.veiculo;

import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.controller.veiculo.request.VeiculoRequest;
import com.autoflow.controller.veiculo.response.VeiculoResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.mapper.VeiculoMapper;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.repository.veiculo.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private VeiculoRequest request;
    private ClienteEntity clienteEntity;
    private VeiculoEntity veiculoEntity;
    private VeiculoResponse response;

    @BeforeEach
    void setup() {
        request = new VeiculoRequest("12345632451","Honda", 2020,"HXS-53454", "Civic");
        clienteEntity = new ClienteEntity();
        clienteEntity.setId(1L);
        veiculoEntity = new VeiculoEntity();
        veiculoEntity.setId(1L);
        veiculoEntity.setCliente(clienteEntity);
        veiculoEntity.setPlaca("ABC1D23");
        response = new VeiculoResponse(1L, "Honda", 2020,"HXS-53454", "Civic", null);
    }

    @Test
    void deveLancarConflictQuandoJaExistirVeiculoComMesmaPlaca() {
        // Arrange
        ClienteEntity cliente = new ClienteEntity();

        when(clienteRepository.findByCpfCnpj(request.cpfCnpj()))
                .thenReturn(Optional.of(cliente));

        when(veiculoRepository.existsByPlaca(request.placa()))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> veiculoService.cadastrar(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(
                "Ja existe um veiculo cadastrado com a placa:" + request.placa(),
                exception.getReason()
        );

        verify(clienteRepository).findByCpfCnpj(request.cpfCnpj());
        verify(veiculoRepository).existsByPlaca(request.placa());

        verify(veiculoRepository, never()).save(any());
        verifyNoInteractions(veiculoMapper);
    }

    @Test
    void deveLancarConflictQuandoAtualizarComPlacaJaCadastradaEmOutroVeiculo() {
        Long id = 1L;

        VeiculoEntity veiculoAtual = new VeiculoEntity();
        veiculoAtual.setId(id);

        VeiculoEntity outroVeiculo = new VeiculoEntity();
        outroVeiculo.setId(2L);

        when(veiculoRepository.findById(id))
                .thenReturn(Optional.of(veiculoAtual));

        when(veiculoRepository.findByPlaca(request.placa()))
                .thenReturn(Optional.of(outroVeiculo));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> veiculoService.atualizar(request, id)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Placa já cadastrada", exception.getReason());

        verify(veiculoRepository).findById(id);
        verify(veiculoRepository).findByPlaca(request.placa());

        verify(veiculoRepository, never()).save(any());
        verifyNoInteractions(veiculoMapper);
    }

    @Test
    void deveAtualizarQuandoPlacaPertencerAoMesmoVeiculo() {
        Long id = 1L;

        VeiculoEntity veiculoAtual = new VeiculoEntity();
        veiculoAtual.setId(id);


        when(veiculoRepository.findById(id))
                .thenReturn(Optional.of(veiculoAtual));

        when(veiculoRepository.findByPlaca(request.placa()))
                .thenReturn(Optional.of(veiculoAtual));

        when(veiculoRepository.save(veiculoAtual))
                .thenReturn(veiculoAtual);

        when(veiculoMapper.mapToResponse(veiculoAtual))
                .thenReturn(response);

        VeiculoResponse resultado = veiculoService.atualizar(request, id);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(veiculoRepository).findById(id);
        verify(veiculoRepository).findByPlaca(request.placa());
        verify(veiculoRepository).save(veiculoAtual);
        verify(veiculoMapper).mapToResponse(veiculoAtual);
    }

    @Test
    void deveCadastrarVeiculoComSucesso() {
        when(clienteRepository.findByCpfCnpj(request.cpfCnpj())).thenReturn(Optional.of(clienteEntity));
        when(veiculoMapper.mapToEntity(request, clienteEntity)).thenReturn(veiculoEntity);
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToResponse(veiculoEntity)).thenReturn(response);

        VeiculoResponse resultado = veiculoService.cadastrar(request);

        assertNotNull(resultado);
        verify(clienteRepository).findByCpfCnpj(request.cpfCnpj());
        verify(veiculoRepository).save(veiculoEntity);
    }

    @Test
    void deveLancarExcecaoAoCadastrarVeiculoComClienteInexistente() {
        when(clienteRepository.findByCpfCnpj(request.cpfCnpj())).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.cadastrar(request);
        });

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Cliente não encontrado com o CPF/CNPJ: " + request.cpfCnpj(), excecao.getReason());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveListarVeiculoPorIdComSucesso() {
        Long id = 1L;
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculoEntity));
        when(veiculoMapper.mapToResponse(veiculoEntity)).thenReturn(response);

        VeiculoResponse resultado = veiculoService.listar(id);

        assertNotNull(resultado);
        verify(veiculoRepository).findById(id);
    }

    @Test
    void deveLancarExcecaoAoListarIdInexistente() {
        Long id = 1L;
        when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.listar(id);
        });

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Veículo não encontrado com o ID: " + id, excecao.getReason());
    }

    @Test
    void deveListarTodosOsVeiculos() {
        List<VeiculoEntity> veiculos = List.of(veiculoEntity);
        List<VeiculoResponse> responses = List.of(response);

        when(veiculoRepository.findAll()).thenReturn(veiculos);
        when(veiculoMapper.mapToList(veiculos)).thenReturn(responses);

        List<VeiculoResponse> resultado = veiculoService.listarTodosVeiculos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(veiculoRepository).findAll();
    }

    @Test
    void deveAtualizarVeiculoComSucesso() {
        Long id = 1L;
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculoEntity));
        doNothing().when(veiculoMapper).updateEntity(request, veiculoEntity);
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToResponse(veiculoEntity)).thenReturn(response);

        VeiculoResponse resultado = veiculoService.atualizar(request, id);

        assertNotNull(resultado);
        verify(veiculoRepository).findById(id);
        verify(veiculoRepository).save(veiculoEntity);
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        Long id = 1L;
        when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.atualizar(request, id);
        });

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveDeletarVeiculoComSucesso() {
        Long id = 1L;
        when(veiculoRepository.existsById(id)).thenReturn(true);
        doNothing().when(veiculoRepository).deleteById(id);

        veiculoService.deletar(id);

        verify(veiculoRepository).existsById(id);
        verify(veiculoRepository).deleteById(id);
    }

    @Test
    void deveLancarExcecaoAoDeletarIdInexistente() {
        Long id = 1L;
        when(veiculoRepository.existsById(id)).thenReturn(false);

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.deletar(id);
        });

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Veículo não encontrado com o ID: " + id, excecao.getReason());
        verify(veiculoRepository, never()).deleteById(any());
    }

    @Test
    void deveBuscarPorIdInternamenteComSucesso() {
        Long id = 1L;
        when(veiculoRepository.findById(id)).thenReturn(Optional.of(veiculoEntity));

        VeiculoEntity resultado = veiculoService.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(veiculoEntity, resultado);
    }

    @Test
    void deveLancarExcecaoAoBuscarPorIdInternoInexistente() {
        Long id = 1L;
        when(veiculoRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.buscarPorId(id);
        });

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Veículo não encontrado com o ID: " + id, excecao.getReason());
    }

    @Test
    void deveUsarVeiculoExistentePorPlacaQuandoPertencerAoCliente() {
        VeiculoOrdemServicoRequest requestOs = new VeiculoOrdemServicoRequest("abc-1d23", null, null, null);

        when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculoEntity));

        VeiculoEntity resultado = veiculoService.buscarOuCadastrarPorPlacaParaCliente(clienteEntity, requestOs);

        assertEquals(veiculoEntity, resultado);
        verify(veiculoRepository).findByPlaca("ABC1D23");
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveCadastrarVeiculoQuandoPlacaNaoExistir() {
        VeiculoOrdemServicoRequest requestOs = new VeiculoOrdemServicoRequest("abc-1d23", "Honda", "Civic", 2020);

        when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.empty());
        when(veiculoRepository.save(any(VeiculoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeiculoEntity resultado = veiculoService.buscarOuCadastrarPorPlacaParaCliente(clienteEntity, requestOs);

        assertEquals(clienteEntity, resultado.getCliente());
        assertEquals("ABC1D23", resultado.getPlaca());
        assertEquals("Honda", resultado.getMarca());
        assertEquals("Civic", resultado.getModelo());
        assertEquals(2020, resultado.getAno());
        verify(veiculoRepository).save(any(VeiculoEntity.class));
    }

    @Test
    void deveRetornarConflictQuandoPlacaPertencerAOutroCliente() {
        ClienteEntity outroCliente = new ClienteEntity();
        outroCliente.setId(99L);
        veiculoEntity.setCliente(outroCliente);
        VeiculoOrdemServicoRequest requestOs = new VeiculoOrdemServicoRequest("ABC1D23", "Honda", "Civic", 2020);

        when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.of(veiculoEntity));

        ResponseStatusException excecao = assertThrows(
                ResponseStatusException.class,
                () -> veiculoService.buscarOuCadastrarPorPlacaParaCliente(clienteEntity, requestOs)
        );

        assertEquals(HttpStatus.CONFLICT, excecao.getStatusCode());
        assertEquals("Placa ja cadastrada para outro cliente.", excecao.getReason());
        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void deveRetornarBadRequestQuandoCadastrarVeiculoNovoSemDadosObrigatorios() {
        VeiculoOrdemServicoRequest requestOs = new VeiculoOrdemServicoRequest("ABC1D23", null, "Civic", 2020);

        when(veiculoRepository.findByPlaca("ABC1D23")).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(
                ResponseStatusException.class,
                () -> veiculoService.buscarOuCadastrarPorPlacaParaCliente(clienteEntity, requestOs)
        );

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
        verify(veiculoRepository, never()).save(any());
    }
}
