package com.autoflow.repository.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.service.ordemservico.dto.OrdemServicoFiltro;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class OrdemServicoSpecificationsTest {

    @Mock private Root<OrdemServicoEntity> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;

    @Mock private Path<Object> statusPath;
    @Mock private Path<Object> clientePath;
    @Mock private Path<String> nomePath;
    @Mock private Path<String> cpfCnpjPath;
    @Mock private Path<String> numeroOsPath;

    @Mock private Expression<String> lowerNome;
    @Mock private Expression<String> lowerCpfCnpj;
    @Mock private Expression<String> lowerNumeroOs;

    @Mock private Predicate predicado;
    @Mock private Predicate predicadoOr;

    @BeforeEach
    void configurarMocks() {
        lenient().when(root.get("status")).thenReturn(statusPath);
        lenient().when(root.get("cliente")).thenReturn(clientePath);
        lenient().doReturn(nomePath).when(clientePath).get("nome");
        lenient().doReturn(cpfCnpjPath).when(clientePath).get("cpfCnpj");
        lenient().doReturn(numeroOsPath).when(root).get("numeroOs");

        lenient().when(cb.lower(nomePath)).thenReturn(lowerNome);
        lenient().when(cb.lower(cpfCnpjPath)).thenReturn(lowerCpfCnpj);
        lenient().when(cb.lower(numeroOsPath)).thenReturn(lowerNumeroOs);

        lenient().when(cb.equal(any(), any())).thenReturn(predicado);
        lenient().when(cb.like(any(Expression.class), any(String.class))).thenReturn(predicado);
        lenient().when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicadoOr);
    }

    private void aplicarSpec(OrdemServicoFiltro filtro) {
        OrdemServicoSpecifications.comFiltros(filtro).toPredicate(root, query, cb);
    }

    // --- filtro vazio ---

    @Test
    void comFiltros_filtroVazio_naoGeraPredicados() {
        aplicarSpec(new OrdemServicoFiltro(null, null, null));

        verify(cb, never()).equal(any(), any());
        verify(cb, never()).like(any(Expression.class), any(String.class));
    }

    // --- status ---

    @Test
    void comFiltros_comStatus_geraPredicadoDeStatus() {
        aplicarSpec(new OrdemServicoFiltro(null, null, StatusOrdemServico.RECEBIDA));

        verify(cb).equal(statusPath, StatusOrdemServico.RECEBIDA);
    }

    @Test
    void comFiltros_comStatusNulo_naoGeraPredicadoDeStatus() {
        aplicarSpec(new OrdemServicoFiltro(null, null, null));

        verify(cb, never()).equal(eq(statusPath), any());
    }

    // --- numeroOs ---

    @Test
    void comFiltros_comNumeroOs_geraPredicadoLike() {
        aplicarSpec(new OrdemServicoFiltro(null, "OS-123", null));

        verify(cb).lower(numeroOsPath);
        verify(cb).like(lowerNumeroOs, "%os-123%");
    }

    @Test
    void comFiltros_comNumeroOsEmMaiusculo_normalizaParaMinusculo() {
        aplicarSpec(new OrdemServicoFiltro(null, "OS-ABC", null));

        verify(cb).like(lowerNumeroOs, "%os-abc%");
    }

    @Test
    void comFiltros_comNumeroOsNulo_naoGeraPredicado() {
        aplicarSpec(new OrdemServicoFiltro(null, null, null));

        verify(cb, never()).lower(numeroOsPath);
    }

    @Test
    void comFiltros_comNumeroOsEmBranco_naoGeraPredicado() {
        aplicarSpec(new OrdemServicoFiltro(null, "  ", null));

        verify(cb, never()).lower(numeroOsPath);
    }

    // --- cliente (nome OR cpfCnpj) ---

    @Test
    void comFiltros_comCliente_geraPredicadoOrEntreNomeECpfCnpj() {
        aplicarSpec(new OrdemServicoFiltro("joao", null, null));

        verify(cb).lower(nomePath);
        verify(cb).lower(cpfCnpjPath);
        verify(cb).like(lowerNome, "%joao%");
        verify(cb).like(lowerCpfCnpj, "%joao%");
        verify(cb).or(predicado, predicado);
    }

    @Test
    void comFiltros_comClienteEmMaiusculo_normalizaParaMinusculo() {
        aplicarSpec(new OrdemServicoFiltro("MARIA", null, null));

        verify(cb).like(lowerNome, "%maria%");
        verify(cb).like(lowerCpfCnpj, "%maria%");
    }

    @Test
    void comFiltros_comClienteNulo_naoGeraPredicado() {
        aplicarSpec(new OrdemServicoFiltro(null, null, null));

        verify(cb, never()).lower(nomePath);
        verify(cb, never()).lower(cpfCnpjPath);
    }

    @Test
    void comFiltros_comClienteEmBranco_naoGeraPredicado() {
        aplicarSpec(new OrdemServicoFiltro("   ", null, null));

        verify(cb, never()).lower(nomePath);
        verify(cb, never()).lower(cpfCnpjPath);
    }

    // --- combinação completa ---

    @Test
    void comFiltros_comTodosOsFiltros_geraPredicadosParaTodosOsCampos() {
        aplicarSpec(new OrdemServicoFiltro("joao", "OS-999", StatusOrdemServico.EM_EXECUCAO));

        verify(cb).equal(statusPath, StatusOrdemServico.EM_EXECUCAO);
        verify(cb).like(lowerNumeroOs, "%os-999%");
        verify(cb).like(lowerNome, "%joao%");
        verify(cb).like(lowerCpfCnpj, "%joao%");
        verify(cb).or(predicado, predicado);
    }
}
