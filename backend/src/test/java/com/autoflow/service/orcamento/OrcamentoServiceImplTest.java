package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.ClienteOrcamentoSnapshot;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.orcamento.OrcamentoSpecifications;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.service.orcamento.dto.OrcamentoFiltro;
import com.autoflow.service.orcamento.impl.OrcamentoServiceImpl;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import com.autoflow.service.usuario.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoServiceImplTest {

    @Mock
    OrcamentoRepository orcamentoRepository;

    @Mock
    OrdemServicoRepository ordemServicoRepository;

    @Mock
    OrcamentoPublicacaoService publicacaoService;

    @Mock
    ReparoAdicionalService reparoAdicionalService;

    @Mock
    UsuarioService usuarioService;

    @InjectMocks
    OrcamentoServiceImpl service;

    @Test
    void consultarAutenticado_deveRetornarOrcamentoQuandoAdmin() {
        OrcamentoEntity orc = orcamentoDisponivel();
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("admin@autoflow.com", "Admin", RoleEnum.ADMIN);

        OrcamentoEntity result = service.consultarAutenticado(10L, "admin@autoflow.com");

        assertSame(orc, result);
    }

    @Test
    void consultarAutenticado_deveRetornarOrcamentoQuandoClienteForDono() {
        OrcamentoEntity orc = orcamentoDisponivel();
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Cliente", RoleEnum.CLIENTE);

        OrcamentoEntity result = service.consultarAutenticado(10L, "cliente@exemplo.com");

        assertSame(orc, result);
    }

    @Test
    void consultarAutenticado_deveRetornarForbiddenQuandoClienteNaoForDono() {
        OrcamentoEntity orc = orcamentoDisponivel();
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("outro@exemplo.com", "Outro Cliente", RoleEnum.CLIENTE);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.consultarAutenticado(10L, "outro@exemplo.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void validarToken_deveRetornarUnauthorizedQuandoTokenInvalido() {
        OrcamentoEntity orc = orcamentoDisponivel();
        when(publicacaoService.validarToken(orc, "tok")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.validarToken(orc, "tok"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void consultar_PorToken_Autenticado_deveRetornarNotFoundQuandoOrcamentoNaoExistir() {
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.consultarAutenticado(10L, "admin@autoflow.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void aprovar_deveAprovarOrcamentoEIniciarExecucaoDaOs() {
        OrcamentoEntity orc = orcamentoDisponivel();
        OrdemServicoEntity os = osAguardandoAprovacao(orc.getOrdemServicoId());

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(os));
        when(ordemServicoRepository.save(any(OrdemServicoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orcamentoRepository.save(any(OrcamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrcamentoEntity result = service.aprovar(10L, "cliente@exemplo.com");

        assertEquals(StatusOrcamento.APROVADO, result.getStatus());
        assertEquals("Maria", result.getAssinaturaNome());
        assertNotNull(result.getAprovadoEm());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, os.getStatus());
        verify(ordemServicoRepository).save(os);
    }

    @Test
    void aprovar_deveAprovarReparoAdicionalQuandoOrcamentoTemReparoVinculadoSemIniciarOs() {
        OrcamentoEntity orc = orcamentoDisponivel();

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        when(orcamentoRepository.save(any(OrcamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reparoAdicionalService.existePorOrcamentoId(10L)).thenReturn(true);

        OrcamentoEntity result = service.aprovar(10L, "cliente@exemplo.com");

        assertEquals(StatusOrcamento.APROVADO, result.getStatus());
        assertEquals("Maria", result.getAssinaturaNome());
        assertNotNull(result.getAprovadoEm());
        verify(reparoAdicionalService).aprovarSeExistirPorOrcamentoId(10L);
        verify(ordemServicoRepository, never()).findById(any());
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void aprovar_deveRetornarMesmoOrcamentoSeJaFinal() {
        OrcamentoEntity orc = orcamentoDisponivel();
        orc.setStatus(StatusOrcamento.REPROVADO);

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        OrcamentoEntity result = service.aprovar(10L, "cliente@exemplo.com");

        assertSame(orc, result);
        verify(orcamentoRepository, never()).save(any());
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void aprovar_deveRetornarMesmoOrcamentoSeJaAprovado() {
        OrcamentoEntity orc = orcamentoDisponivel();
        orc.setStatus(StatusOrcamento.APROVADO);

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        OrcamentoEntity result = service.aprovar(10L, "cliente@exemplo.com");

        assertSame(orc, result);
        verify(orcamentoRepository, never()).save(any());
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void aprovar_deveDarBadRequestQuandoOrcamentoNaoDisponivel() {
        OrcamentoEntity orc = orcamentoDisponivel();
        orc.setStatus(StatusOrcamento.SUBSTITUIDO);

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.aprovar(10L, "cliente@exemplo.com"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(orcamentoRepository, never()).save(any());
    }

    @Test
    void aprovar_deveRetornarForbiddenQuandoClienteNaoForDono() {
        OrcamentoEntity orc = orcamentoDisponivel();
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("outro@exemplo.com", "Outro Cliente", RoleEnum.CLIENTE);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.aprovar(10L, "outro@exemplo.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(orcamentoRepository, never()).save(any());
    }

    @Test
    void recusar_deveReprovarOrcamentoEFinalizarOs() {
        OrcamentoEntity orc = orcamentoDisponivel();
        OrdemServicoEntity os = osAguardandoAprovacao(orc.getOrdemServicoId());

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        when(orcamentoRepository.save(any(OrcamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ordemServicoRepository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));
        when(ordemServicoRepository.save(any(OrdemServicoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrcamentoEntity result = service.recusar(10L, "Nao quero", "cliente@exemplo.com");

        assertEquals(StatusOrcamento.REPROVADO, result.getStatus());
        assertEquals("Nao quero", result.getRecusaMotivo());
        assertEquals("Maria", result.getAssinaturaNome());
        assertNotNull(result.getReprovadoEm());
        assertEquals(StatusOrdemServico.FINALIZADA, os.getStatus());
        verify(ordemServicoRepository).save(os);
    }

    @Test
    void recusar_deveRecusarReparoAdicionalQuandoOrcamentoTemReparoVinculadoSemFinalizarOs() {
        OrcamentoEntity orc = orcamentoDisponivel();

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        when(orcamentoRepository.save(any(OrcamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reparoAdicionalService.existePorOrcamentoId(10L)).thenReturn(true);

        OrcamentoEntity result = service.recusar(10L, "Cliente recusou adicional", "cliente@exemplo.com");

        assertEquals(StatusOrcamento.REPROVADO, result.getStatus());
        assertEquals("Cliente recusou adicional", result.getRecusaMotivo());
        assertNotNull(result.getReprovadoEm());
        verify(reparoAdicionalService).recusarSeExistirPorOrcamentoId(10L, "Cliente recusou adicional");
        verify(ordemServicoRepository, never()).findById(any());
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void recusar_deveReprovarSemMotivoQuandoMotivoNulo() {
        OrcamentoEntity orc = orcamentoDisponivel();
        OrdemServicoEntity os = osAguardandoAprovacao(orc.getOrdemServicoId());

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        when(orcamentoRepository.save(any(OrcamentoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ordemServicoRepository.findByNumeroOs("OS-123")).thenReturn(Optional.of(os));

        OrcamentoEntity result = service.recusar(10L, null, "cliente@exemplo.com");

        assertEquals(StatusOrcamento.REPROVADO, result.getStatus());
        assertNull(result.getRecusaMotivo());
        verify(ordemServicoRepository).save(os);
    }

    @Test
    void recusar_deveDarBadRequestQuandoJaAprovado() {
        OrcamentoEntity orc = orcamentoDisponivel();
        orc.setStatus(StatusOrcamento.APROVADO);

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.recusar(10L, "x", "cliente@exemplo.com"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void recusar_deveRetornarMesmoOrcamentoQuandoJaReprovado() {
        OrcamentoEntity orc = orcamentoDisponivel();
        orc.setStatus(StatusOrcamento.REPROVADO);

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        OrcamentoEntity result = service.recusar(10L, "x", "cliente@exemplo.com");

        assertSame(orc, result);
        verify(orcamentoRepository, never()).save(any());
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void recusar_deveDarBadRequestQuandoOrcamentoNaoDisponivel() {
        OrcamentoEntity orc = orcamentoDisponivel();
        orc.setStatus(StatusOrcamento.SUBSTITUIDO);

        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("cliente@exemplo.com", "Maria", RoleEnum.CLIENTE);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.recusar(10L, "x", "cliente@exemplo.com"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(orcamentoRepository, never()).save(any());
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void recusar_deveRetornarForbiddenQuandoClienteNaoForDono() {
        OrcamentoEntity orc = orcamentoDisponivel();
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orc));
        mockUsuario("outro@exemplo.com", "Outro Cliente", RoleEnum.CLIENTE);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.recusar(10L, "x", "outro@exemplo.com"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(orcamentoRepository, never()).save(any());
    }

    private OrcamentoEntity orcamentoDisponivel() {
        OrcamentoEntity orc = new OrcamentoEntity();
        orc.setId(10L);
        orc.setOrdemServicoId(1L);
        orc.setNumeroOs("OS-123");
        orc.setStatus(StatusOrcamento.DISPONIVEL);
        orc.setCriadoEm(LocalDateTime.of(2026, 5, 31, 10, 0));
        orc.setCliente(ClienteOrcamentoSnapshot.builder()
                .nome("Cliente")
                .cpfCnpj("12345678901")
                .email("cliente@exemplo.com")
                .telefone("11999999999")
                .build());
        return orc;
    }

    private OrdemServicoEntity osAguardandoAprovacao(Long osId) {
        OrdemServicoEntity os = new OrdemServicoEntity();
        os.setId(osId);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        return os;
    }

    @Test
    void deveConsultarOrcamentosPorStatusEPlacaQuandoAdmin() {

        OrcamentoFiltro filtro = new OrcamentoFiltro(
                StatusOrcamento.DISPONIVEL,
                null,
                "ABC1234",
                null,
                null,
                null
        );
        OrcamentoEntity orcamento = orcamentoDisponivel();
        Specification<OrcamentoEntity> specification = semPredicado();

        mockUsuario("admin@autoflow.com", "Admin", RoleEnum.ADMIN);
        try (MockedStatic<OrcamentoSpecifications> specifications = mockSpecifications(filtro, specification)) {
            when(orcamentoRepository.findAll(specification)).thenReturn(List.of(orcamento));

            List<OrcamentoEntity> resultado =
                    service.consultarOrcamentos("admin@autoflow.com", filtro);

            assertEquals(1, resultado.size());
            assertSame(orcamento, resultado.getFirst());

            specifications.verify(() -> OrcamentoSpecifications.comFiltros(filtro));
            verify(orcamentoRepository).findAll(specification);
        }
    }

    @Test
    void deveConsultarOrcamentosPorNumeroOsQuandoAtendente() {
        OrcamentoFiltro filtro = new OrcamentoFiltro(
                null,
                "OS-123",
                null,
                null,
                null,
                null
        );
        OrcamentoEntity orcamento = orcamentoDisponivel();
        Specification<OrcamentoEntity> specification = semPredicado();

        mockUsuario("atendente@autoflow.com", "Atendente", RoleEnum.ATENDENTE);
        try (MockedStatic<OrcamentoSpecifications> specifications = mockSpecifications(filtro, specification)) {
            when(orcamentoRepository.findAll(specification)).thenReturn(List.of(orcamento));

            List<OrcamentoEntity> resultado =
                    service.consultarOrcamentos("atendente@autoflow.com", filtro);

            assertEquals(1, resultado.size());
            assertSame(orcamento, resultado.getFirst());

            specifications.verify(() -> OrcamentoSpecifications.comFiltros(filtro));
            verify(orcamentoRepository).findAll(specification);
        }
    }

    @Test
    void deveForcarClienteEmailQuandoClienteConsultaSemFiltros() {
        OrcamentoFiltro filtroEfetivo = new OrcamentoFiltro(
                null,
                null,
                null,
                "cliente@exemplo.com",
                null,
                null
        );
        OrcamentoEntity orcamento = orcamentoDisponivel();
        Specification<OrcamentoEntity> specification = semPredicado();

        mockUsuario("cliente@exemplo.com", "Cliente", RoleEnum.CLIENTE);
        try (MockedStatic<OrcamentoSpecifications> specifications = mockSpecifications(filtroEfetivo, specification)) {
            when(orcamentoRepository.findAll(specification)).thenReturn(List.of(orcamento));

            List<OrcamentoEntity> resultado =
                    service.consultarOrcamentos("cliente@exemplo.com", null);

            assertEquals(1, resultado.size());
            assertSame(orcamento, resultado.getFirst());

            specifications.verify(() -> OrcamentoSpecifications.comFiltros(filtroEfetivo));
            verify(orcamentoRepository).findAll(specification);
        }
    }

    @Test
    void deveRetornarForbiddenQuandoClienteEnviaClienteEmailDiferente() {
        OrcamentoFiltro filtro = new OrcamentoFiltro(
                null,
                null,
                null,
                "outro@exemplo.com",
                null,
                null
        );
        mockUsuario("cliente@exemplo.com", "Cliente", RoleEnum.CLIENTE);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.consultarOrcamentos("cliente@exemplo.com", filtro));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(orcamentoRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void deveManterPlacaMasForcarClienteEmailQuandoClienteConsultaComPlaca() {
        OrcamentoFiltro filtro = new OrcamentoFiltro(
                null,
                null,
                "ABC1234",
                null,
                null,
                TipoOrcamento.PRINCIPAL
        );
        OrcamentoFiltro filtroEfetivo = new OrcamentoFiltro(
                null,
                null,
                "ABC1234",
                "cliente@exemplo.com",
                null,
                TipoOrcamento.PRINCIPAL
        );
        OrcamentoEntity orcamento = orcamentoDisponivel();
        Specification<OrcamentoEntity> specification = semPredicado();

        mockUsuario("cliente@exemplo.com", "Cliente", RoleEnum.CLIENTE);
        try (MockedStatic<OrcamentoSpecifications> specifications = mockSpecifications(filtroEfetivo, specification)) {
            when(orcamentoRepository.findAll(specification)).thenReturn(List.of(orcamento));

            List<OrcamentoEntity> resultado =
                    service.consultarOrcamentos("cliente@exemplo.com", filtro);

            assertEquals(1, resultado.size());
            assertSame(orcamento, resultado.getFirst());

            specifications.verify(() -> OrcamentoSpecifications.comFiltros(filtroEfetivo));
            verify(orcamentoRepository).findAll(specification);
        }
    }

    private void mockUsuario(String email, String nome, RoleEnum role) {
        when(usuarioService.buscarPorEmail(email)).thenReturn(usuario(email, nome, role));
    }

    private UsuarioEntity usuario(String email, String nome, RoleEnum role) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail(email);
        usuario.setNome(nome);
        usuario.setRole(role);
        return usuario;
    }

    private Specification<OrcamentoEntity> semPredicado() {
        return (root, query, criteriaBuilder) -> null;
    }

    private MockedStatic<OrcamentoSpecifications> mockSpecifications(
            OrcamentoFiltro filtroEsperado,
            Specification<OrcamentoEntity> specification
    ) {
        MockedStatic<OrcamentoSpecifications> specifications = org.mockito.Mockito.mockStatic(OrcamentoSpecifications.class);
        specifications.when(() -> OrcamentoSpecifications.comFiltros(filtroEsperado)).thenReturn(specification);
        return specifications;
    }
}

