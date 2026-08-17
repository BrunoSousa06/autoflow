package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.application.exception.UsuarioNaoEncontradoException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.port.in.ordemservico.ListarOrdensServicoUseCase;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ListarOrdensServicoUseCaseImpl implements ListarOrdensServicoUseCase {

    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;

    @Override
    public PageResult<OrdemServico> execute(
            OrdemServicoFiltroInput filtro,
            PageQuery pageQuery,
            String emailUsuarioLogado
    ) {
        Usuario usuario = usuarioGateway.findByEmail(emailUsuarioLogado)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        String emailMecanico = RoleEnum.MECANICO.equals(usuario.getRole())
                ? emailUsuarioLogado
                : null;
        return ordemServicoGateway.findAll(filtro, emailMecanico, pageQuery);
    }
}
