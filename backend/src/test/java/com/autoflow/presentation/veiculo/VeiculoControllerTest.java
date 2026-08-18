package com.autoflow.presentation.veiculo;

import com.autoflow.application.input.veiculo.CadastrarVeiculoCommand;
import com.autoflow.application.input.veiculo.PageInput;
import com.autoflow.application.input.veiculo.VeiculoInput;
import com.autoflow.application.output.veiculo.PageOutput;
import com.autoflow.application.output.veiculo.VeiculoOutput;
import com.autoflow.application.port.in.veiculo.*;
import com.autoflow.presentation.veiculo.request.VeiculoRequest;
import com.autoflow.presentation.veiculo.request.VeiculoUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private final VeiculoControllerMapper mapper =
            Mappers.getMapper(VeiculoControllerMapper.class);

    private VeiculoRequest cadastroRequest;
    private VeiculoUpdateRequest updateRequest;
    private VeiculoOutput veiculoOutput;

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

        cadastroRequest = new VeiculoRequest(
                "11222333000181",
                "Honda",
                2020,
                "ABC1234",
                "Civic");

        updateRequest = new VeiculoUpdateRequest(
                "Honda",
                2020,
                "ABC1234",
                "Civic");

        veiculoOutput = new VeiculoOutput(
                1L,
                "ABC1234",
                "Honda",
                "Civic",
                2020,
                null);
    }

    @Test
    void deveCadastrarVeiculo() throws Exception {

        when(cadastrarVeiculoUseCase.execute(any(CadastrarVeiculoCommand.class)))
                .thenReturn(veiculoOutput);

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastroRequest)))
                .andExpect(status().isCreated());

        verify(cadastrarVeiculoUseCase)
                .execute(any(CadastrarVeiculoCommand.class));
    }

    @Test
    void deveListarVeiculoPorId() throws Exception {

        when(buscarVeiculoUseCase.execute(1L))
                .thenReturn(veiculoOutput);

        mockMvc.perform(get("/veiculos/1"))
                .andExpect(status().isOk());

        verify(buscarVeiculoUseCase).execute(1L);
    }

    @Test
    void deveListarVeiculosSemFiltros() throws Exception {

        PageOutput<VeiculoOutput> pageOutput =
                new PageOutput<>(List.of(veiculoOutput), 0, 20, 1);

        when(listarVeiculosUseCase.execute(any(VeiculoInput.class), any(PageInput.class)))
                .thenReturn(pageOutput);

        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isOk());

        verify(listarVeiculosUseCase)
                .execute(any(VeiculoInput.class), any(PageInput.class));
    }

    @Test
    void deveListarVeiculosComFiltrosDePlacaEMarca() throws Exception {

        PageOutput<VeiculoOutput> pageOutput =
                new PageOutput<>(List.of(veiculoOutput), 0, 20, 1);

        when(listarVeiculosUseCase.execute(any(VeiculoInput.class), any(PageInput.class)))
                .thenReturn(pageOutput);

        mockMvc.perform(get("/veiculos")
                        .param("placa", "ABC1234")
                        .param("marca", "Honda"))
                .andExpect(status().isOk());

        verify(listarVeiculosUseCase)
                .execute(any(VeiculoInput.class), any(PageInput.class));
    }

    @Test
    void deveAtualizarVeiculo() throws Exception {

        when(atualizarVeiculoUseCase.execute(eq(1L), any(VeiculoInput.class)))
                .thenReturn(veiculoOutput);

        mockMvc.perform(patch("/veiculos/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(atualizarVeiculoUseCase)
                .execute(eq(1L), any(VeiculoInput.class));
    }

    @Test
    void deveRetornar400QuandoAtualizarSemPlaca() throws Exception {

        var requestSemPlaca = new VeiculoUpdateRequest(
                "Honda",
                2020,
                "",
                "Civic");

        mockMvc.perform(patch("/veiculos/1/atualizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSemPlaca)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(
                cadastrarVeiculoUseCase,
                buscarVeiculoUseCase,
                listarVeiculosUseCase,
                atualizarVeiculoUseCase,
                deletarVeiculoUseCase);
    }

    @Test
    void deveDeletarVeiculo() throws Exception {

        doNothing().when(deletarVeiculoUseCase).execute(1L);

        mockMvc.perform(delete("/veiculos/1"))
                .andExpect(status().isNoContent());

        verify(deletarVeiculoUseCase).execute(1L);
    }
}
