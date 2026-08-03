package com.autoflow.repository.pecainsumo;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.infrastructure.persistence.repository.PecaInsumoSpecifications;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PecaInsumoSpecificationsTest {

    @Mock private Root<PecaInsumoEntity> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;

    @Mock private Path<String> nomePath;
    @Mock private Path<Object> tipoPath;
    @Mock private Expression<String> lowerNome;
    @Mock private Predicate predicado;

    @BeforeEach
    void configurarMocks() {
        lenient().doReturn(nomePath).when(root).get("nome");
        lenient().doReturn(tipoPath).when(root).get("tipo");
        lenient().when(cb.lower(nomePath)).thenReturn(lowerNome);
        lenient().when(cb.like(any(Expression.class), any(String.class))).thenReturn(predicado);
        lenient().when(cb.equal(any(), any())).thenReturn(predicado);
    }

    private void aplicarSpec(String nome, CategoriaPecaInsumo tipo) {
        PecaInsumoSpecifications.comFiltros(nome, tipo).toPredicate(root, query, cb);
    }

    @Test
    void comFiltroVazioNaoGeraPredicados() {
        aplicarSpec(null, null);

        verify(cb, never()).like(any(Expression.class), any(String.class));
        verify(cb, never()).equal(any(), any());
    }

    @Test
    void comNomeGeraLikeEmMinusculo() {
        aplicarSpec("filtro", null);

        verify(cb).lower(nomePath);
        verify(cb).like(lowerNome, "%filtro%");
    }

    @Test
    void comNomeEmMaiusculoNormalizaParaMinusculo() {
        aplicarSpec("FILTRO", null);

        verify(cb).like(lowerNome, "%filtro%");
    }

    @Test
    void comNomeNuloNaoGeraPredicadoDeNome() {
        aplicarSpec(null, null);

        verify(cb, never()).lower(nomePath);
    }

    @Test
    void comNomeEmBrancoNaoGeraPredicadoDeNome() {
        aplicarSpec("   ", null);

        verify(cb, never()).lower(nomePath);
    }

    @Test
    void comTipoGeraPredicadoDeIgualdade() {
        aplicarSpec(null, CategoriaPecaInsumo.PECA);

        verify(cb).equal(tipoPath, CategoriaPecaInsumo.PECA);
    }

    @Test
    void comTipoInsumoGeraPredicadoDeIgualdade() {
        aplicarSpec(null, CategoriaPecaInsumo.INSUMO);

        verify(cb).equal(tipoPath, CategoriaPecaInsumo.INSUMO);
    }

    @Test
    void comTipoNuloNaoGeraPredicadoDeTipo() {
        aplicarSpec(null, null);

        verify(cb, never()).equal(eq(tipoPath), any());
    }

    @Test
    void comTodosOsFiltrosGeraPredicadosParaTodosOsCampos() {
        aplicarSpec("filtro", CategoriaPecaInsumo.PECA);

        verify(cb).like(lowerNome, "%filtro%");
        verify(cb).equal(tipoPath, CategoriaPecaInsumo.PECA);
    }
}
