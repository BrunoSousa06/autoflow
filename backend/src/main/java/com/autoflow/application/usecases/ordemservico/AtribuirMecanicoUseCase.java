package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.ordemservico.DiagnosticoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class AtribuirMecanicoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;
    private final UsuarioGateway usuarioGateway;

    @Transactional
    public OrdemServicoEntity execute(String numeroOs, Long mecanicoId, String mecanicoEmail) {
        OrdemServicoEntity os = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
        UsuarioEntity mecanico = buscarMecanico(mecanicoId, mecanicoEmail);
        if (os.getDiagnostico() == null) os.setDiagnostico(new DiagnosticoEntity());
        os.getDiagnostico().setMecanico(mecanico);
        return ordemServicoGateway.save(os);
    }

    private UsuarioEntity buscarMecanico(Long id, String email) {
        UsuarioEntity usuario;
        if (id != null) {
            usuario = usuarioGateway.findById(id).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Mecânico não encontrado."));
        } else if (email != null && !email.isBlank()) {
            usuario = usuarioGateway.findByEmail(email).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Usuário autenticado não encontrado."));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe mecanicoId ou mecanicoEmail.");
        }
        if (!RoleEnum.MECANICO.equals(usuario.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário informado não é mecânico.");
        }
        return usuario;
    }
}
