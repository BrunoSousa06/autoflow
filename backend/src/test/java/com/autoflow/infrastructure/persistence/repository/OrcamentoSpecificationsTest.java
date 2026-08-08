package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.application.dto.orcamento.OrcamentoFiltro;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
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
class OrcamentoSpecificationsTest {

    @Mock private Root<OrcamentoEntity> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;

    @Mock private Path<Object> statusPath;
    @Mock private Path<Object> tipoPath;
    @Mock private Path<String> numeroOsPath;
    @Mock private Path<Object> veiculoPath;
    @Mock private Path<String> placaPath;
    @Mock private Path<Object> clientePath;
    @Mock private Path<String> emailPath;
    @Mock private Path<Object> cpfCnpjPath;

    @Mock private Expression<String> lowerNumeroOs;
    @Mock private Expression<String> lowerPlaca;
    @Mock private Expression<String> lowerEmail;

    @Mock private Predicate predicado;

    @BeforeEach
    void configurarMocks() {
        lenient().when(root.get("status")).thenReturn(statusPath);
        lenient().when(root.get("tipo")).thenReturn(tipoPath);
        lenient().when(root.get("veiculo")).thenReturn(veiculoPath);
        lenient().when(root.get("cliente")).thenReturn(clientePath);
        lenient().when(clientePath.get("cpfCnpj")).thenReturn(cpfCnpjPath);

        // doReturn contorna invariância de generics para Path<String>
        lenient().doReturn(numeroOsPath).when(root).get("numeroOs");
        lenient().doReturn(placaPath).when(veiculoPath).get("placa");
        lenient().doReturn(emailPath).when(clientePath).get("email");

        lenient().when(cb.lower(numeroOsPath)).thenReturn(lowerNumeroOs);
        lenient().when(cb.lower(placaPath)).thenReturn(lowerPlaca);
        lenient().when(cb.lower(emailPath)).thenReturn(lowerEmail);

        lenient().when(cb.equal(any(), any())).thenReturn(predicado);
    }

    private void aplicarSpec(OrcamentoFiltro filtro) {
        OrcamentoSpecifications.comFiltros(filtro).toPredicate(root, query, cb);
    }

    // --- filtro completamente vazio ---

    @Test
    void comFiltros_filtroVazio_naoGeraPredicados() {
        var filtro = new OrcamentoFiltro(null, null, null, null, null, null);

        aplicarSpec(filtro);

        verify(cb, never()).equal(any(), any());
    }

    // --- status ---

    @Test
    void comFiltros_comStatus_geraPredicadoDeStatus() {
        var filtro = new OrcamentoFiltro(StatusOrcamento.DISPONIVEL, null, null, null, null, null);

        aplicarSpec(filtro);

        verify(cb).equal(statusPath, StatusOrcamento.DISPONIVEL);
    }

    @Test
    void comFiltros_comStatusNulo_naoGeraPredicadoDeStatus() {
        var filtro = new OrcamentoFiltro(null, null, null, null, null, null);

        aplicarSpec(filtro);

        verify(cb, never()).equal(eq(statusPath), any());
    }

    // --- numeroOs ---

    @Test
    void comFiltros_comNumeroOs_geraPredicadoComValorEmMinusculo() {
        var filtro = new OrcamentoFiltro(null, "OS-001", null, null, null, null);

        aplicarSpec(filtro);

        verify(cb).lower(numeroOsPath);
        verify(cb).equal(lowerNumeroOs, "os-001");
    }

    @Test
    void comFiltros_comNumeroOsEmMaiusculo_normalizaParaMinusculo() {
        var filtro = new OrcamentoFiltro(null, "OS-ABC", null, null, null, null);

        aplicarSpec(filtro);

        verify(cb).equal(lowerNumeroOs, "os-abc");
    }

    @Test
    void comFiltros_comNumeroOsNulo_naoGeraPredicadoDeNumeroOs() {
        var filtro = new OrcamentoFiltro(null, null, null, null, null, null);

        aplicarSpec(filtro);

        verify(cb, never()).lower(numeroOsPath);
    }

    @Test
    void comFiltros_comNumeroOsEmBranco_naoGeraPredicadoDeNumeroOs() {
        var filtro = new OrcamentoFiltro(null, "   ", null, null, null, null);

        aplicarSpec(filtro);

        verify(cb, never()).lower(numeroOsPath);
    }

    // --- placa ---

    @Test
    void comFiltros_comPlaca_geraPredicadoComPlacaEmMinusculo() {
        var filtro = new OrcamentoFiltro(null, null, "ABC-1234", null, null, null);

        aplicarSpec(filtro);

        verify(cb).lower(placaPath);
        verify(cb).equal(lowerPlaca, "abc-1234");
    }

