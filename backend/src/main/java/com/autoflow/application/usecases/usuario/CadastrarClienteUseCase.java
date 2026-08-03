package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.mapper.UsuarioMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CadastrarClienteUseCase {

    private final ClienteGateway clienteGateway;
    private final UsuarioMapper usuarioMapper;

    @Transactional
    public ClienteEntity execute(
            RegistroInput request,
            UsuarioEntity usuario) {

        if (clienteGateway.existsByCpfCnpj(request.cpfCnpj())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "CPF/CNPJ já cadastrado");
        }

        ClienteEntity cliente =
                usuarioMapper.mapToClienteEntity(request);

        cliente.setUsuario(usuario);

        return clienteGateway.save(cliente);
    }
}
