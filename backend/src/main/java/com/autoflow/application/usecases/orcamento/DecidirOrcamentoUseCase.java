package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Orquestra a autorização do solicitante com a decisão de aprovação ou recusa.
 * As transições de estado permanecem nos casos de uso específicos.
 */
@Service
@RequiredArgsConstructor
public class DecidirOrcamentoUseCase {

    private final OrcamentoGateway orcamentoGateway;
    private final UsuarioGateway usuarioGateway;
    private final AprovarOrcamentoUseCase aprovarOrcamentoUseCase;
    private final RecusarOrcamentoUseCase recusarOrcamentoUseCase;

    @Transactional
    public OrcamentoEntity aprovarComoUsuario(Long orcamentoId, String emailUsuario) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        UsuarioEntity usuario = validarAcesso(orcamento, emailUsuario);
        return aprovarOrcamentoUseCase.execute(orcamento, usuario.getNome());
    }

    @Transactional
    public OrcamentoEntity recusarComoUsuario(Long orcamentoId, String motivo, String emailUsuario) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        UsuarioEntity usuario = validarAcesso(orcamento, emailUsuario);
        return recusarOrcamentoUseCase.execute(orcamento, motivo, usuario.getNome());
    }

    @Transactional
    public OrcamentoEntity aprovarDaOrdem(Long orcamentoId, String numeroOs) {
        OrcamentoEntity orcamento = buscarOrcamento(orcamentoId);
        if (!orcamento.getNumeroOs().equals(numeroOs)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Orçamento não encontrado para esta ordem de serviço");
        }
        return aprovarOrcamentoUseCase.execute(orcamento, orcamento.getCliente().getNome());
    }

    private OrcamentoEntity buscarOrcamento(Long orcamentoId) {
        return orcamentoGateway.findById(orcamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado"));
    }

    private UsuarioEntity validarAcesso(OrcamentoEntity orcamento, String emailUsuario) {
        UsuarioEntity usuario = usuarioGateway.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuário autenticado não encontrado"));
        if (usuario.getRole() == RoleEnum.CLIENTE && !emailUsuario.equals(orcamento.getCliente().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return usuario;
    }
}
