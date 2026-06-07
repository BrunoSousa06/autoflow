package com.autoflow.controller.ordemservico.reparoadicional;

import com.autoflow.config.security.service.CustomUserDetailsService;
import com.autoflow.config.security.service.JwtService;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.mapper.ServicoSolicitadoMapper;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import com.autoflow.service.ordemservico.reparoadicional.impl.CriarReparoAdicionalResult;
import org.junit.jupiter.api.Test;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReparoAdicionalController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        ReparoAdicionalControllerTest.MethodSecurityTestConfig.class,
        ReparoAdicionalControllerTest.SecurityExceptionHandler.class
})
class ReparoAdicionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReparoAdicionalService reparoAdicionalService;

    @MockitoBean
    private ServicoSolicitadoMapper servicoSolicitadoMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveCriarReparoAdicionalComoMecanico() throws Exception {
        List<ServicoSolicitadoEntity> servicos = List.of(
                new ServicoSolicitadoEntity(10L, "Troca de pastilha", new BigDecimal("120.00"))
        );
        when(servicoSolicitadoMapper.mapToEntities(any())).thenReturn(servicos);
        when(reparoAdicionalService.criar(
                "OS-123",
                "mecanico@autoflow.com",
                servicos
        )).thenReturn(new CriarReparoAdicionalResult(
                5L,
                20L,
                "http://localhost:8080/public/orcamentos/20?token=abc"
        ));

        mockMvc.perform(post("/ordens-servico/{numeroOs}/reparos-adicionais", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "servicos": [
                                    {
                                      "servicoId": 10
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reparoAdicionalId").value(5L))
                .andExpect(jsonPath("$.orcamentoId").value(20L))
                .andExpect(jsonPath("$.publicUrl").value("http://localhost:8080/public/orcamentos/20?token=abc"));

        verify(servicoSolicitadoMapper).mapToEntities(any());
        verify(reparoAdicionalService).criar("OS-123", "mecanico@autoflow.com", servicos);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarReparoAdicionalComoAdmin() throws Exception {
        List<ServicoSolicitadoEntity> servicos = List.of(
                new ServicoSolicitadoEntity(10L, "Troca de pastilha", new BigDecimal("120.00"))
        );
        when(servicoSolicitadoMapper.mapToEntities(any())).thenReturn(servicos);
        when(reparoAdicionalService.criar("OS-123", "user", servicos))
                .thenReturn(new CriarReparoAdicionalResult(5L, 20L, "url"));

        mockMvc.perform(post("/ordens-servico/{numeroOs}/reparos-adicionais", "OS-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "servicos": [
                                    {
                                      "servicoId": 10
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reparoAdicionalId").value(5L));

        verify(reparoAdicionalService).criar("OS-123", "user", servicos);
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    void deveRetornarBadRequestQuandoServicoNaoForInformado() throws Exception {
        mockMvc.perform(post("/ordens-servico/{numeroOs}/reparos-adicionais", "OS-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "servicos": []
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicoSolicitadoMapper);
        verifyNoInteractions(reparoAdicionalService);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornarForbiddenQuandoClienteTentarCriarReparoAdicional() throws Exception {
        mockMvc.perform(post("/ordens-servico/{numeroOs}/reparos-adicionais", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "servicos": [
                                    {
                                      "servicoId": 10
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(servicoSolicitadoMapper, never()).mapToEntities(any());
        verify(reparoAdicionalService, never()).criar(any(), any(), any());
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
