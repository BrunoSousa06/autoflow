package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import lombok.RequiredArgsConstructor;

/**
 * Orquestra a autorização do solicitante com a decisão de aprovação ou recusa.
 * As transições de estado permanecem nos casos de uso específicos.
 */

@RequiredArgsConstructor
public class DecidirOrcamentoUseCase {

    private final OrcamentoGateway orcamentoGateway;
    private final UsuarioGateway usuarioGateway;
    private final AprovarOrcamentoUseCase aprovarOrcamentoUseCase;
    private final RecusarOrcamentoUseCase recusarOrcamentoUseCase;

    @TransactionalUseCase
    public OrcamentoEntity aprovarComoUsuario(Long orcamentoId, String emailUsuario) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        UsuarioEntity usuario = validarAcesso(orcamento, emailUsuario);
        return aprovarOrcamentoUseCase.execute(orcamento, usuario.getNome());
    }

    @TransactionalUseCase
    public OrcamentoEntity recusarComoUsuario(Long orcamentoId, String motivo, String emailUsuario) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        UsuarioEntity usuario = validarAcesso(orcamento, emailUsuario);
        return recusarOrcamentoUseCase.execute(orcamento, motivo, usuario.getNome());
    }

    @TransactionalUseCase
    public OrcamentoEntity aprovarDaOrdem(Long orcamentoId, String numeroOs) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        if (!orcamento.getNumeroOs().equals(numeroOs)) {
            throw ApplicationException.notFound(
                    "Orçamento não encontrado para esta ordem de serviço");
        }
        return aprovarOrcamentoUseCase.execute(orcamento, orcamento.getCliente().getNome());
    }

    private OrcamentoEntity buscarOrcamento(Long orcamentoId) {
        return orcamentoGateway.findById(orcamentoId)
                .orElseThrow(() -> ApplicationException.notFound("Orçamento não encontrado"));
    }

    private UsuarioEntity validarAcesso(OrcamentoEntity orcamento, String emailUsuario) {
        UsuarioEntity usuario = usuarioGateway.findByEmail(emailUsuario)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Usuário autenticado não encontrado"));
        if (!RoleEnum.ADMIN.equals(usuario.getRole()) && !RoleEnum.CLIENTE.equals(usuario.getRole())) {
            throw ApplicationException.forbidden(
                    "Somente cliente ou administrador pode decidir o orçamento.");
        }
        if (RoleEnum.CLIENTE.equals(usuario.getRole()) && !emailUsuario.equals(orcamento.getCliente().getEmail())) {
            throw ApplicationException.forbidden();
        }
        return usuario;
    }
}
