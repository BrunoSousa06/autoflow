package com.autoflow.service.veiculo;

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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        request = new VeiculoRequest(1L,"Honda", 2020,"HXS-53454", "Civic");
        clienteEntity = new ClienteEntity();
        veiculoEntity = new VeiculoEntity();
        response = new VeiculoResponse(1L, null, "Honda", 2020,"HXS-53454", "Civic");
    }

    @Test
    void deveCadastrarVeiculoComSucesso() {
        when(clienteRepository.findById(request.idCliente())).thenReturn(Optional.of(clienteEntity));
        when(veiculoMapper.mapToEntity(request, clienteEntity)).thenReturn(veiculoEntity);
        when(veiculoRepository.save(veiculoEntity)).thenReturn(veiculoEntity);
        when(veiculoMapper.mapToResponse(veiculoEntity)).thenReturn(response);

        VeiculoResponse resultado = veiculoService.cadastrar(request);

        assertNotNull(resultado);
        verify(clienteRepository).findById(request.idCliente());
        verify(veiculoRepository).save(veiculoEntity);
    }

    @Test
    void deveLancarExcecaoAoCadastrarVeiculoComClienteInexistente() {
        when(clienteRepository.findById(request.idCliente())).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            veiculoService.cadastrar(request);
        });

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Cliente não encontrado com o ID: " + request.idCliente(), excecao.getReason());
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
}