    @Test
    void comFiltros_comPlacaEmMaiusculo_normalizaParaMinusculo() {
        var filtro = new OrcamentoFiltro(null, null, "XYZ-9999", null, null, null);

        aplicarSpec(filtro);

        verify(cb).equal(lowerPlaca, "xyz-9999");
    }

    @Test
    void comFiltros_comPlacaNula_naoGeraPredicadoDePlaca() {
        var filtro = new OrcamentoFiltro(null, null, null, null, null, null);

        aplicarSpec(filtro);

        verify(cb, never()).lower(placaPath);
    }

    @Test
    void comFiltros_comPlacaVazia_naoGeraPredicadoDePlaca() {
        var filtro = new OrcamentoFiltro(null, null, "", null, null, null);

        aplicarSpec(filtro);

        verify(cb, never()).lower(placaPath);
    }

    // --- clienteEmail ---

    @Test
    void comFiltros_comClienteEmail_geraPredicadoComEmailEmMinusculo() {
        var filtro = new OrcamentoFiltro(null, null, null, "CLIENTE@EMAIL.COM", null, null);

        aplicarSpec(filtro);

        verify(cb).lower(emailPath);
        verify(cb).equal(lowerEmail, "cliente@email.com");
    }

    @Test
    void comFiltros_comClienteEmailNulo_naoGeraPredicadoDeEmail() {
        var filtro = new OrcamentoFiltro(null, null, null, null, null, null);

        aplicarSpec(filtro);

        verify(cb, never()).lower(emailPath);
    }

    @Test
    void comFiltros_comClienteEmailEmBranco_naoGeraPredicadoDeEmail() {
        var filtro = new OrcamentoFiltro(null, null, null, "  ", null, null);

        aplicarSpec(filtro);

        verify(cb, never()).lower(emailPath);
    }

    // --- clienteDocumento ---

    @Test
    void comFiltros_comClienteDocumento_geraPredicadoDeDocumento() {
        var filtro = new OrcamentoFiltro(null, null, null, null, "12345678901", null);

        aplicarSpec(filtro);

        verify(cb).equal(cpfCnpjPath, "12345678901");
    }

    @Test
    void comFiltros_comClienteDocumentoNulo_naoGeraPredicadoDeDocumento() {
        var filtro = new OrcamentoFiltro(null, null, null, null, null, null);

        aplicarSpec(filtro);

        verify(cb, never()).equal(eq(cpfCnpjPath), any());
    }

    @Test
    void comFiltros_comClienteDocumentoEmBranco_naoGeraPredicadoDeDocumento() {
        var filtro = new OrcamentoFiltro(null, null, null, null, "  ", null);

        aplicarSpec(filtro);

        verify(cb, never()).equal(eq(cpfCnpjPath), any());
    }

    // --- tipo ---

    @Test
    void comFiltros_comTipoPrincipal_geraPredicadoDeTipo() {
        var filtro = new OrcamentoFiltro(null, null, null, null, null, TipoOrcamento.PRINCIPAL);

        aplicarSpec(filtro);

        verify(cb).equal(tipoPath, TipoOrcamento.PRINCIPAL);
    }

    @Test
    void comFiltros_comTipoAdicional_geraPredicadoDeTipo() {
        var filtro = new OrcamentoFiltro(null, null, null, null, null, TipoOrcamento.COMPLEMENTAR);

        aplicarSpec(filtro);

        verify(cb).equal(tipoPath, TipoOrcamento.COMPLEMENTAR);
    }

    @Test
    void comFiltros_comTipoNulo_naoGeraPredicadoDeTipo() {
        var filtro = new OrcamentoFiltro(null, null, null, null, null, null);

        aplicarSpec(filtro);

        verify(cb, never()).equal(eq(tipoPath), any());
    }

    // --- combinação completa ---

    @Test
    void comFiltros_comTodosOsFiltros_geraPredicadosParaTodosOsCampos() {
        var filtro = new OrcamentoFiltro(
                StatusOrcamento.APROVADO,
                "OS-999",
                "DEF-5678",
                "test@test.com",
                "98765432100",
                TipoOrcamento.COMPLEMENTAR
        );

        aplicarSpec(filtro);

        verify(cb).equal(statusPath, StatusOrcamento.APROVADO);
        verify(cb).equal(lowerNumeroOs, "os-999");
        verify(cb).equal(lowerPlaca, "def-5678");
        verify(cb).equal(lowerEmail, "test@test.com");
        verify(cb).equal(cpfCnpjPath, "98765432100");
        verify(cb).equal(tipoPath, TipoOrcamento.COMPLEMENTAR);
    }
}
