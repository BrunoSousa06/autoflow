package com.autoflow.repository.veiculo;

import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.service.veiculo.dto.VeiculoFiltro;
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
class VeiculoSpecificationsTest {

    @Mock private Root<VeiculoEntity> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;

    @Mock private Path<String> placaPath;
    @Mock private Path<String> marcaPath;
    @Mock private Path<String> modeloPath;
    @Mock private Path<Object> anoPah;
    @Mock private Path<Object> clientePath;
    @Mock private Path<Object> clienteIdPath;

    @Mock private Expression<String> lowerPlaca;
    @Mock private Expression<String> lowerMarca;
    @Mock private Expression<String> lowerModelo;

    @Mock private Predicate predicado;

    @BeforeEach
    void configurarMocks() {
        lenient().doReturn(placaPath).when(root).get("placa");
        lenient().doReturn(marcaPath).when(root).get("marca");
        lenient().doReturn(modeloPath).when(root).get("modelo");
        lenient().when(root.get("ano")).thenReturn(anoPah);
        lenient().when(root.get("cliente")).thenReturn(clientePath);
        lenient().when(clientePath.get("id")).thenReturn(clienteIdPath);

        lenient().when(cb.lower(placaPath)).thenReturn(lowerPlaca);
        lenient().when(cb.lower(marcaPath)).thenReturn(lowerMarca);
        lenient().when(cb.lower(modeloPath)).thenReturn(lowerModelo);

        lenient().when(cb.equal(any(), any())).thenReturn(predicado);
        lenient().when(cb.like(any(Expression.class), any(String.class))).thenReturn(predicado);
    }

    private void aplicarSpec(VeiculoFiltro filtro) {
        VeiculoSpecifications.comFiltros(filtro).toPredicate(root, query, cb);
    }

    // ── filtro vazio ──────────────────────────────────────────────────────────

    @Test
    void comFiltros_filtroVazio_naoGeraPredicados() {
        aplicarSpec(new VeiculoFiltro(null, null, null, null, null));

        verify(cb, never()).equal(any(), any());
        verify(cb, never()).like(any(Expression.class), any(String.class));
    }

    // ── placa ─────────────────────────────────────────────────────────────────

    @Test
    void comFiltros_comPlaca_normalizaEComparaEmMinusculo() {
        aplicarSpec(new VeiculoFiltro("ABC1234", null, null, null, null));

        verify(cb).lower(placaPath);
        verify(cb).equal(lowerPlaca, "abc1234");
    }

    @Test
    void comFiltros_comPlacaComHifen_removeHifenENormaliza() {
        aplicarSpec(new VeiculoFiltro("ABC-1234", null, null, null, null));

        verify(cb).equal(lowerPlaca, "abc1234");
    }

    @Test
    void comFiltros_comPlacaNula_naoGeraPredicadoDePlaca() {
        aplicarSpec(new VeiculoFiltro(null, null, null, null, null));

        verify(cb, never()).lower(placaPath);
    }

    @Test
    void comFiltros_comPlacaEmBranco_naoGeraPredicadoDePlaca() {
        aplicarSpec(new VeiculoFiltro("  ", null, null, null, null));

        verify(cb, never()).lower(placaPath);
    }

    // ── marca ─────────────────────────────────────────────────────────────────

    @Test
    void comFiltros_comMarca_geraLikeEmMinusculo() {
        aplicarSpec(new VeiculoFiltro(null, "Honda", null, null, null));

        verify(cb).lower(marcaPath);
        verify(cb).like(lowerMarca, "%honda%");
    }

    @Test
    void comFiltros_comMarcaEmMaiusculo_normalizaParaMinusculo() {
        aplicarSpec(new VeiculoFiltro(null, "TOYOTA", null, null, null));

        verify(cb).like(lowerMarca, "%toyota%");
    }

    @Test
    void comFiltros_comMarcaNula_naoGeraPredicadoDeMarca() {
        aplicarSpec(new VeiculoFiltro(null, null, null, null, null));

        verify(cb, never()).lower(marcaPath);
    }

    @Test
    void comFiltros_comMarcaEmBranco_naoGeraPredicadoDeMarca() {
        aplicarSpec(new VeiculoFiltro(null, "  ", null, null, null));

        verify(cb, never()).lower(marcaPath);
    }

    // ── modelo ────────────────────────────────────────────────────────────────

    @Test
    void comFiltros_comModelo_geraLikeEmMinusculo() {
        aplicarSpec(new VeiculoFiltro(null, null, "Civic", null, null));

        verify(cb).lower(modeloPath);
        verify(cb).like(lowerModelo, "%civic%");
    }

    @Test
    void comFiltros_comModeloNulo_naoGeraPredicadoDeModelo() {
        aplicarSpec(new VeiculoFiltro(null, null, null, null, null));

        verify(cb, never()).lower(modeloPath);
    }

    // ── ano ───────────────────────────────────────────────────────────────────

    @Test
    void comFiltros_comAno_geraPredicadoDeIgualdade() {
        aplicarSpec(new VeiculoFiltro(null, null, null, 2022, null));

        verify(cb).equal(anoPah, 2022);
    }

    @Test
    void comFiltros_comAnoNulo_naoGeraPredicadoDeAno() {
        aplicarSpec(new VeiculoFiltro(null, null, null, null, null));

        verify(cb, never()).equal(eq(anoPah), any());
    }

    // ── clienteId ─────────────────────────────────────────────────────────────

    @Test
    void comFiltros_comClienteId_geraPredicadoDeIgualdade() {
        aplicarSpec(new VeiculoFiltro(null, null, null, null, 42L));

        verify(cb).equal(clienteIdPath, 42L);
    }

    @Test
    void comFiltros_comClienteIdNulo_naoGeraPredicadoDeClienteId() {
        aplicarSpec(new VeiculoFiltro(null, null, null, null, null));

        verify(clientePath, never()).get("id");
    }

    // ── combinação completa ───────────────────────────────────────────────────

    @Test
    void comFiltros_comTodosOsFiltros_geraPredicadosParaTodosOsCampos() {
        aplicarSpec(new VeiculoFiltro("ABC1234", "Honda", "Civic", 2022, 1L));

        verify(cb).equal(lowerPlaca, "abc1234");
        verify(cb).like(lowerMarca, "%honda%");
        verify(cb).like(lowerModelo, "%civic%");
        verify(cb).equal(anoPah, 2022);
        verify(cb).equal(clienteIdPath, 1L);
    }
}
