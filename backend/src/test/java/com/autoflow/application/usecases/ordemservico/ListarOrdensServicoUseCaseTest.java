package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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
        UsuarioEntity mecanico = new UsuarioEntity();
        mecanico.setRole(RoleEnum.MECANICO);
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdemServicoEntity> esperado = new PageImpl<>(List.of());
        when(usuarioGateway.findByEmail("mecanico@autoflow.com")).thenReturn(Optional.of(mecanico));
        when(ordemServicoGateway.findAll(any(Specification.class), eq(pageable))).thenReturn(esperado);

        Page<OrdemServicoEntity> resultado = new ListarOrdensServicoUseCase(ordemServicoGateway, usuarioGateway)
                .execute(new OrdemServicoFiltroInput(null, null, null), pageable, "mecanico@autoflow.com");

        assertSame(esperado, resultado);
        verify(ordemServicoGateway).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void deveRejeitarUsuarioAutenticadoInexistente() {
        when(usuarioGateway.findByEmail("ausente@autoflow.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> new ListarOrdensServicoUseCase(ordemServicoGateway, usuarioGateway)
                .execute(new OrdemServicoFiltroInput(null, null, null), PageRequest.of(0, 10), "ausente@autoflow.com"));
    }
}
