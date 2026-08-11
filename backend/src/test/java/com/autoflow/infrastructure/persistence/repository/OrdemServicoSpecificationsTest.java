package com.autoflow.infrastructure.persistence.repository;

import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
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
    @Mock private Path<LocalDateTime> dataAberturaPath;
    @Mock private Path<Long> idPath;
    @Mock private Path<Object> diagnosticoPath;
    @Mock private Path<Object> mecanicoPath;
    @Mock private Path<String> mecanicoEmailPath;

    @Mock private Expression<String> lowerNome;
    @Mock private Expression<String> lowerCpfCnpj;
    @Mock private Expression<String> lowerNumeroOs;

    @Mock private Predicate predicado;
    @Mock private Predicate predicadoOr;
    @Mock private Predicate statusOperacionalPredicado;
    @Mock private CriteriaBuilder.SimpleCase<StatusOrdemServico, Integer> prioridadeStatus;

    @BeforeEach
    void configurarMocks() {
        lenient().when(root.get("status")).thenReturn(statusPath);
        lenient().when(root.get("cliente")).thenReturn(clientePath);
        lenient().doReturn(nomePath).when(clientePath).get("nome");
        lenient().doReturn(cpfCnpjPath).when(clientePath).get("cpfCnpj");
        lenient().doReturn(numeroOsPath).when(root).get("numeroOs");
        lenient().doReturn(dataAberturaPath).when(root).get("dataAbertura");
        lenient().doReturn(idPath).when(root).get("id");
        lenient().when(root.get("diagnostico")).thenReturn(diagnosticoPath);
        lenient().when(diagnosticoPath.get("mecanico")).thenReturn(mecanicoPath);
        lenient().doReturn(mecanicoEmailPath).when(mecanicoPath).get("email");

        lenient().when(cb.lower(nomePath)).thenReturn(lowerNome);
        lenient().when(cb.lower(cpfCnpjPath)).thenReturn(lowerCpfCnpj);
        lenient().when(cb.lower(numeroOsPath)).thenReturn(lowerNumeroOs);

        lenient().when(cb.equal(any(), any())).thenReturn(predicado);
        lenient().when(cb.like(any(Expression.class), any(String.class))).thenReturn(predicado);
        lenient().when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicadoOr);
        lenient().when(statusPath.in(anyCollection())).thenReturn(statusOperacionalPredicado);
        lenient().when(cb.selectCase(any(Expression.class))).thenReturn(prioridadeStatus);
        lenient().when(prioridadeStatus.when(any(StatusOrdemServico.class), anyInt())).thenReturn(prioridadeStatus);
        lenient().when(prioridadeStatus.otherwise(anyInt())).thenReturn(prioridadeStatus);
    }

    private void aplicarSpec(OrdemServicoFiltroInput filtro) {
        aplicarSpec(filtro, null);
    }

    private void aplicarSpec(OrdemServicoFiltroInput filtro, String emailMecanico) {
        OrdemServicoSpecifications.comFiltros(filtro, emailMecanico).toPredicate(root, query, cb);
    }

    // --- filtro vazio ---

    @Test
    void comFiltros_filtroVazio_naoGeraPredicadosDeFiltrosExplicitos() {
        aplicarSpec(new OrdemServicoFiltroInput(null, null, null));

        verify(cb, never()).equal(any(), any());
        verify(cb, never()).like(any(Expression.class), any(String.class));
    }

    @Test
    void comFiltros_filtroVazio_restringeAosStatusOperacionais() {
        aplicarSpec(new OrdemServicoFiltroInput(null, null, null));

        verify(statusPath).in(OrdemServicoSpecifications.STATUS_OPERACIONAIS);
    }

    // --- status ---

    @Test
    void comFiltros_comStatus_geraPredicadoDeStatus() {
        aplicarSpec(new OrdemServicoFiltroInput(null, null, StatusOrdemServico.RECEBIDA));

        verify(cb).equal(statusPath, StatusOrdemServico.RECEBIDA);
    }

    @Test
    void comFiltros_comStatusNulo_naoGeraPredicadoDeStatus() {
        aplicarSpec(new OrdemServicoFiltroInput(null, null, null));

        verify(cb, never()).equal(eq(statusPath), any());
    }

    @Test
    void comFiltros_comStatusFinalizada_mantemExclusaoDosStatusOperacionais() {
        aplicarSpec(new OrdemServicoFiltroInput(null, null, StatusOrdemServico.FINALIZADA));

        verify(statusPath).in(OrdemServicoSpecifications.STATUS_OPERACIONAIS);
        verify(cb).equal(statusPath, StatusOrdemServico.FINALIZADA);
    }

    @Test
    void comFiltros_consultaDeDados_ordenaPorPrioridadeDataAscendenteEId() {
        doReturn(OrdemServicoEntity.class).when(query).getResultType();

        aplicarSpec(new OrdemServicoFiltroInput(null, null, null));

        verify(prioridadeStatus).when(StatusOrdemServico.EM_EXECUCAO, 1);
        verify(prioridadeStatus).when(StatusOrdemServico.AGUARDANDO_APROVACAO, 2);
        verify(prioridadeStatus).when(StatusOrdemServico.EM_DIAGNOSTICO, 3);
        verify(prioridadeStatus).when(StatusOrdemServico.RECEBIDA, 4);
        verify(prioridadeStatus).otherwise(5);
        verify(cb).asc(prioridadeStatus);
        verify(cb).asc(dataAberturaPath);
        verify(cb).asc(idPath);
    }

    @Test
    void comFiltros_consultaDeContagem_naoAplicaOrdenacao() {
        doReturn(Long.class).when(query).getResultType();

        aplicarSpec(new OrdemServicoFiltroInput(null, null, null));

        verify(query, never()).orderBy(any(Order[].class));
    }

    // --- numeroOs ---

    @Test
    void comFiltros_comNumeroOs_geraPredicadoLike() {
        aplicarSpec(new OrdemServicoFiltroInput(null, "OS-123", null));

        verify(cb).lower(numeroOsPath);
        verify(cb).like(lowerNumeroOs, "%os-123%");
    }

    @Test
    void comFiltros_comNumeroOsEmMaiusculo_normalizaParaMinusculo() {
        aplicarSpec(new OrdemServicoFiltroInput(null, "OS-ABC", null));

        verify(cb).like(lowerNumeroOs, "%os-abc%");
    }

    @Test
    void comFiltros_comNumeroOsNulo_naoGeraPredicado() {
        aplicarSpec(new OrdemServicoFiltroInput(null, null, null));

        verify(cb, never()).lower(numeroOsPath);
    }

    @Test
    void comFiltros_comNumeroOsEmBranco_naoGeraPredicado() {
        aplicarSpec(new OrdemServicoFiltroInput(null, "  ", null));

        verify(cb, never()).lower(numeroOsPath);
    }

    // --- cliente (nome OR cpfCnpj) ---

    @Test
    void comFiltros_comCliente_geraPredicadoOrEntreNomeECpfCnpj() {
        aplicarSpec(new OrdemServicoFiltroInput("joao", null, null));

        verify(cb).lower(nomePath);
        verify(cb).lower(cpfCnpjPath);
        verify(cb).like(lowerNome, "%joao%");
        verify(cb).like(lowerCpfCnpj, "%joao%");
        verify(cb).or(predicado, predicado);
    }

    @Test
    void comFiltros_comClienteEmMaiusculo_normalizaParaMinusculo() {
        aplicarSpec(new OrdemServicoFiltroInput("MARIA", null, null));

        verify(cb).like(lowerNome, "%maria%");
        verify(cb).like(lowerCpfCnpj, "%maria%");
    }

    @Test
    void comFiltros_comClienteNulo_naoGeraPredicado() {
        aplicarSpec(new OrdemServicoFiltroInput(null, null, null));

        verify(cb, never()).lower(nomePath);
        verify(cb, never()).lower(cpfCnpjPath);
    }

    @Test
    void comFiltros_comClienteEmBranco_naoGeraPredicado() {
        aplicarSpec(new OrdemServicoFiltroInput("   ", null, null));

        verify(cb, never()).lower(nomePath);
        verify(cb, never()).lower(cpfCnpjPath);
    }

    // --- mecânico ---

    @Test
    void comFiltros_comEmailMecanico_geraPredicadoDeIgualdade() {
        aplicarSpec(new OrdemServicoFiltroInput(null, null, null), "mecanico@autoflow.com");

        verify(cb).equal(mecanicoEmailPath, "mecanico@autoflow.com");
    }

    @Test
    void comFiltros_comEmailMecanicoNulo_naoGeraPredicadoDeMecanico() {
        aplicarSpec(new OrdemServicoFiltroInput(null, null, null), null);

        verify(root, never()).get("diagnostico");
    }

    // --- combinação completa ---

    @Test
    void comFiltros_comTodosOsFiltros_geraPredicadosParaTodosOsCampos() {
        aplicarSpec(new OrdemServicoFiltroInput("joao", "OS-999", StatusOrdemServico.EM_EXECUCAO));

        verify(cb).equal(statusPath, StatusOrdemServico.EM_EXECUCAO);
        verify(cb).like(lowerNumeroOs, "%os-999%");
        verify(cb).like(lowerNome, "%joao%");
        verify(cb).like(lowerCpfCnpj, "%joao%");
        verify(cb).or(predicado, predicado);
    }
}
