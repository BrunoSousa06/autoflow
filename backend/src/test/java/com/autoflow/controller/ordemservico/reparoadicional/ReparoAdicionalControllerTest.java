package com.autoflow.controller.ordemservico.reparoadicional;

import com.autoflow.infrastructure.persistence.security.service.CustomUserDetailsService;
import com.autoflow.infrastructure.persistence.security.service.JwtService;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.infrastructure.persistence.mapper.ItensNecessariosMapper;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import com.autoflow.service.ordemservico.reparoadicional.impl.CriarReparoAdicionalResult;
import org.junit.jupiter.api.Test;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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
    private ItensNecessariosMapper itensNecessariosMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "mecanico@autoflow.com", roles = "MECANICO")
    void deveCriarReparoAdicionalComoMecanico() throws Exception {
        List<ItemNecessarioEntity> itens = List.of(itemNecessario(7L, 2));
        when(itensNecessariosMapper.mapToEntities(any())).thenReturn(itens);
        when(reparoAdicionalService.criar(
                eq("OS-123"),
                eq("mecanico@autoflow.com"),
                any()
        )).thenReturn(new CriarReparoAdicionalResult(
                5L,
                20L,
                "http://localhost:8080/public/orcamentos/20?token=abc"
        ));

        mockMvc.perform(post("/ordens-servico/{numeroOs}/reparos-adicionais", "OS-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "servicos": [
                                    {
                                      "servicoId": 10,
                                      "itensNecessarios": [
                                        {
                                          "pecaInsumoId": 7,
                                          "quantidade": 2
                                        }
                                      ]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reparoAdicionalId").value(5L))
                .andExpect(jsonPath("$.orcamentoId").value(20L))
                .andExpect(jsonPath("$.publicUrl").value("http://localhost:8080/public/orcamentos/20?token=abc"));

        verify(itensNecessariosMapper).mapToEntities(any());
        verify(reparoAdicionalService).criar(eq("OS-123"), eq("mecanico@autoflow.com"), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarReparoAdicionalComoAdmin() throws Exception {
        when(itensNecessariosMapper.mapToEntities(any())).thenReturn(List.of(itemNecessario(7L, 2)));
        when(reparoAdicionalService.criar(eq("OS-123"), eq("user"), any()))
                .thenReturn(new CriarReparoAdicionalResult(5L, 20L, "url"));

        mockMvc.perform(post("/ordens-servico/{numeroOs}/reparos-adicionais", "OS-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "servicos": [
                                    {
                                      "servicoId": 10,
                                      "itensNecessarios": [
                                        {
                                          "pecaInsumoId": 7,
                                          "quantidade": 2
                                        }
                                      ]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reparoAdicionalId").value(5L));

        verify(reparoAdicionalService).criar(eq("OS-123"), eq("user"), any());
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

        verifyNoInteractions(itensNecessariosMapper);
        verifyNoInteractions(reparoAdicionalService);
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deveRetornarForbiddenQuandoClienteTentarCriarReparoAdicional() throws Exception {
        mockMvc.perform(post("/ordens-servico/{numeroOs}/reparos-adicionais", "OS-123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "servicos": [
                                    {
                                      "servicoId": 10,
                                      "itensNecessarios": [
                                        {
                                          "pecaInsumoId": 7,
                                          "quantidade": 2
                                        }
                                      ]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(itensNecessariosMapper, never()).mapToEntities(any());
        verify(reparoAdicionalService, never()).criar(any(), any(), any());
    }

    private ItemNecessarioEntity itemNecessario(Long pecaInsumoId, Integer quantidade) {
        ItemNecessarioEntity item = new ItemNecessarioEntity();
        item.setPecaInsumoId(pecaInsumoId);
        item.setQuantidade(quantidade);
        return item;
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
