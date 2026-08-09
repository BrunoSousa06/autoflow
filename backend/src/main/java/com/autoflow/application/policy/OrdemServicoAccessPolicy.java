package com.autoflow.application.policy;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.domain.ordemservico.DiagnosticoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoAccessPolicy {
    public void validarPodeAlterarDiagnostico(OrdemServicoEntity ordemServico, UsuarioEntity usuario) {
        if (RoleEnum.ADMIN.equals(usuario.getRole())) return;
        DiagnosticoEntity diagnostico = ordemServico.getDiagnostico();
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
