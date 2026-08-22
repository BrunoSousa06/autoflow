package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.input.orcamento.OrcamentoFiltro;
import com.autoflow.application.port.in.orcamento.ConsultarOrcamentosUseCase;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ConsultarOrcamentosUseCaseImpl implements ConsultarOrcamentosUseCase {
    private final OrcamentoGateway orcamentoGateway;
    private final UsuarioGateway usuarioGateway;

    @Override
    public List<Orcamento> execute(String emailUsuario, OrcamentoFiltro filtro) {
        var usuario = usuarioGateway.findByEmail(emailUsuario)
                .orElseThrow(() -> ApplicationException.notFound("Usuário autenticado não encontrado"));
        OrcamentoFiltro filtroEfetivo = normalizar(filtro);
        if (usuario.getRole() == RoleEnum.CLIENTE) {
            if (filtroEfetivo.clienteEmail() != null && !filtroEfetivo.clienteEmail().isBlank()
                    && !filtroEfetivo.clienteEmail().equalsIgnoreCase(usuario.getEmail())) {
                throw ApplicationException.forbidden();
            }
            filtroEfetivo = new OrcamentoFiltro(filtroEfetivo.status(), filtroEfetivo.numeroOs(),
                    filtroEfetivo.placa(), usuario.getEmail(), filtroEfetivo.clienteDocumento(), filtroEfetivo.tipo());
        }
        return orcamentoGateway.findAll(filtroEfetivo);
    }

    private OrcamentoFiltro normalizar(OrcamentoFiltro filtro) {
        return filtro != null ? filtro : new OrcamentoFiltro(null, null, null, null, null, null);
    }
}
