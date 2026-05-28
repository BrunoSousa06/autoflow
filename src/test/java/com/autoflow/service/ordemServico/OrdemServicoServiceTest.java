package com.autoflow.service.ordemServico;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemServico.StatusOrdemServico;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.repository.ordemServico.OrdemServicoRepository;
import com.autoflow.service.cliente.ClienteService;
import com.autoflow.service.veiculo.VeiculoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    @Test
    void deveCriarESalvarOrdemServico() {
        Long clienteId = 1L;
        Long veiculoId = 1L;
        ClienteEntity cliente = criarCliente(clienteId);
        VeiculoEntity veiculo = criarVeiculo(veiculoId, cliente);
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Revisao");

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(veiculoService.buscarPorId(veiculoId)).thenReturn(veiculo);
        when(repository.save(any(OrdemServicoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrdemServicoEntity ordemServicoEntity = service.criar(clienteId, veiculoId, List.of(servico));

        assertEquals(clienteId, ordemServicoEntity.getClienteId());
        assertEquals(veiculoId, ordemServicoEntity.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, ordemServicoEntity.getStatus());
        assertTrue(ordemServicoEntity.getNumeroOs().startsWith("OS-"));
        assertNotNull(ordemServicoEntity.getDataAbertura());
        assertEquals(List.of(servico), ordemServicoEntity.getServicosSolicitados());

        ArgumentCaptor<OrdemServicoEntity> captor = ArgumentCaptor.forClass(OrdemServicoEntity.class);
        verify(repository).save(captor.capture());
        OrdemServicoEntity ordemServicoSalva = captor.getValue();

        assertEquals(clienteId, ordemServicoSalva.getClienteId());
        assertEquals(veiculoId, ordemServicoSalva.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, ordemServicoSalva.getStatus());
        assertTrue(ordemServicoSalva.getNumeroOs().startsWith("OS-"));
        assertNotNull(ordemServicoSalva.getDataAbertura());
        assertEquals(List.of(servico), ordemServicoSalva.getServicosSolicitados());
        verify(clienteService).buscarPorId(clienteId);
        verify(veiculoService).buscarPorId(veiculoId);
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
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        ServicoSolicitadoEntity servicoInicial = new ServicoSolicitadoEntity(1L, "Revisao");
        ServicoSolicitadoEntity novoServico = new ServicoSolicitadoEntity(2L, "Troca de oleo");
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(
                cliente,
                veiculo,
                List.of(servicoInicial)
        );

        when(repository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServicoEntity));

        OrdemServicoEntity resultado = service.incluirServicos(ordemServicoId, List.of(novoServico));

        assertEquals(ordemServicoEntity, resultado);
        assertEquals(List.of(servicoInicial, novoServico), resultado.getServicosSolicitados());
        verify(repository).findById(ordemServicoId);
        verify(repository, never()).save(any(OrdemServicoEntity.class));
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

    private ClienteEntity criarCliente(Long clienteId) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(clienteId);
        cliente.setCpf("1223321123");
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
}
