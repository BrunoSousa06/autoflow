package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.application.dto.orcamento.OrcamentoFiltro;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultarOrcamentosUseCase {
    private final OrcamentoGateway orcamentoGateway;
    private final UsuarioGateway usuarioGateway;

    public List<OrcamentoEntity> execute(String emailUsuario, OrcamentoFiltro filtro) {
        var usuario = usuarioGateway.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário autenticado não encontrado"));
        OrcamentoFiltro filtroEfetivo = normalizar(filtro);
        if (usuario.getRole() == RoleEnum.CLIENTE) {
            if (filtroEfetivo.clienteEmail() != null && !filtroEfetivo.clienteEmail().isBlank()
                    && !filtroEfetivo.clienteEmail().equalsIgnoreCase(usuario.getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
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
