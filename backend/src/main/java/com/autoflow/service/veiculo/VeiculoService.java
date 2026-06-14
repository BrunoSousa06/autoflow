package com.autoflow.service.veiculo;

import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.controller.veiculo.request.VeiculoRequest;
import com.autoflow.controller.veiculo.request.VeiculoUpdateRequest;
import com.autoflow.controller.veiculo.response.VeiculoResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.mapper.VeiculoMapper;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.repository.veiculo.VeiculoRepository;
import com.autoflow.repository.veiculo.VeiculoSpecifications;
import com.autoflow.service.veiculo.dto.VeiculoFiltro;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoMapper veiculoMapper;

    public VeiculoResponse cadastrar(VeiculoRequest request) {

        ClienteEntity cliente =
                clienteRepository.findByCpfCnpj(request.cpfCnpj())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado com o CPF/CNPJ: " + request.cpfCnpj()));

        if(veiculoRepository.existsByPlaca(request.placa())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um veiculo cadastrado com a placa:" + request.placa());
        }
        VeiculoEntity veiculo = veiculoRepository.save(veiculoMapper.mapToEntity(request, cliente));

        return veiculoMapper.mapToResponse(veiculo);
    }

    public VeiculoResponse listar(Long id) {
        VeiculoEntity veiculo = buscarPorId(id);
        validarPermissaoCliente(veiculo);
        return veiculoMapper.mapToResponse(veiculo);
    }

    public Page<VeiculoResponse> listarComFiltros(VeiculoFiltro filtro, Pageable pageable) {
        Long clienteId = getClienteIdSeCliente();
        VeiculoFiltro filtroEfetivo = new VeiculoFiltro(
                filtro.placa(), filtro.marca(), filtro.modelo(), filtro.ano(), clienteId);
        return veiculoRepository
                .findAll(VeiculoSpecifications.comFiltros(filtroEfetivo), pageable)
                .map(veiculoMapper::mapToResponse);
    }

    private Long getClienteIdSeCliente() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isCliente = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
        if (!isCliente) {
            return null;
        }
        return clienteRepository.findByUsuarioEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Cliente não encontrado para o usuário autenticado"))
                .getId();
    }

    public VeiculoResponse atualizar(VeiculoUpdateRequest request, Long id) {

        VeiculoEntity veiculo = buscarPorId(id);

        validarPermissaoCliente(veiculo);

        Optional<VeiculoEntity> veiculoPlaca =
                veiculoRepository.findByPlaca(request.placa());

        if (veiculoPlaca.isPresent()
                && !veiculoPlaca.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Placa já cadastrada");
        }

        veiculoMapper.updateEntity(request, veiculo);

        VeiculoEntity atualizado = veiculoRepository.save(veiculo);

        return veiculoMapper.mapToResponse(atualizado);
    }

    private void validarPermissaoCliente(VeiculoEntity veiculo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isCliente = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));
        if (!isCliente) {
            return;
        }
        ClienteEntity clienteLogado = clienteRepository.findByUsuarioEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Cliente não encontrado para o usuário autenticado"));
        if (!veiculo.getCliente().getId().equals(clienteLogado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este veículo");
        }
    }

    public void deletar(Long id) {

        if (!veiculoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado com o ID: " + id
            );
        }

        veiculoRepository.deleteById(id);
    }

    public VeiculoEntity buscarPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado com o ID: " + id));
    }

    public VeiculoEntity buscarOuCadastrarPorPlacaParaCliente(
            ClienteEntity cliente,
            VeiculoOrdemServicoRequest request
    ) {
        String placaNormalizada = normalizarPlaca(request.placa());

        Optional<VeiculoEntity> existente = veiculoRepository.findByPlaca(placaNormalizada);

        if (existente.isPresent()) {
            VeiculoEntity veiculo = existente.get();

            if (!veiculo.getCliente().getId().equals(cliente.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Placa ja cadastrada para outro cliente.");
            }

            return veiculo;
        }

        if (request.marca() == null || request.marca().isBlank()
                || request.modelo() == null || request.modelo().isBlank()
                || request.ano() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Marca, modelo e ano são obrigatórios para cadastrar um novo veiculo."
            );
        }

        VeiculoEntity novo = new VeiculoEntity();
        novo.setCliente(cliente);
        novo.setPlaca(placaNormalizada);
        novo.setMarca(request.marca());
        novo.setModelo(request.modelo());
        novo.setAno(request.ano());

        return veiculoRepository.save(novo);
    }

    private String normalizarPlaca(String placa) {
        return placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}

