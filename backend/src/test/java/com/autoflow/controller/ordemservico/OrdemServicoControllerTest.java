package com.autoflow.controller.ordemservico;

import com.autoflow.config.security.service.CustomUserDetailsService;
import com.autoflow.config.security.service.JwtService;
import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.controller.ordemservico.response.TempoMedioOrdemServicoResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.mapper.ItensNecessariosMapperImpl;
import com.autoflow.mapper.ServicoSolicitadoMapperImpl;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import com.autoflow.service.ordemservico.dto.OrdemServicoFiltro;
import com.autoflow.service.ordemservico.impl.OrdemServicoServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrdemServicoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        ItensNecessariosMapperImpl.class,
        ServicoSolicitadoMapperImpl.class,
        OrdemServicoControllerTest.MethodSecurityTestConfig.class,
        OrdemServicoControllerTest.SecurityExceptionHandler.class
})
class OrdemServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdemServicoServiceImpl ordemServicoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveCriarOrdemServico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");

        when(ordemServicoService.criar(eq("52998224725"), any(VeiculoOrdemServicoRequest.class), anyList()))
                .thenReturn(ordemServico);

        mockMvc.perform(post("/ordens-servico")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpfCnpj": "52998224725",
                                  "veiculo": {
                                    "placa": "NEX0517",
                                    "marca": "Honda",
                                    "modelo": "Civic",
                                    "ano": 2020
                                  },
                                  "servicosSolicitados": [
                                    {
                                      "servicoId": 10
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroOs").value("OS-123"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.servicos[0].id").value(55L));

        ArgumentCaptor<VeiculoOrdemServicoRequest> veiculoCaptor = ArgumentCaptor.forClass(VeiculoOrdemServicoRequest.class);
        ArgumentCaptor<List<ServicoSolicitadoEntity>> servicosCaptor = captorDeLista();
        verify(ordemServicoService).criar(eq("52998224725"), veiculoCaptor.capture(), servicosCaptor.capture());
        assertEquals("NEX0517", veiculoCaptor.getValue().placa());
        assertEquals("Honda", veiculoCaptor.getValue().marca());
        assertEquals("Civic", veiculoCaptor.getValue().modelo());
        assertEquals(2020, veiculoCaptor.getValue().ano());
        assertEquals(10L, servicosCaptor.getValue().getFirst().getServicoId());
        assertNull(servicosCaptor.getValue().getFirst().getNome());
    }

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveIncluirServicosNaOrdemServico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");

        when(ordemServicoService.incluirServicos(eq("OS-123"), anyList(), eq("mecanico@autoflow.com")))
                .thenReturn(ordemServico);

        mockMvc.perform(post("/ordens-servico/{numeroOs}/servicos", "OS-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "servicoId": 20
                                  }
                                ]
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L));

        verify(ordemServicoService).incluirServicos(eq("OS-123"), anyList(), eq("mecanico@autoflow.com"));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveAtribuirMecanicoPorEmail() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");

        when(ordemServicoService.atribuirMecanico(eq("OS-123"), isNull(), eq("mecanico@autoflow.com")))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{numeroOs}/mecanico", "OS-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mecanicoEmail": "mecanico@autoflow.com"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L));

        verify(ordemServicoService).atribuirMecanico("OS-123", null, "mecanico@autoflow.com");
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveAtribuirMecanicoPorId() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");

        when(ordemServicoService.atribuirMecanico(eq("OS-123"), eq(2L), isNull()))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{numeroOs}/mecanico", "OS-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mecanicoId": 2
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L));

        verify(ordemServicoService).atribuirMecanico("OS-123", 2L, null);
    }

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveRegistrarItensNecessariosComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");

        when(ordemServicoService.registrarItemNecessario(eq("342-sb"),eq(55L), eq("mecanico@autoflow.com"),anyList()))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{numeroOs}/servicos/{servicoOsId}/itens-necessarios", "342-sb", 55L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "pecaInsumoId": 10,
                                    "quantidade": 2
                                  }
                                ]
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L));

        ArgumentCaptor<List<ItemNecessarioEntity>> itensCaptor = captorDeLista();
        verify(ordemServicoService).registrarItemNecessario(eq("342-sb"),
                eq(55L),
                eq("mecanico@autoflow.com"),
                itensCaptor.capture());
        assertEquals(10L, itensCaptor.getValue().getFirst().getPecaInsumoId());
        assertEquals(2, itensCaptor.getValue().getFirst().getQuantidade());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveBloquearInicioServicoParaAtendente() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{numeroOs}/servicos/{servicoOsId}/iniciar", "OS-123", 55L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).iniciarServico(any(), any());
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void deveFinalizarServicoComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        ordemServico.getServicosSolicitados().getFirst().setStatus(StatusServicoOs.FINALIZADO);

        when(ordemServicoService.finalizarServico("OS-123", 55L)).thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{numeroOs}/servicos/{servicoOsId}/finalizar", "OS-123", 55L)
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.servicos[0].status").value("FINALIZADO"));

        verify(ordemServicoService).finalizarServico("OS-123", 55L);
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveEntregarOrdemServicoComoAtendente() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        ordemServico.setStatus(StatusOrdemServico.FINALIZADA);
        ordemServico.entregar();

        when(ordemServicoService.entregar("OS-123")).thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{numeroOs}/entregar", "OS-123")
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ENTREGUE"))
                .andExpect(jsonPath("$.entregueEm").exists());

        verify(ordemServicoService).entregar("OS-123");
    }

    @Test
    @WithMockUser(username = "admin@autoflow.com", roles = "ADMIN")
    void deveListarOrdensServicoComoAdmin() throws Exception {
        OrdemServicoEntity primeiraOrdem = criarOrdemServico(1L, 55L, "OS-123");
        OrdemServicoEntity segundaOrdem = criarOrdemServico(2L, 66L, "OS-456");
        var page = new PageImpl<>(List.of(primeiraOrdem, segundaOrdem));

        when(ordemServicoService.listar(any(OrdemServicoFiltro.class), any(Pageable.class), anyString())).thenReturn(page);

        mockMvc.perform(get("/ordens-servico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].status").value("RECEBIDA"))
                .andExpect(jsonPath("$.content[0].clienteNome").value("Cliente 1"))
                .andExpect(jsonPath("$.content[0].servicos[0].id").value(55L))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].numeroOs").value("OS-456"))
                .andExpect(jsonPath("$.content[1].servicos[0].id").value(66L))
                .andExpect(jsonPath("$.page.totalElements").value(2));

        verify(ordemServicoService).listar(any(OrdemServicoFiltro.class), any(Pageable.class), eq("admin@autoflow.com"));
    }

    @Test
    @WithMockUser(username = "atendente@autoflow.com", roles = "ATENDENTE")
    void deveListarOrdensServicoComFiltroDeCliente() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        var page = new PageImpl<>(List.of(ordemServico));

        when(ordemServicoService.listar(any(OrdemServicoFiltro.class), any(Pageable.class), anyString())).thenReturn(page);

        mockMvc.perform(get("/ordens-servico").param("cliente", "Cliente 1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].clienteNome").value("Cliente 1"))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        verify(ordemServicoService).listar(
                argThat(f -> "Cliente 1".equals(f.cliente())),
                any(Pageable.class),
                anyString()
        );
    }

    @Test
    @WithMockUser(username = "atendente@autoflow.com", roles = "ATENDENTE")
    void deveListarOrdensServicoComFiltroDeStatus() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        var page = new PageImpl<>(List.of(ordemServico));

        when(ordemServicoService.listar(any(OrdemServicoFiltro.class), any(Pageable.class), anyString())).thenReturn(page);

        mockMvc.perform(get("/ordens-servico").param("status", "RECEBIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("RECEBIDA"));

        verify(ordemServicoService).listar(
                argThat(f -> StatusOrdemServico.RECEBIDA.equals(f.status())),
                any(Pageable.class),
                anyString()
        );
    }

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveListarOrdensServicoComoMecanicoPassandoSeuEmail() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        var page = new PageImpl<>(List.of(ordemServico));

        when(ordemServicoService.listar(any(OrdemServicoFiltro.class), any(Pageable.class), eq("mecanico@autoflow.com"))).thenReturn(page);

        mockMvc.perform(get("/ordens-servico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1));

        verify(ordemServicoService).listar(any(OrdemServicoFiltro.class), any(Pageable.class), eq("mecanico@autoflow.com"));
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveDetalharOrdemServicoComoAtendente() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        OrcamentoEntity orcamento = criarOrcamento(10L, ordemServico.getNumeroOs());

        when(ordemServicoService.buscaOrdemServicoPorNumeroOs("OS-123")).thenReturn(ordemServico);
        when(ordemServicoService.buscarOrcamentoAtual("OS-123")).thenReturn(orcamento);

        mockMvc.perform(get("/ordens-servico/{numeroOs}", "OS-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroOs").value("OS-123"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.cliente.id").value(1L))
                .andExpect(jsonPath("$.cliente.nome").value("Cliente 1"))
                .andExpect(jsonPath("$.cliente.cpfCnpj").value("12345678901"))
                .andExpect(jsonPath("$.cliente.email").value("cliente1@exemplo.com"))
                .andExpect(jsonPath("$.cliente.telefone").value("11999999999"))
                .andExpect(jsonPath("$.veiculo.id").value(2L))
                .andExpect(jsonPath("$.veiculo.placa").value("ABC1D23"))
                .andExpect(jsonPath("$.veiculo.marca").value("Honda"))
                .andExpect(jsonPath("$.veiculo.modelo").value("Civic"))
                .andExpect(jsonPath("$.veiculo.ano").value(2020))
                .andExpect(jsonPath("$.servicos[0].id").value(55L))
                .andExpect(jsonPath("$.servicos[0].servicoId").value(10L))
                .andExpect(jsonPath("$.servicos[0].nome").value("Revisao"))
                .andExpect(jsonPath("$.orcamentoAtual.id").value(10L))
                .andExpect(jsonPath("$.orcamentoAtual.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.orcamentoAtual.totalGeral").value(100.00));

        verify(ordemServicoService).buscaOrdemServicoPorNumeroOs("OS-123");
        verify(ordemServicoService).buscarOrcamentoAtual("OS-123");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCalcularTempoMedioFinalizacaoComoAdmin() throws Exception {
        TempoMedioOrdemServicoResponse response = new TempoMedioOrdemServicoResponse(
                3L,
                7200.0,
                120.0,
                2.0
        );
        when(ordemServicoService.calcularTempoMedioFinalizacao()).thenReturn(response);

        mockMvc.perform(get("/ordens-servico/metricas/tempo-medio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeOrdensFinalizadas").value(3L))
                .andExpect(jsonPath("$.tempoMedioSegundos").value(7200.0))
                .andExpect(jsonPath("$.tempoMedioMinutos").value(120.0))
                .andExpect(jsonPath("$.tempoMedioHoras").value(2.0));

        verify(ordemServicoService).calcularTempoMedioFinalizacao();
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarForbiddenQuandoAtendenteTentarCalcularTempoMedioFinalizacao() throws Exception {
        mockMvc.perform(get("/ordens-servico/metricas/tempo-medio"))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).calcularTempoMedioFinalizacao();
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornarForbiddenQuandoClienteTentarListarOrdensServico() throws Exception {
        mockMvc.perform(get("/ordens-servico"))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).listar(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornarForbiddenQuandoClienteTentarDetalharOrdemServico() throws Exception {
        mockMvc.perform(get("/ordens-servico/{numeroOs}", "OS-123"))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).buscaOrdemServicoPorNumeroOs(anyString());
        verify(ordemServicoService, never()).buscarOrcamentoAtual(anyString());
    }

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveFinalizarDiagnosticoComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        ordemServico.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(ordemServicoService.finalizarDiagnostico("OS-123", "mecanico@autoflow.com"))
                .thenReturn(new FinalizarDiagnosticoResult(
                        ordemServico,
                        10L,
                        "http://localhost:8080/public/orcamentos/10?token=abc"
                ));

        mockMvc.perform(patch("/ordens-servico/{numeroOs}/diagnostico/finalizar", "OS-123")
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.ordemServico.id").value(1L))
                .andExpect(jsonPath("$.orcamentoId").value(10L));

        verify(ordemServicoService).finalizarDiagnostico("OS-123", "mecanico@autoflow.com");
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornarForbiddenQuandoClienteTentarIniciarServico() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{numeroOs}/servicos/{servicoOsId}/iniciar", "OS-123", 55L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).iniciarServico(anyString(), anyLong());
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void deveRetornarForbiddenQuandoMecanicoTentarEntregarOrdemServico() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{numeroOs}/entregar", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).entregar(anyString());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarForbiddenQuandoAtendenteTentarRegistrarItensNecessarios() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{numeroOs}/servicos/{servicoOsId}/itens-necessarios", "OS-123", 55L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "pecaInsumoId": 10,
                                    "quantidade": 2
                                  }
                                ]
                                """))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).registrarItemNecessario(anyString(),anyLong(), anyString(), anyList());
    }

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveIniciarDiagnosticoComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        ordemServico.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);

        when(ordemServicoService.iniciarDiagnostico("OS-123", "mecanico@autoflow.com"))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{numeroOs}/diagnostico/iniciar", "OS-123")
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));

        verify(ordemServicoService).iniciarDiagnostico("OS-123", "mecanico@autoflow.com");
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarForbiddenQuandoAtendenteTentarIniciarDiagnostico() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{numeroOs}/diagnostico/iniciar", "OS-123")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).iniciarDiagnostico(anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveRegistrarLaudoComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");

        when(ordemServicoService.registrarLaudo("OS-123", "mecanico@autoflow.com", "Motor com desgaste"))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{numeroOs}/diagnostico/laudo", "OS-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "laudo": "Motor com desgaste"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L));

        verify(ordemServicoService).registrarLaudo("OS-123", "mecanico@autoflow.com", "Motor com desgaste");
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void deveIniciarServicoComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        ordemServico.getServicosSolicitados().getFirst().setStatus(StatusServicoOs.EM_EXECUCAO);

        when(ordemServicoService.iniciarServico("OS-123", 55L)).thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{numeroOs}/servicos/{servicoOsId}/iniciar", "OS-123", 55L)
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.servicos[0].status").value("EM_EXECUCAO"));

        verify(ordemServicoService).iniciarServico("OS-123", 55L);
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarBadRequestQuandoCriarOrdemSemServicos() throws Exception {
        mockMvc.perform(post("/ordens-servico")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpfCnpj": "52998224725",
                                  "veiculo": {
                                    "placa": "ABC1D23"
                                  },
                                  "servicosSolicitados": []
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordemServicoService);
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarBadRequestQuandoCriarOrdemSemVeiculo() throws Exception {
        mockMvc.perform(post("/ordens-servico")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpfCnpj": "52998224725",
                                  "servicosSolicitados": [
                                    {
                                      "servicoId": 10
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordemServicoService);
    }

    private OrdemServicoEntity criarOrdemServico(Long id, Long servicoOsId, String numeroOs) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNome("Cliente 1");
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("cliente1@exemplo.com");
        cliente.setTelefone("11999999999");

        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(2L);
        veiculo.setCliente(cliente);
        veiculo.setPlaca("ABC1D23");
        veiculo.setMarca("Honda");
        veiculo.setModelo("Civic");
        veiculo.setAno(2020);

        OrdemServicoEntity ordemServico = OrdemServicoEntity.criar(cliente, veiculo);
        ServicoSolicitadoEntity servico = ServicoSolicitadoEntity.criar(10L, "Revisao", new BigDecimal("100.00"));
        servico.setId(servicoOsId);
        ordemServico.adicionarServicosSolicitados(List.of(servico));

        ordemServico.setId(id);
        ordemServico.setNumeroOs(numeroOs);
        ordemServico.setDataAbertura(LocalDateTime.of(2026, 5, 30, 10, 0));

        return ordemServico;
    }

    private OrcamentoEntity criarOrcamento(Long id, String numeroOs) {
        OrcamentoEntity orcamento = new OrcamentoEntity();
        orcamento.setId(id);
        orcamento.setNumeroOs(numeroOs);
        orcamento.setTipo(TipoOrcamento.PRINCIPAL);
        orcamento.setVersao(1);
        orcamento.setStatus(StatusOrcamento.DISPONIVEL);
        orcamento.setCriadoEm(LocalDateTime.of(2026, 5, 30, 11, 0));
        orcamento.setDisponibilizadoEm(LocalDateTime.of(2026, 5, 30, 12, 0));
        orcamento.setTotalServicos(new BigDecimal("100.00"));
        orcamento.setTotalItens(BigDecimal.ZERO);
        orcamento.setTotalGeral(new BigDecimal("100.00"));
        return orcamento;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> ArgumentCaptor<List<T>> captorDeLista() {
        return ArgumentCaptor.forClass((Class) List.class);
    }

    @TestConfiguration
    @EnableMethodSecurity(proxyTargetClass = true)
    static class MethodSecurityTestConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @RestControllerAdvice
    static class SecurityExceptionHandler {

        @ExceptionHandler(AuthorizationDeniedException.class)
        ResponseEntity<Void> handleAuthorizationDenied() {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
