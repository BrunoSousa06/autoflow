package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.port.in.orcamento.AprovarOrcamentoUseCase;
import com.autoflow.application.port.in.orcamento.DecidirOrcamentoUseCase;
import com.autoflow.application.port.in.orcamento.RecusarOrcamentoUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;

/**
 * Orquestra a autorização do solicitante com a decisão de aprovação ou recusa.
 * As transições de estado permanecem nos casos de uso específicos.
 */

@RequiredArgsConstructor
public class DecidirOrcamentoUseCaseImpl implements DecidirOrcamentoUseCase {

    private final OrcamentoGateway orcamentoGateway;
    private final OrcamentoPublicacaoGateway publicacaoGateway;
    private final UsuarioGateway usuarioGateway;
    private final AprovarOrcamentoUseCase aprovarOrcamentoUseCase;
    private final RecusarOrcamentoUseCase recusarOrcamentoUseCase;

    @TransactionalUseCase
    @Override
    public OrcamentoEntity aprovarComoUsuario(Long orcamentoId, String emailUsuario) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        Usuario usuario = validarAcesso(orcamento, emailUsuario);
        return aprovarOrcamentoUseCase.execute(orcamento, usuario.getNome());
    }

    @TransactionalUseCase
    @Override
    public OrcamentoEntity recusarComoUsuario(Long orcamentoId, String motivo, String emailUsuario) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        Usuario usuario = validarAcesso(orcamento, emailUsuario);
        return recusarOrcamentoUseCase.execute(orcamento, motivo, usuario.getNome());
    }

    @TransactionalUseCase
    @Override
    public OrcamentoEntity aprovarComoToken(Long orcamentoId, String token, String nome) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        validarToken(orcamento, token);
        return aprovarOrcamentoUseCase.execute(orcamento, normalizarAssinatura(orcamento, nome));
    }

    @TransactionalUseCase
    @Override
    public OrcamentoEntity recusarComoToken(Long orcamentoId, String token, String motivo, String nome) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        validarToken(orcamento, token);
        return recusarOrcamentoUseCase.execute(
                orcamento,
                motivo,
                normalizarAssinatura(orcamento, nome)
        );
    }

    @TransactionalUseCase
    @Override
    public OrcamentoEntity aprovarDaOrdem(Long orcamentoId, String numeroOs) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        if (!orcamento.getNumeroOs().equals(numeroOs)) {
            throw ApplicationException.notFound(
                    "Orçamento não encontrado para esta ordem de serviço");
        }
        return aprovarOrcamentoUseCase.execute(orcamento, orcamento.getCliente().getNome());
    }

    private OrcamentoEntity buscarOrcamento(Long orcamentoId) {
        return orcamentoGateway.findByIdForUpdate(orcamentoId)
                .orElseThrow(() -> ApplicationException.notFound("Orçamento não encontrado"));
    }

    private void validarToken(OrcamentoEntity orcamento, String token) {
        if (!publicacaoGateway.validarToken(orcamento, token)) {
            throw ApplicationException.unauthorized("Token invalido ou expirado");
        }
    }

    private String normalizarAssinatura(OrcamentoEntity orcamento, String nome) {
        String assinatura = nome == null || nome.isBlank()
                ? orcamento.getCliente().getNome()
                : nome.trim();
        if (assinatura != null && assinatura.length() > 120) {
            throw ApplicationException.badRequest(
                    "Nome da assinatura deve ter no máximo 120 caracteres");
        }
        return assinatura;
    }

    private Usuario validarAcesso(OrcamentoEntity orcamento, String emailUsuario) {
        Usuario usuario = usuarioGateway.findByEmail(emailUsuario)
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
