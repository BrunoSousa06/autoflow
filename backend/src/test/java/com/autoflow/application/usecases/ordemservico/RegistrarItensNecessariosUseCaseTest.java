package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import com.autoflow.application.usecases.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarItensNecessariosUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private OrdemServicoAccessPolicy accessPolicy;

    @Mock
    private ConsultarDisponibilidadeEstoqueUseCase disponibilidadeEstoque;

    @Test
    void deveRegistrarItensParaAdminDuranteDiagnostico() {
        var os = ordemEmDiagnostico();
        var usuario = usuario(RoleEnum.ADMIN, "admin@autoflow.com");
        var itens = List.of(itemSolicitado());
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(os));
        when(usuarioGateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(disponibilidadeEstoque.execute(itens)).thenReturn(itens);
        when(ordemServicoGateway.save(os)).thenReturn(os);

        var resultado = new RegistrarItensNecessariosUseCase(
                ordemServicoGateway, usuarioGateway, accessPolicy, disponibilidadeEstoque)
                .execute("OS-1", 10L, usuario.getEmail(), itens);

        assertEquals(itens, resultado.buscarServicoSolicitado(10L).getItensNecessarios());
        verify(accessPolicy, never()).validarPodeAlterarDiagnostico(os, usuario);
        verify(ordemServicoGateway).save(os);
    }

    @Test
    void deveValidarAcessoDoUsuarioQueNaoEAdmin() {
        var os = ordemEmDiagnostico();
        var usuario = usuario(RoleEnum.MECANICO, "mecanico@autoflow.com");
        var itens = List.of(itemSolicitado());
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(os));
        when(usuarioGateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(disponibilidadeEstoque.execute(itens)).thenReturn(itens);
        when(ordemServicoGateway.save(os)).thenReturn(os);

        new RegistrarItensNecessariosUseCase(
                ordemServicoGateway, usuarioGateway, accessPolicy, disponibilidadeEstoque)
                .execute("OS-1", 10L, usuario.getEmail(), itens);

        verify(accessPolicy).validarPodeAlterarDiagnostico(os, usuario);
    }

    @Test
    void deveRejeitarRegistroForaDoDiagnostico() {
        var os = ordemEmDiagnostico();
        os.setStatus(StatusOrdemServico.RECEBIDA);
        var usuario = usuario(RoleEnum.ADMIN, "admin@autoflow.com");
        var itens = List.of(itemSolicitado());
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(os));
        when(usuarioGateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        var exception = assertThrows(ResponseStatusException.class,
                () -> new RegistrarItensNecessariosUseCase(
                        ordemServicoGateway, usuarioGateway, accessPolicy, disponibilidadeEstoque)
                        .execute("OS-1", 10L, usuario.getEmail(), itens));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(disponibilidadeEstoque, never()).execute(itens);
        verify(ordemServicoGateway, never()).save(os);
    }

    private OrdemServicoEntity ordemEmDiagnostico() {
        var os = new OrdemServicoEntity();
        os.setNumeroOs("OS-1");
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        os.adicionarServicosSolicitados(List.of(
                ServicoSolicitadoEntity.criar(10L, "Servico", BigDecimal.TEN)));
        return os;
    }

    private ItemNecessarioEntity itemSolicitado() {
        var item = new ItemNecessarioEntity();
        item.setPecaInsumoId(20L);
        item.setQuantidade(2);
        return item;
    }

    private UsuarioEntity usuario(RoleEnum role, String email) {
        var usuario = new UsuarioEntity();
        usuario.setRole(role);
        usuario.setEmail(email);
        return usuario;
    }
}
