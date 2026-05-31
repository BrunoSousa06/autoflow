package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.service.orcamento.impl.PublicOrcamentoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicOrcamentoServiceImplTest {

    @Mock
    OrcamentoRepository orcamentoRepository;

    @Mock
    OrdemServicoRepository ordemServicoRepository;

    @Mock
    OrcamentoPublicacaoService publicacaoService;

    @InjectMocks
    PublicOrcamentoServiceImpl service;

    @Test
    void consultar_deveRetornarOrcamentoQuandoTokenOk() {
        OrcamentoEntity orc = orcamentoDisponivel();
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        when(publicacaoService.validarToken(orc, "tok")).thenReturn(true);

        OrcamentoEntity result = service.consultar(10L, "tok");

        assertSame(orc, result);
    }

    @Test
    void consultar_deveRetornarUnauthorizedQuandoTokenInvalido() {
        OrcamentoEntity orc = orcamentoDisponivel();
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        when(publicacaoService.validarToken(orc, "tok")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.consultar(10L, "tok"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void aceitar_deveAprovarOrcamentoEColocarOsEmExecucao() {
        OrcamentoEntity orc = orcamentoDisponivel();
        OrdemServicoEntity os = osAguardandoAprovacao(orc.getOrdemServicoId());

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        when(publicacaoService.validarToken(orc, "tok")).thenReturn(true);
        when(orcamentoRepository.save(any(OrcamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(os));
        when(ordemServicoRepository.save(any(OrdemServicoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrcamentoEntity result = service.aceitar(10L, "tok", " Maria ");

        assertEquals(StatusOrcamento.APROVADO, result.getStatus());
        assertEquals("Maria", result.getAssinaturaNome());
        assertNotNull(result.getAprovadoEm());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, os.getStatus());
        verify(ordemServicoRepository).save(os);
    }

    @Test
    void aceitar_deveRetornarMesmoOrcamentoSeJaFinal() {
        OrcamentoEntity orc = orcamentoDisponivel();
        orc.setStatus(StatusOrcamento.REPROVADO);

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        when(publicacaoService.validarToken(orc, "tok")).thenReturn(true);

        OrcamentoEntity result = service.aceitar(10L, "tok", "Maria");

        assertSame(orc, result);
        verify(orcamentoRepository, never()).save(any());
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void aceitar_deveDarBadRequestQuandoNomeVazio() {
        OrcamentoEntity orc = orcamentoDisponivel();
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        when(publicacaoService.validarToken(orc, "tok")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.aceitar(10L, "tok", " "));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void recusar_deveReprovarOrcamentoEFinalizarOs() {
        OrcamentoEntity orc = orcamentoDisponivel();
        OrdemServicoEntity os = osAguardandoAprovacao(orc.getOrdemServicoId());

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        when(publicacaoService.validarToken(orc, "tok")).thenReturn(true);
        when(orcamentoRepository.save(any(OrcamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(os));
        when(ordemServicoRepository.save(any(OrdemServicoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrcamentoEntity result = service.recusar(10L, "tok", "Nao quero");

        assertEquals(StatusOrcamento.REPROVADO, result.getStatus());
        assertEquals("Nao quero", result.getRecusaMotivo());
        assertNotNull(result.getReprovadoEm());
        assertEquals(StatusOrdemServico.FINALIZADA, os.getStatus());
        verify(ordemServicoRepository).save(os);
    }

    @Test
    void recusar_deveDarBadRequestQuandoJaAprovado() {
        OrcamentoEntity orc = orcamentoDisponivel();
        orc.setStatus(StatusOrcamento.APROVADO);

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        when(publicacaoService.validarToken(orc, "tok")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.recusar(10L, "tok", "x"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    private OrcamentoEntity orcamentoDisponivel() {
        OrcamentoEntity orc = new OrcamentoEntity();
        orc.setId(10L);
        orc.setOrdemServicoId(1L);
        orc.setStatus(StatusOrcamento.DISPONIVEL);
        orc.setCriadoEm(LocalDateTime.of(2026, 5, 31, 10, 0));
        return orc;
    }

    private OrdemServicoEntity osAguardandoAprovacao(Long osId) {
        OrdemServicoEntity os = new OrdemServicoEntity();
        os.setId(osId);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        return os;
    }
}
