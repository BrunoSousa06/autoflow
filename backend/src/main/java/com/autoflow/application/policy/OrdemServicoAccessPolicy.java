package com.autoflow.application.policy;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.domain.ordemservico.Diagnostico;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoAccessPolicy {
    public void validarPodeAlterarDiagnostico(OrdemServico ordemServico, Usuario usuario) {
        if (RoleEnum.ADMIN.equals(usuario.getRole())) return;
        Diagnostico diagnostico = ordemServico.getDiagnostico();
        if (diagnostico == null || diagnostico.getMecanico() == null) {
            throw ApplicationException.badRequest(
                    "A ordem de serviço ainda não possui mecânico atribuído.");
        }
        if (!diagnostico.getMecanico().getId().equals(usuario.getId())) {
            throw ApplicationException.forbidden(
                    "Somente o mecânico atribuído pode alterar o diagnóstico.");
        }
    }
}
