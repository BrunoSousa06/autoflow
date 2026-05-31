package com.autoflow.controller.ordemServico;

import com.autoflow.config.security.service.CustomUserDetailsService;
import com.autoflow.config.security.service.JwtService;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.ordemServico.ItemNecessarioEntity;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemServico.StatusOrdemServico;
import com.autoflow.mapper.ItensNecessariosMapperImpl;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.mapper.ServicoSolicitadoMapperImpl;
import com.autoflow.service.ordemServico.OrdemServicoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    private OrdemServicoService ordemServicoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveCriarOrdemServico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, "OS-123");

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
                .andExpect(jsonPath("$.dataAbertura").exists());

        ArgumentCaptor<List<ServicoSolicitadoEntity>> servicosCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(ordemServicoService).criar(eq(2L), servicosCaptor.capture());

        List<ServicoSolicitadoEntity> servicos = servicosCaptor.getValue();
        assertEquals(1, servicos.size());
        assertEquals(10L, servicos.getFirst().getServicoId());
        assertNull(servicos.getFirst().getNome());
        assertNull(servicos.getFirst().getValor());
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void deveIncluirServicosNaOrdemServico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, "OS-123");

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
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroOs").value("OS-123"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.dataAbertura").exists());

        ArgumentCaptor<List<ServicoSolicitadoEntity>> servicosCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(ordemServicoService).incluirServicos(eq(1L), servicosCaptor.capture());

        List<ServicoSolicitadoEntity> servicos = servicosCaptor.getValue();
        assertEquals(1, servicos.size());
        assertEquals(20L, servicos.getFirst().getServicoId());
        assertNull(servicos.getFirst().getNome());
        assertNull(servicos.getFirst().getValor());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveAtribuirMecanicoNaOrdemServico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, "OS-123");

        when(ordemServicoService.atribuirMecanico(1L, 2L))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/mecanico", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mecanicoId": 2
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroOs").value("OS-123"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.dataAbertura").exists());

        verify(ordemServicoService).atribuirMecanico(1L, 2L);
    }

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveIniciarDiagnosticoComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, "OS-123");
        ordemServico.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);

        when(ordemServicoService.iniciarDiagnostico(1L, "mecanico@autoflow.com"))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/diagnostico/iniciar", 1L)
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroOs").value("OS-123"))
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"))
                .andExpect(jsonPath("$.dataAbertura").exists());

        verify(ordemServicoService).iniciarDiagnostico(1L, "mecanico@autoflow.com");
    }

    @Test
    @WithMockUser(username = "admin@autoflow.com", roles = "ADMIN")
    void deveIniciarDiagnosticoComoAdmin() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, "OS-123");
        ordemServico.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);

        when(ordemServicoService.iniciarDiagnostico(1L, "admin@autoflow.com"))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/diagnostico/iniciar", 1L)
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroOs").value("OS-123"))
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"))
                .andExpect(jsonPath("$.dataAbertura").exists());

        verify(ordemServicoService).iniciarDiagnostico(1L, "admin@autoflow.com");
    }

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveRegistrarItensNecessariosComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, "OS-123");

        when(ordemServicoService.registrarItemNecessario(eq(1L), eq("mecanico@autoflow.com"), anyList()))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/itens-necessarios", 1L)
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
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroOs").value("OS-123"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.dataAbertura").exists());

        ArgumentCaptor<List<ItemNecessarioEntity>> itensCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(ordemServicoService).registrarItemNecessario(
                eq(1L),
                eq("mecanico@autoflow.com"),
                itensCaptor.capture()
        );

        List<ItemNecessarioEntity> itens = itensCaptor.getValue();
        assertEquals(1, itens.size());
        assertEquals(10L, itens.getFirst().getPecaInsumoId());
        assertEquals(2, itens.getFirst().getQuantidade());
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void deveRetornarForbiddenQuandoMecanicoTentarCriarOrdemServico() throws Exception {
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
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).criar(eq(2L), anyList());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornarForbiddenQuandoClienteTentarIncluirServicos() throws Exception {
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
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).incluirServicos(eq(1L), anyList());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornarForbiddenQuandoClienteTentarAtribuirMecanico() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/mecanico", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mecanicoId": 2
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).atribuirMecanico(eq(1L), anyLong());
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void deveRetornarForbiddenQuandoMecanicoTentarAtribuirMecanico() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/mecanico", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mecanicoId": 2
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).atribuirMecanico(eq(1L), anyLong());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarForbiddenQuandoAtendenteTentarIniciarDiagnostico() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/diagnostico/iniciar", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).iniciarDiagnostico(eq(1L), anyString());
    }

    @Test
    @WithMockUser(roles = "MECANICO", username = "mecanico@autoflow.com")
    void deveFinalizarDiagnosticoComoMecanico() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, "OS-123");
        ordemServico.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(ordemServicoService.finalizarDiagnostico(eq(1L), eq("mecanico@autoflow.com")))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/diagnostico/finalizar", 1L)
                        .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.numeroOs").value("OS-123"))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.dataAbertura").exists());

        verify(ordemServicoService).finalizarDiagnostico(1L, "mecanico@autoflow.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@autoflow.com")
    void deveFinalizarDiagnosticoComoAdmin() throws Exception {
        OrdemServicoEntity ordemServico = criarOrdemServico(1L, "OS-123");
        ordemServico.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(ordemServicoService.finalizarDiagnostico(eq(1L), eq("admin@autoflow.com")))
                .thenReturn(ordemServico);

        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/diagnostico/finalizar", 1L)
                        .with(csrf()))
                .andExpect(status().isAccepted());

        verify(ordemServicoService).finalizarDiagnostico(1L, "admin@autoflow.com");
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarForbiddenQuandoAtendenteTentarFinalizarDiagnostico() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/diagnostico/finalizar", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).finalizarDiagnostico(eq(1L), anyString());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornarForbiddenQuandoClienteTentarFinalizarDiagnostico() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/diagnostico/finalizar", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(ordemServicoService, never()).finalizarDiagnostico(eq(1L), anyString());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarForbiddenQuandoAtendenteTentarRegistrarItensNecessarios() throws Exception {
        mockMvc.perform(patch("/ordens-servico/{ordemServicoId}/itens-necessarios", 1L)
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

        verify(ordemServicoService, never()).registrarItemNecessario(eq(1L), anyString(), anyList());
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

    private OrdemServicoEntity criarOrdemServico(Long id, String numeroOs) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNome("Cliente 1");
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("cliente1@exemplo.com");
        cliente.setTelefone("11999999999");

        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(2L);
        veiculo.setCliente(cliente);

        OrdemServicoEntity ordemServico = OrdemServicoEntity.criar(
                veiculo,
                List.of(new ServicoSolicitadoEntity(10L, "Revisao", new BigDecimal("100.00")))
        );

        ordemServico.setId(id);
        ordemServico.setNumeroOs(numeroOs);
        ordemServico.setDataAbertura(LocalDateTime.of(2026, 5, 30, 10, 0));

        return ordemServico;
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
        @ResponseStatus(HttpStatus.FORBIDDEN)
        void handleAuthorizationDenied() {
        }
    }

}
