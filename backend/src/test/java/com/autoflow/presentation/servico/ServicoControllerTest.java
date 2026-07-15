package com.autoflow.presentation.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.dto.servico.TempoMedioServicoMetricaOutput;
import com.autoflow.application.usecases.servico.*;
import com.autoflow.presentation.servico.ServicoController;
import com.autoflow.presentation.servico.request.ServicoRequest;
import com.autoflow.presentation.servico.response.ServicoResponse;
import com.autoflow.presentation.servico.response.TempoMedioServicoResponse;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.mapper.ServicoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ServicoControllerTest {

    @Mock
    private CriarServicoUseCase criarServicoUseCase;

    @Mock
    private BuscarServicoPorIdUseCase buscarServicoPorIdUseCase;

    @Mock
    private ListarServicosUseCase listarServicosUseCase;

    @Mock
    private AtualizarServicoUseCase atualizarServicoUseCase;

    @Mock
    private InativarServicoUseCase inativarServicoUseCase;

    @Mock
    private CalcularTempoMedioServicoUseCase calcularTempoMedioServicoUseCase;

    @Mock
    private ServicoMapper servicoMapper;

    @InjectMocks
    private ServicoController servicoController;

    private ServicoRequest servicoRequest;
    private ServicoOutput servicoOutput;
    private ServicoResponse servicoResponse;
    private TempoMedioServicoMetricaOutput tempoMedioServicoMetricaOutput;
    private TempoMedioServicoResponse tempoMedioServicoResponse; // Added for the test

    @BeforeEach
    void setup() {
        servicoRequest = new ServicoRequest(
                "Troca de Óleo",
                "Substituição do óleo do motor",
                new BigDecimal("150.00")
        );

        servicoOutput = ServicoOutput.builder()
                .id(1L)
                .nome("Troca de Óleo")
                .descricao("Substituição do óleo do motor")
                .valor(new BigDecimal("150.00"))
                .ativo(true)
                .build();

        servicoResponse = new ServicoResponse(
                1L,
                "Troca de Óleo",
                "Substituição do óleo do motor",
                new BigDecimal("150.00"),
                true
        );

        tempoMedioServicoMetricaOutput = new TempoMedioServicoMetricaOutput(1L, "Troca de Óleo", 2L, 3600.0, 60.0, 1.0);
        tempoMedioServicoResponse = new TempoMedioServicoResponse(1L, "Troca de Óleo", 2L, 3600.0, 60.0, 1.0); // Initialize
    }

    @Test
    void deveCadastrarServico() {
        when(criarServicoUseCase.execute(any(ServicoInput.class)))
                .thenReturn(servicoOutput);

        ServicoEntity entity = new ServicoEntity();
        entity.setId(1L);
        entity.setNome("Troca de Óleo");
        entity.setDescricao("Substituição do óleo do motor");
        entity.setValor(new BigDecimal("150.00"));
        entity.setAtivo(true);

        when(servicoMapper.mapToResponse(any(ServicoOutput.class)))
                .thenReturn(servicoResponse);

        ResponseEntity<ServicoResponse> resultado = servicoController.cadastrar(servicoRequest);

        assertNotNull(resultado);
        assertEquals(HttpStatus.CREATED, resultado.getStatusCode());
        assertEquals("Troca de Óleo", resultado.getBody().nome());

        ArgumentCaptor<ServicoInput> captor = ArgumentCaptor.forClass(ServicoInput.class);
        verify(criarServicoUseCase).execute(captor.capture());
    }

    @Test
    void deveListarServicoPorId() {
        when(buscarServicoPorIdUseCase.execute(1L))
                .thenReturn(servicoOutput);

        ServicoEntity entity = new ServicoEntity();
        entity.setId(1L);
        entity.setNome("Troca de Óleo");
        entity.setDescricao("Substituição do óleo do motor");
        entity.setValor(new BigDecimal("150.00"));
        entity.setAtivo(true);

        when(servicoMapper.mapToResponse(any(ServicoOutput.class)))
                .thenReturn(servicoResponse);

        ResponseEntity<ServicoResponse> resultado = servicoController.listar(1L);

        assertNotNull(resultado);
        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals("Troca de Óleo", resultado.getBody().nome());

        verify(buscarServicoPorIdUseCase).execute(1L);
    }

    @Test
    void deveListarTodosServicos() {
        Page<ServicoOutput> outputPage = new PageImpl<>(List.of(servicoOutput));

        when(listarServicosUseCase.execute(any(Pageable.class)))
                .thenReturn(outputPage);

        ServicoEntity entity = new ServicoEntity();
        entity.setId(1L);
        entity.setNome("Troca de Óleo");
        entity.setDescricao("Substituição do óleo do motor");
        entity.setValor(new BigDecimal("150.00"));
        entity.setAtivo(true);


        ResponseEntity<Page<ServicoResponse>> resultado = servicoController.listarTodosServicos(0, 20);

        assertNotNull(resultado);
        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(1, resultado.getBody().getContent().size());

        verify(listarServicosUseCase).execute(any(Pageable.class));
    }

    @Test
    void deveAtualizarServico() {
        when(atualizarServicoUseCase.execute(eq(1L), any(ServicoInput.class)))
                .thenReturn(servicoOutput);

        ServicoEntity entity = new ServicoEntity();
        entity.setId(1L);
        entity.setNome("Troca de Óleo");
        entity.setDescricao("Substituição do óleo do motor");
        entity.setValor(new BigDecimal("150.00"));
        entity.setAtivo(true);

        ResponseEntity<ServicoResponse> resultado = servicoController.atualizar(servicoRequest, 1L);

        assertNotNull(resultado);
        assertEquals(HttpStatus.OK, resultado.getStatusCode());

        ArgumentCaptor<ServicoInput> captor = ArgumentCaptor.forClass(ServicoInput.class);
        verify(atualizarServicoUseCase).execute(eq(1L), captor.capture());
    }

    @Test
    void deveInativarServico() {
        ResponseEntity<String> resultado = servicoController.inativar(1L);

        assertNotNull(resultado);
        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals("Serviço inativado com sucesso", resultado.getBody());

        verify(inativarServicoUseCase).execute(1L);
    }

    @Test
    void deveListarTempoMedioPorServico() {
        TempoMedioServicoMetricaOutput metricaOutput = TempoMedioServicoMetricaOutput.builder()
                .servicoId(1L)
                .nomeServico("Troca de Óleo")
                .quantidadeExecucoes(2L)
                .tempoMedioSegundos(3600.0)
                .tempoMedioMinutos(60.0)
                .tempoMedioHoras(1.0)
                .build();

        List<TempoMedioServicoResponse> expectedResponses = List.of(tempoMedioServicoResponse);

        when(calcularTempoMedioServicoUseCase.execute())
                .thenReturn(List.of(metricaOutput));
        
        when(servicoMapper.mapToMetricResponse(anyList()))
                .thenReturn(expectedResponses);


        ResponseEntity<List<TempoMedioServicoResponse>> resultado = servicoController.listarTempoMedioPorServico();

        assertNotNull(resultado);
        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(1, resultado.getBody().size());
        assertEquals("Troca de Óleo", resultado.getBody().get(0).nomeServico());
        assertEquals(2L, resultado.getBody().get(0).quantidadeExecucoes());

        verify(calcularTempoMedioServicoUseCase).execute();
        verify(servicoMapper).mapToMetricResponse(List.of(metricaOutput)); // Verify with the actual argument
    }

}