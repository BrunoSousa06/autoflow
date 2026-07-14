package com.autoflow.presentation.veiculo;

import com.autoflow.application.dto.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.usecases.veiculo.*;
import com.autoflow.mapper.VeiculoControllerMapper;
import com.autoflow.presentation.veiculo.request.VeiculoRequest;
import com.autoflow.presentation.veiculo.request.VeiculoUpdateRequest;
import com.autoflow.presentation.veiculo.response.VeiculoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VeiculoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CadastrarVeiculoUseCase cadastrarVeiculoUseCase;
    @Mock
    private BuscarVeiculoUseCase buscarVeiculoUseCase;
    @Mock
    private ListarVeiculosUseCase listarVeiculosUseCase;
    @Mock
    private AtualizarVeiculoUseCase atualizarVeiculoUseCase;
    @Mock
    private DeletarVeiculoUseCase deletarVeiculoUseCase;
    @Mock
    private VeiculoControllerMapper mapper;

    private VeiculoRequest cadastroRequest;
    private VeiculoUpdateRequest updateRequest;
    private VeiculoResponse response;
    private VeiculoOutput veiculoOutput;
    private CadastrarVeiculoInput cadastrarVeiculoInput;
    private VeiculoInput veiculoInput;


    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VeiculoController(
                        cadastrarVeiculoUseCase,
                        buscarVeiculoUseCase,
                        listarVeiculosUseCase,
                        atualizarVeiculoUseCase,
                        deletarVeiculoUseCase,
                        mapper))
                .build();

        cadastroRequest = new VeiculoRequest("11222333000181", "Honda", 2020, "ABC1234", "Civic");
        updateRequest = new VeiculoUpdateRequest("Honda", 2020, "ABC1234", "Civic");
        response = new VeiculoResponse(1L, "Honda", 2020, "ABC1234", "Civic", null);
        // Corrected VeiculoOutput constructor parameters: id, placa, marca, modelo, ano, clienteOutput
        veiculoOutput = new VeiculoOutput(1L, "ABC1234", "Honda", "Civic", 2020, null);
        // CadastrarVeiculoInput constructor parameters: cpfCnpj, placa, marca, modelo, ano
        cadastrarVeiculoInput = new CadastrarVeiculoInput("11222333000181", "ABC1234", "Honda", "Civic", 2020);
        veiculoInput = new VeiculoInput("ABC1234", 2020, "Civic", "Honda");
    }

    @Test
    void deveCadastrarVeiculo() throws Exception {
        when(mapper.toInput(cadastroRequest)).thenReturn(cadastrarVeiculoInput);
        when(cadastrarVeiculoUseCase.execute(cadastrarVeiculoInput)).thenReturn(veiculoOutput);
        when(mapper.toResponse(veiculoOutput)).thenReturn(response);

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastroRequest)))
                .andExpect(status().isCreated());

        verify(mapper).toInput(cadastroRequest);
        verify(cadastrarVeiculoUseCase).execute(cadastrarVeiculoInput);
        verify(mapper).toResponse(veiculoOutput);
    }

    @Test
    void deveListarVeiculoPorId() throws Exception {
        when(buscarVeiculoUseCase.execute(1L)).thenReturn(veiculoOutput);
        when(mapper.toResponse(veiculoOutput)).thenReturn(response);

        mockMvc.perform(get("/veiculos/1"))
                .andExpect(status().isOk());

        verify(buscarVeiculoUseCase).execute(1L);
        verify(mapper).toResponse(veiculoOutput);
    }

    @Test
    void deveListarVeiculosSemFiltros() throws Exception {
        PageImpl<VeiculoOutput> pageOutput = new PageImpl<>(List.of(veiculoOutput), PageRequest.of(0, 20), 1);
        when(listarVeiculosUseCase.execute(any(VeiculoInput.class), any(Pageable.class)))
                .thenReturn(pageOutput);
        when(mapper.toResponse(any(VeiculoOutput.class))).thenReturn(response);

        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isOk());

        verify(listarVeiculosUseCase).execute(any(VeiculoInput.class), any(Pageable.class));
        verify(mapper, times(pageOutput.getContent().size())).toResponse(any(VeiculoOutput.class));
    }

    @Test
    void deveListarVeiculosComFiltrosDePlacaEMarca() throws Exception {
        PageImpl<VeiculoOutput> pageOutput = new PageImpl<>(List.of(veiculoOutput), PageRequest.of(0, 20), 1);
        when(listarVeiculosUseCase.execute(any(VeiculoInput.class), any(Pageable.class)))
                .thenReturn(pageOutput);
        when(mapper.toResponse(any(VeiculoOutput.class))).thenReturn(response);

        mockMvc.perform(get("/veiculos").param("placa", "ABC1234").param("marca", "Honda"))
                .andExpect(status().isOk());

        verify(listarVeiculosUseCase).execute(any(VeiculoInput.class), any(Pageable.class));
        verify(mapper, times(pageOutput.getContent().size())).toResponse(any(VeiculoOutput.class));
    }

    @Test
    void deveAtualizarVeiculo() throws Exception {
        when(mapper.toInput(any(VeiculoUpdateRequest.class))).thenReturn(veiculoInput);
        when(atualizarVeiculoUseCase.execute(eq(1L), any(VeiculoInput.class))).thenReturn(veiculoOutput);
        when(mapper.toResponse(veiculoOutput)).thenReturn(response);

        mockMvc.perform(patch("/veiculos/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(mapper).toInput(any(VeiculoUpdateRequest.class));
        verify(atualizarVeiculoUseCase).execute(eq(1L), any(VeiculoInput.class));
        verify(mapper).toResponse(veiculoOutput);
    }

    @Test
    void deveRetornar400QuandoAtualizarSemPlaca() throws Exception {
        var requestSemPlaca = new VeiculoUpdateRequest("Honda", 2020, "", "Civic");

        mockMvc.perform(patch("/veiculos/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSemPlaca)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cadastrarVeiculoUseCase, buscarVeiculoUseCase, listarVeiculosUseCase, atualizarVeiculoUseCase, deletarVeiculoUseCase);
    }

    @Test
    void deveDeletarVeiculo() throws Exception {
        doNothing().when(deletarVeiculoUseCase).execute(1L);

        mockMvc.perform(delete("/veiculos/1"))
                .andExpect(status().isNoContent()); // Changed from isOk() to isNoContent()

        verify(deletarVeiculoUseCase).execute(1L);
    }
}