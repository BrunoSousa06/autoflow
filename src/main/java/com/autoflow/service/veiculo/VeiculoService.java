package com.autoflow.service.veiculo;

import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.controller.veiculo.request.VeiculoRequest;
import com.autoflow.controller.veiculo.response.VeiculoResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.mapper.VeiculoMapper;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.repository.veiculo.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
        return veiculoMapper.mapToResponse(buscarPorId(id));
    }

    public List<VeiculoResponse> listarTodosVeiculos() {
        return veiculoMapper.mapToList(veiculoRepository.findAll());
    }

    public VeiculoResponse atualizar(VeiculoRequest request, Long id) {

        VeiculoEntity veiculo = buscarPorId(id);

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

