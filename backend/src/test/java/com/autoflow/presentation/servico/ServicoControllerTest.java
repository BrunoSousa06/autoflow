package com.autoflow.presentation.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.dto.servico.TempoMedioServicoMetricaOutput;
import com.autoflow.application.usecases.servico.*;
import com.autoflow.infrastructure.persistence.mapper.ServicoMapperImpl;
import com.autoflow.presentation.servico.request.ServicoRequest;
import com.autoflow.presentation.servico.response.ServicoResponse;
import com.autoflow.presentation.servico.response.TempoMedioServicoResponse;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.infrastructure.persistence.mapper.ServicoMapper;
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

    private ServicoMapper servicoMapper;

    private ServicoController servicoController;

    private ServicoRequest servicoRequest;
    private ServicoOutput servicoOutput;
    private TempoMedioServicoResponse tempoMedioServicoResponse;

    @BeforeEach
    void setup() {

        servicoMapper = new ServicoMapperImpl();

        servicoController = new ServicoController(
                criarServicoUseCase,
                buscarServicoPorIdUseCase,
                listarServicosUseCase,
                atualizarServicoUseCase,
                inativarServicoUseCase,
                calcularTempoMedioServicoUseCase,
                servicoMapper
        );

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

        tempoMedioServicoResponse = new TempoMedioServicoResponse(
                1L,
                "Troca de Óleo",
                2L,
                3600.0,
                60.0,
                1.0
        );
    }

    @Test
    void deveCadastrarServico() {

        when(criarServicoUseCase.execute(any(ServicoInput.class)))
                .thenReturn(servicoOutput);

        ResponseEntity<ServicoResponse> resultado =
                servicoController.cadastrar(servicoRequest);

        assertNotNull(resultado);
        assertEquals(HttpStatus.CREATED, resultado.getStatusCode());

        assertNotNull(resultado.getBody());
        assertEquals(1L, resultado.getBody().id());
        assertEquals("Troca de Óleo", resultado.getBody().nome());
        assertEquals("Substituição do óleo do motor", resultado.getBody().descricao());
        assertEquals(new BigDecimal("150.00"), resultado.getBody().valor());
        assertTrue(resultado.getBody().ativo());

        ArgumentCaptor<ServicoInput> captor =
                ArgumentCaptor.forClass(ServicoInput.class);

        verify(criarServicoUseCase).execute(captor.capture());

        ServicoInput input = captor.getValue();

        assertEquals("Troca de Óleo", input.nome());
        assertEquals("Substituição do óleo do motor", input.descricao());
        assertEquals(new BigDecimal("150.00"), input.valor());
    }

    @Test
    void deveListarServicoPorId() {

        when(buscarServicoPorIdUseCase.execute(1L))
                .thenReturn(servicoOutput);

        ResponseEntity<ServicoResponse> resultado =
                servicoController.listar(1L);

        assertNotNull(resultado);
        assertEquals(HttpStatus.OK, resultado.getStatusCode());

        assertEquals(1L, resultado.getBody().id());
        assertEquals("Troca de Óleo", resultado.getBody().nome());

        verify(buscarServicoPorIdUseCase).execute(1L);
    }

    @Test
    void deveListarTodosServicos() {

        Page<ServicoOutput> outputPage =
                new PageImpl<>(List.of(servicoOutput));

        when(listarServicosUseCase.execute(any(Pageable.class)))
                .thenReturn(outputPage);

        ResponseEntity<Page<ServicoResponse>> resultado =
                servicoController.listarTodosServicos(0,20);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());

        assertEquals(1, resultado.getBody().getContent().size());
        assertEquals(
                "Troca de Óleo",
                resultado.getBody().getContent().getFirst().nome());

        verify(listarServicosUseCase)
                .execute(any(Pageable.class));
    }

    @Test
    void deveAtualizarServico() {

        when(atualizarServicoUseCase.execute(eq(1L), any(ServicoInput.class)))
                .thenReturn(servicoOutput);

        ResponseEntity<ServicoResponse> resultado =
                servicoController.atualizar(servicoRequest,1L);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());

        ArgumentCaptor<ServicoInput> captor =
                ArgumentCaptor.forClass(ServicoInput.class);

        verify(atualizarServicoUseCase)
                .execute(eq(1L), captor.capture());

        ServicoInput input = captor.getValue();

        assertEquals("Troca de Óleo", input.nome());
        assertEquals("Substituição do óleo do motor", input.descricao());
        assertEquals(new BigDecimal("150.00"), input.valor());
    }

    @Test
    void deveInativarServico() {

        ResponseEntity<String> resultado =
                servicoController.inativar(1L);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(
                "Serviço inativado com sucesso",
                resultado.getBody());

        verify(inativarServicoUseCase)
                .execute(1L);
    }

    @Test
    void deveListarTempoMedioPorServico() {

        TempoMedioServicoMetricaOutput output =
                TempoMedioServicoMetricaOutput.builder()
                        .servicoId(1L)
                        .nomeServico("Troca de Óleo")
                        .quantidadeExecucoes(2L)
                        .tempoMedioSegundos(3600.0)
                        .tempoMedioMinutos(60.0)
                        .tempoMedioHoras(1.0)
                        .build();

        when(calcularTempoMedioServicoUseCase.execute())
                .thenReturn(List.of(output));

        ResponseEntity<List<TempoMedioServicoResponse>> resultado =
                servicoController.listarTempoMedioPorServico();

        assertEquals(HttpStatus.OK, resultado.getStatusCode());

        assertEquals(1, resultado.getBody().size());

        TempoMedioServicoResponse response =
                resultado.getBody().getFirst();

        assertEquals(1L, response.servicoId());
        assertEquals("Troca de Óleo", response.nomeServico());
        assertEquals(2L, response.quantidadeExecucoes());
        assertEquals(3600.0, response.tempoMedioSegundos());
        assertEquals(60.0, response.tempoMedioMinutos());
        assertEquals(1.0, response.tempoMedioHoras());

        verify(calcularTempoMedioServicoUseCase).execute();
    }

}