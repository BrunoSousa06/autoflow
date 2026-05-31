package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.service.orcamento.dto.PublicacaoOrcamentoResult;
import com.autoflow.service.orcamento.impl.OrcamentoPublicacaoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoPublicacaoServiceImplTest {

    @Mock
    OrcamentoRepository orcamentoRepository;

    @InjectMocks
    OrcamentoPublicacaoServiceImpl service;

    @Test
    void publicar_deveGerarTokenHashEDefinirDisponibilizadoEm() {
        OrcamentoEntity orc = new OrcamentoEntity();
        orc.setId(10L);
        orc.setStatus(StatusOrcamento.DISPONIVEL);
        orc.setDisponibilizadoEm(null);

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        when(orcamentoRepository.save(any(OrcamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(service, "publicBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "tokenSecret", "local-secret");

        PublicacaoOrcamentoResult result = service.publicar(10L);

        assertEquals(10L, result.orcamentoId());
        assertNotNull(orc.getPublicTokenHash());
        assertNotNull(orc.getDisponibilizadoEm());
        assertTrue(result.url().startsWith("http://localhost:8080/public/orcamentos/10?token="));
        verify(orcamentoRepository).save(orc);
    }

    @Test
    void publicar_deveDarBadRequestQuandoOrcamentoNaoDisponivel() {
        OrcamentoEntity orc = new OrcamentoEntity();
        orc.setId(10L);
        orc.setStatus(StatusOrcamento.APROVADO);

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));

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

        OrcamentoEntity orc = new OrcamentoEntity();
        String token = "abc";
        String hash = ReflectionTestUtils.invokeMethod(service, "sha256Hex", token + ":" + "local-secret");
        orc.setPublicTokenHash(hash);

        assertTrue(service.validarToken(orc, token));
    }
}
