package com.autoflow.service.servico;

import com.autoflow.controller.servico.request.ServicoRequest;
import com.autoflow.controller.servico.response.ServicoResponse;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.mapper.ServicoMapper;
import com.autoflow.repository.servico.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @InjectMocks
    private ServicoService servicoService;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ServicoMapper servicoMapper;

    private ServicoRequest request;
    private ServicoEntity entity;
    private ServicoResponse response;

    @BeforeEach
    void setup() {
        request = new ServicoRequest("Troca de Óleo", BigDecimal.valueOf(80.00));
        entity = new ServicoEntity();
        response = new ServicoResponse(1L,"Troca de Óleo", BigDecimal.valueOf(80.00));
    }

    @Test
    void deveCadastrarServicoComSucesso() {
        when(servicoRepository.findByNomeIgnoreCase(request.nome())).thenReturn(Optional.empty());
        when(servicoMapper.mapToEntity(request)).thenReturn(entity);
        when(servicoRepository.save(entity)).thenReturn(entity);
        when(servicoMapper.toResponse(entity)).thenReturn(response);

        ServicoResponse resultado = servicoService.cadastrar(request);

        assertNotNull(resultado);
        assertEquals(response.nome(), resultado.nome());
        verify(servicoRepository).save(entity);
    }

    @Test
    void deveLancarExcecaoAoCadastrarServicoDuplicado() {
        when(servicoRepository.findByNomeIgnoreCase(request.nome())).thenReturn(Optional.of(entity));

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            servicoService.cadastrar(request);
        });

        assertEquals(HttpStatus.BAD_REQUEST, excecao.getStatusCode());
        assertEquals("Serviço já foi cadastrado", excecao.getReason());
        verify(servicoRepository, never()).save(any());
    }

    @Test
    void deveListarTodosOsServicos() {
        List<ServicoEntity> listaEntities = List.of(entity);
        List<ServicoResponse> listaResponses = List.of(response);

        when(servicoRepository.findAll()).thenReturn(listaEntities);
        when(servicoMapper.toResponseList(listaEntities)).thenReturn(listaResponses);

        List<ServicoResponse> resultado = servicoService.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(servicoRepository).findAll();
    }

    @Test
    void deveBuscarServicoPorIdComSucesso() {
        Long id = 1L;
        when(servicoRepository.findById(id)).thenReturn(Optional.of(entity));
        when(servicoMapper.toResponse(entity)).thenReturn(response);

        ServicoResponse resultado = servicoService.buscarPorId(id);

        assertNotNull(resultado);
        verify(servicoRepository).findById(id);
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        Long id = 1L;
        when(servicoRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            servicoService.buscarPorId(id);
        });

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Serviço não encontrado com o ID: " + id, excecao.getReason());
    }

    @Test
    void deveAtualizarServicoComSucesso() {
        Long id = 1L;
        when(servicoRepository.findById(id)).thenReturn(Optional.of(entity));
        doNothing().when(servicoMapper).updateEntity(request, entity);
        when(servicoRepository.save(entity)).thenReturn(entity);
        when(servicoMapper.toResponse(entity)).thenReturn(response);

        ServicoResponse resultado = servicoService.atualizar(request, id);

        assertNotNull(resultado);
        verify(servicoRepository).findById(id);
        verify(servicoRepository).save(entity);
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdInexistente() {
        Long id = 1L;
        when(servicoRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            servicoService.atualizar(request, id);
        });

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        verify(servicoRepository, never()).save(any());
    }

    @Test
    void deveDeletarServicoComSucesso() {
        Long id = 1L;
        when(servicoRepository.existsById(id)).thenReturn(true);
        doNothing().when(servicoRepository).deleteById(id);

        servicoService.deletar(id);

        verify(servicoRepository).existsById(id);
        verify(servicoRepository).deleteById(id);
    }

    @Test
    void deveLancarExcecaoAoDeletarIdInexistente() {
        Long id = 1L;
        when(servicoRepository.existsById(id)).thenReturn(false);

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> {
            servicoService.deletar(id);
        });

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("ID informado para exclusão não existe: " + id, excecao.getReason());
        verify(servicoRepository, never()).deleteById(id);
    }
}
