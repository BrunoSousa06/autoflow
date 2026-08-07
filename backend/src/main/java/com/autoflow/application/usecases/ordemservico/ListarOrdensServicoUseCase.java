package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.repository.ordemservico.OrdemServicoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class ListarOrdensServicoUseCase {

    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;

    public Page<OrdemServicoEntity> execute(
            OrdemServicoFiltroInput filtro,
            Pageable pageable,
            String emailUsuarioLogado
    ) {
        UsuarioEntity usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário autenticado não encontrado."
                ));
        String emailMecanico = RoleEnum.MECANICO.equals(usuario.getRole())
                ? emailUsuarioLogado
                : null;
        return ordemServicoGateway.findAll(
                OrdemServicoSpecifications.comFiltros(filtro, emailMecanico),
                pageable
        );
    }
}
