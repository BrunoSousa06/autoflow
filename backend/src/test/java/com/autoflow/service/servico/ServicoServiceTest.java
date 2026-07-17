package com.autoflow.service.servico;

import com.autoflow.presentation.servico.request.ServicoRequest;
import com.autoflow.presentation.servico.response.ServicoResponse;
import com.autoflow.presentation.servico.response.TempoMedioServicoResponse;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.infrastructure.persistence.mapper.ServicoMapper;
import com.autoflow.infrastructure.persistence.repository.ServicoRepository;
import com.autoflow.infrastructure.persistence.repository.ServicoSolicitadoRepository;
import com.autoflow.infrastructure.persistence.repository.TempoMedioServicoProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @InjectMocks
    private ServicoService servicoService;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ServicoMapper servicoMapper;

    @Mock
    private ServicoSolicitadoRepository servicoSolicitadoRepository;

    private ServicoRequest request;
    private ServicoEntity entity;
    private ServicoResponse response;

    @BeforeEach
    void setup() {
        request = new ServicoRequest("Troca de Óleo", "Substituição do óleo do motor", BigDecimal.valueOf(80.00));
        entity = new ServicoEntity();
        response = new ServicoResponse(1L, "Troca de Óleo", "Substituição do óleo do motor", BigDecimal.valueOf(80.00), true);
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

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> servicoService.cadastrar(request));

        assertEquals(HttpStatus.CONFLICT, excecao.getStatusCode());
        assertEquals("Serviço já foi cadastrado", excecao.getReason());
        verify(servicoRepository, never()).save(any());
    }

    @Test
    void deveListarTodosOsServicosAtivos() {
        List<ServicoEntity> listaEntities = List.of(entity);
        PageRequest pageable = PageRequest.of(0, 20);

        when(servicoRepository.findAllByAtivoTrue(pageable)).thenReturn(new PageImpl<>(listaEntities));
        when(servicoMapper.toResponse(entity)).thenReturn(response);

        Page<ServicoResponse> resultado = servicoService.listar(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(servicoRepository).findAllByAtivoTrue(pageable);
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
    void deveBuscarServicoEntityPorIdComSucesso() {
        Long id = 1L;
        when(servicoRepository.findById(id)).thenReturn(Optional.of(entity));
        ServicoEntity resultado = servicoService.buscarEntityPorId(id);

        assertNotNull(resultado);
        verify(servicoRepository).findById(id);
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        Long id = 1L;
        when(servicoRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> servicoService.buscarPorId(id));

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Serviço não encontrado com o ID: " + id, excecao.getReason());
    }

    @Test
    void deveLancarExcecaoAoBuscarServicoEntityIdInexistente() {
        Long id = 1L;
        when(servicoRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> servicoService.buscarEntityPorId(id));

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

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> servicoService.atualizar(request, id));

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        verify(servicoRepository, never()).save(any());
    }

    @Test
    void deveInativarServicoComSucesso() {
        Long id = 1L;
        entity.setAtivo(true);
        when(servicoRepository.findById(id)).thenReturn(Optional.of(entity));
        when(servicoRepository.save(entity)).thenReturn(entity);

        servicoService.inativar(id);

        assertFalse(entity.isAtivo());
        verify(servicoRepository).findById(id);
        verify(servicoRepository).save(entity);
    }

    @Test
    void deveLancarExcecaoAoInativarIdInexistente() {
        Long id = 1L;
        when(servicoRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException excecao = assertThrows(ResponseStatusException.class, () -> servicoService.inativar(id));

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
        assertEquals("Serviço não encontrado com o ID: " + id, excecao.getReason());
        verify(servicoRepository, never()).save(any());
    }

    @Test
    void deveListarTempoMedioPorServico() {
        TempoMedioServicoProjection projection = mock(TempoMedioServicoProjection.class);
        when(projection.getServicoId()).thenReturn(1L);
        when(projection.getNomeServico()).thenReturn("Troca de Óleo");
        when(projection.getQuantidadeExecucoes()).thenReturn(2L);
        when(projection.getTempoMedioSegundos()).thenReturn(3600.0);
        when(servicoSolicitadoRepository.calcularTempoMedioPorServico()).thenReturn(List.of(projection));

        List<TempoMedioServicoResponse> resultado = servicoService.listarTempoMedioPorServico();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.getFirst().servicoId());
        assertEquals("Troca de Óleo", resultado.getFirst().nomeServico());
        assertEquals(2L, resultado.getFirst().quantidadeExecucoes());
        assertEquals(3600.0, resultado.getFirst().tempoMedioSegundos());
        verify(servicoSolicitadoRepository).calcularTempoMedioPorServico();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistirServicoFinalizado() {
        when(servicoSolicitadoRepository.calcularTempoMedioPorServico()).thenReturn(List.of());

        List<TempoMedioServicoResponse> resultado = servicoService.listarTempoMedioPorServico();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(servicoSolicitadoRepository).calcularTempoMedioPorServico();
    }
}
