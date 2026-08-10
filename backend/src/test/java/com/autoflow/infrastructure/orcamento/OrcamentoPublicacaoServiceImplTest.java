package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.dto.orcamento.OrcamentoPublicacao;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoPublicacaoServiceImplTest {

    @Mock
    OrcamentoGateway orcamentoGateway;

    @Mock
    Clock clock;

    @InjectMocks
    OrcamentoPublicacaoAdapter service;

    @Test
    void publicar_deveGerarTokenHashEDefinirDisponibilizadoEm() {
        OrcamentoEntity orc = new OrcamentoEntity();
        orc.setId(10L);
        orc.setStatus(StatusOrcamento.DISPONIVEL);
        orc.setDisponibilizadoEm(null);

        when(orcamentoGateway.findById(10L)).thenReturn(Optional.of(orc));
        when(orcamentoGateway.save(any(OrcamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(service, "publicBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "tokenSecret", "local-secret");
        ReflectionTestUtils.setField(service, "frontendPublicBaseUrl", "http://localhost:4200");
        when(clock.instant()).thenReturn(Instant.parse("2026-06-04T12:00:00Z"));

        OrcamentoPublicacao result = service.publicarComLinks(10L);

        assertNotNull(orc.getPublicTokenHash());
        assertEquals(LocalDateTime.ofInstant(Instant.parse("2026-06-11T12:00:00Z"), ZoneOffset.UTC),
                orc.getPublicTokenExpiraEm());
        assertNotNull(orc.getDisponibilizadoEm());
        assertTrue(result.urlPdf().startsWith("http://localhost:8080/public/orcamentos/10/pdf?token="));
        assertTrue(result.urlDecisao().startsWith("http://localhost:4200/public/orcamentos/10?token="));
        verify(orcamentoGateway).save(orc);
    }

    @Test
    void publicar_deveManterDisponibilizadoEmQuandoJaExistir() {
        OrcamentoEntity orc = new OrcamentoEntity();
        orc.setId(10L);
        orc.setStatus(StatusOrcamento.DISPONIVEL);
        var disponibilizadoEm = java.time.LocalDateTime.of(2026, Month.JUNE, 4, 10, 0);
        orc.setDisponibilizadoEm(disponibilizadoEm);

        when(orcamentoGateway.findById(10L)).thenReturn(Optional.of(orc));
        when(orcamentoGateway.save(any(OrcamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(service, "publicBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "tokenSecret", "local-secret");
        when(clock.instant()).thenReturn(Instant.parse("2026-06-04T12:00:00Z"));

        service.publicar(10L);

        assertEquals(disponibilizadoEm, orc.getDisponibilizadoEm());
        verify(orcamentoGateway).save(orc);
    }

    @Test
    void publicar_deveDarNotFoundQuandoOrcamentoNaoExistir() {
        when(orcamentoGateway.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.publicar(10L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void publicar_deveDarBadRequestQuandoOrcamentoNaoDisponivel() {
        OrcamentoEntity orc = new OrcamentoEntity();
        orc.setId(10L);
        orc.setStatus(StatusOrcamento.APROVADO);

        when(orcamentoGateway.findById(10L)).thenReturn(Optional.of(orc));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.publicar(10L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void validarToken_deveRetornarFalseQuandoHashNulo() {
        OrcamentoEntity orc = new OrcamentoEntity();
        orc.setPublicTokenHash(null);

        ReflectionTestUtils.setField(service, "tokenSecret", "local-secret");
        assertFalse(service.validarToken(orc, "tok"));
    }

    @Test
    void validarToken_deveValidarHashComSecret() {
        ReflectionTestUtils.setField(service, "tokenSecret", "local-secret");
        when(clock.instant()).thenReturn(Instant.parse("2026-06-04T12:00:00Z"));

        OrcamentoEntity orc = new OrcamentoEntity();
        String token = "abc";
        String hash = ReflectionTestUtils.invokeMethod(service, "sha256Hex", token + ":" + "local-secret");
        orc.setPublicTokenHash(hash);
        orc.setPublicTokenExpiraEm(LocalDateTime.of(2026, Month.JUNE, 5, 12, 0));

        assertTrue(service.validarToken(orc, token));
        assertFalse(service.validarToken(orc, "token-invalido"));
    }

    @Test
    void validarToken_deveRetornarFalseQuandoExpirado() {
        ReflectionTestUtils.setField(service, "tokenSecret", "local-secret");
        when(clock.instant()).thenReturn(Instant.parse("2026-06-05T12:00:00Z"));

        OrcamentoEntity orc = new OrcamentoEntity();
        String token = "abc";
        orc.setPublicTokenHash(ReflectionTestUtils.invokeMethod(
                service, "sha256Hex", token + ":local-secret"));
        orc.setPublicTokenExpiraEm(LocalDateTime.of(2026, Month.JUNE, 5, 12, 0));

        assertFalse(service.validarToken(orc, token));
    }
}
