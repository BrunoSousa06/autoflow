package com.autoflow.application.policy;

import com.autoflow.domain.ordemservico.DiagnosticoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class OrdemServicoAccessPolicy {
    public void validarPodeAlterarDiagnostico(OrdemServicoEntity ordemServico, UsuarioEntity usuario) {
        if (RoleEnum.ADMIN.equals(usuario.getRole())) return;
        DiagnosticoEntity diagnostico = ordemServico.getDiagnostico();
        if (diagnostico == null || diagnostico.getMecanico() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A ordem de serviço ainda não possui mecânico atribuído.");
        }
        if (!diagnostico.getMecanico().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Somente o mecânico atribuído pode alterar o diagnóstico.");
        }
    }
}
