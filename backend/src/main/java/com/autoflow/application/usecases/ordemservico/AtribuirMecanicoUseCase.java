package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.DiagnosticoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AtribuirMecanicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;

    @TransactionalUseCase
    public OrdemServicoEntity execute(String numeroOs, Long mecanicoId, String mecanicoEmail) {
        OrdemServicoEntity os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound("Ordem de serviço não encontrada."));
        Usuario mecanico = buscarMecanico(mecanicoId, mecanicoEmail);
        if (os.getDiagnostico() == null) os.setDiagnostico(new DiagnosticoEntity());
        os.getDiagnostico().setMecanico(mecanico);
        return ordemServicoGateway.save(os);
    }

    private Usuario buscarMecanico(Long id, String email) {
        Usuario usuario;
        if (id != null) {
            usuario = usuarioGateway.findById(id).orElseThrow(() -> ApplicationException.notFound(
                    "Mecânico não encontrado."));
        } else if (email != null && !email.isBlank()) {
            usuario = usuarioGateway.findByEmail(email).orElseThrow(() -> ApplicationException.notFound(
                    "Usuário autenticado não encontrado."));
        } else {
            throw ApplicationException.badRequest("Informe mecanicoId ou mecanicoEmail.");
        }
        if (!RoleEnum.MECANICO.equals(usuario.getRole())) {
            throw ApplicationException.badRequest("Usuário informado não é mecânico.");
        }
        return usuario;
    }
}
