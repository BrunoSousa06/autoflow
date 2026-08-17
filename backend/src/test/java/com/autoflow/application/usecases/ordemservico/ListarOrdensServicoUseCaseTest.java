package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarOrdensServicoUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;
    @Mock
    private UsuarioGateway usuarioGateway;

    @Test
    void deveListarOrdensDoMecanicoUsandoEmailComoRestricao() {
        Usuario mecanico = new Usuario();
        mecanico.setRole(RoleEnum.MECANICO);
        PageQuery pageQuery = new PageQuery(0, 10);
        PageResult<OrdemServico> esperado = new PageResult<>(List.of(), 0, 0, 10);
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));
        when(ordemServicoGateway.findAll(any(), eq("mecanico@autoflow.com"), eq(pageQuery)))
                .thenReturn(esperado);

        PageResult<OrdemServico> resultado = new ListarOrdensServicoUseCase(ordemServicoGateway, usuarioGateway)
                .execute(new OrdemServicoFiltroInput(null, null, null), pageQuery, "mecanico@autoflow.com");

        assertSame(esperado, resultado);
        verify(ordemServicoGateway).findAll(any(), eq("mecanico@autoflow.com"), eq(pageQuery));
    }

    @Test
    void deveRejeitarUsuarioAutenticadoInexistente() {
        var useCase = new ListarOrdensServicoUseCase(ordemServicoGateway, usuarioGateway);
        var filtro = new OrdemServicoFiltroInput(null, null, null);
        var pageQuery = new PageQuery(0, 10);
        var email = "ausente@autoflow.com";
        when(usuarioGateway.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> useCase.execute(filtro, pageQuery, email));
    }
}
