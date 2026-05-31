package com.autoflow.service.ordemServico;

import com.autoflow.domain.ordemServico.DiagnosticoEntity;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class OrdemServicoAccessPolicy {

    public void validarPodeAlterarDiagnostico(OrdemServicoEntity ordemServico, UsuarioEntity usuarioLogado) {
        if (RoleEnum.ADMIN.equals(usuarioLogado.getRole())) {
            return;
        }

        DiagnosticoEntity diagnostico = ordemServico.getDiagnostico();
        if (diagnostico == null || diagnostico.getMecanico() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A ordem de serviço ainda não possui mecânico atribuído."
            );
        }

        Long mecanicoAtribuidoId = diagnostico.getMecanico().getId();
        if (!mecanicoAtribuidoId.equals(usuarioLogado.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Somente o mecânico atribuído pode alterar o diagnóstico."
            );
        }
    }
}
