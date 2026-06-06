package com.autoflow.controller.ordemservico;

import com.autoflow.config.security.service.CustomUserDetailsService;
import com.autoflow.config.security.service.JwtService;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.ordemservico.StatusServicoOs;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.mapper.ItensNecessariosMapperImpl;
import com.autoflow.mapper.ServicoSolicitadoMapperImpl;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import com.autoflow.service.ordemservico.impl.OrdemServicoServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        when(ordemServicoService.criar(eq(2L), anyList()))
                .thenReturn(ordemServico);

        mockMvc.perform(post("/ordens-servico")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "veiculoId": 2,
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

        ArgumentCaptor<List<ServicoSolicitadoEntity>> servicosCaptor = captorDeLista();
        verify(ordemServicoService).criar(eq(2L), servicosCaptor.capture());
        assertEquals(10L, servicosCaptor.getValue().getFirst().getServicoId());
        assertNull(servicosCaptor.getValue().getFirst().getNome());
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void deveIncluirServicosNaOrdemServico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");

        when(ordemServicoService.incluirServicos(eq(1L), anyList()))
                .thenReturn(ordemServico);

        mockMvc.perform(post("/ordens-servico/{ordemServicoId}/servicos", 1L)
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

        verify(ordemServicoService).incluirServicos(eq(1L), anyList());
    }

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveRegistrarItensNecessariosComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");

        when(ordemServicoService.registrarItemNecessario(eq(1L), eq(55L), eq("mecanico@autoflow.com"), anyList()))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/servicos/{servicoOsId}/itens-necessarios", 1L, 55L)
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
        verify(ordemServicoService).registrarItemNecessario(
                eq(1L),
                eq(55L),
                eq("mecanico@autoflow.com"),
                itensCaptor.capture()
        );
        assertEquals(10L, itensCaptor.getValue().getFirst().getPecaInsumoId());
        assertEquals(2, itensCaptor.getValue().getFirst().getQuantidade());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveIniciarServicoComoAtendente() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ordemServico.getServicosSolicitados().getFirst().setStatus(StatusServicoOs.EM_EXECUCAO);

        when(ordemServicoService.iniciarServico(1L, 55L)).thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/servicos/{servicoOsId}/iniciar", 1L, 55L)
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.servicos[0].status").value("EM_EXECUCAO"));

        verify(ordemServicoService).iniciarServico(1L, 55L);
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void deveFinalizarServicoComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        ordemServico.getServicosSolicitados().getFirst().setStatus(StatusServicoOs.FINALIZADO);

        when(ordemServicoService.finalizarServico(1L, 55L)).thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/servicos/{servicoOsId}/finalizar", 1L, 55L)
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.servicos[0].status").value("FINALIZADO"));

        verify(ordemServicoService).finalizarServico(1L, 55L);
    }

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveFinalizarDiagnosticoComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, 55L, "OS-123");
        ordemServico.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(ordemServicoService.finalizarDiagnostico(1L, "mecanico@autoflow.com"))
                .thenReturn(new FinalizarDiagnosticoResult(
                        ordemServico,
                        10L,
                        "http://localhost:8080/public/orcamentos/10?token=abc"
                ));

        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/diagnostico/finalizar", 1L)
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.ordemServico.id").value(1L))
                .andExpect(jsonPath("$.orcamentoId").value(10L));

        verify(ordemServicoService).finalizarDiagnostico(1L, "mecanico@autoflow.com");
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornarForbiddenQuandoClienteTentarIniciarServico() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/servicos/{servicoOsId}/iniciar", 1L, 55L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).iniciarServico(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarForbiddenQuandoAtendenteTentarRegistrarItensNecessarios() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/servicos/{servicoOsId}/itens-necessarios", 1L, 55L)
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

        verify(ordemServicoService, never()).registrarItemNecessario(anyLong(), anyLong(), anyString(), anyList());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarBadRequestQuandoCriarOrdemSemServicos() throws Exception {
        mockMvc.perform(post("/ordens-servico")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "veiculoId": 2,
                                  "servicosSolicitados": []
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

        OrdemServicoEntity ordemServico = OrdemServicoEntity.criar(veiculo);
        ServicoSolicitadoEntity servico = ServicoSolicitadoEntity.criar(10L, "Revisao", new BigDecimal("100.00"));
        servico.setId(servicoOsId);
        ordemServico.adicionarServicos(List.of(servico));

        ordemServico.setId(id);
        ordemServico.setNumeroOs(numeroOs);
        ordemServico.setDataAbertura(LocalDateTime.of(2026, 5, 30, 10, 0));

        return ordemServico;
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
