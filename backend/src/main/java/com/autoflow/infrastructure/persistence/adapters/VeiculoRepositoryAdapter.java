package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.dto.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.dto.veiculo.PageInput;
import com.autoflow.application.dto.veiculo.PageOutput;
import com.autoflow.application.dto.veiculo.VeiculoFiltro;
import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.mapper.VeiculoMapper;
import com.autoflow.infrastructure.persistence.repository.ClienteRepository;
import com.autoflow.infrastructure.persistence.repository.VeiculoRepository;
import com.autoflow.infrastructure.persistence.repository.VeiculoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VeiculoRepositoryAdapter implements VeiculoGateway {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoMapper veiculoMapper;

    @Override
    public VeiculoOutput save(CadastrarVeiculoInput input, Long clienteId) {
        ClienteEntity cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalStateException("Cliente não encontrado durante o cadastro do veículo"));

        VeiculoEntity veiculo = veiculoMapper.mapToEntity(input, cliente);
        return veiculoMapper.mapToOutput(veiculoRepository.save(veiculo));
    }

    @Override
    public VeiculoOutput save(VeiculoOrdemServicoInput input, Long clienteId) {
        return save(
                new CadastrarVeiculoInput(
                        null,
                        input.placa(),
                        input.marca(),
                        input.modelo(),
                        input.ano()),
                clienteId);
    }

    @Override
    public VeiculoOutput update(Long id, VeiculoInput input) {
        VeiculoEntity veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Veículo não encontrado durante a atualização"));

        veiculoMapper.updateEntity(input, veiculo);
        return veiculoMapper.mapToOutput(veiculoRepository.save(veiculo));
    }

    @Override
    public Optional<VeiculoOutput> findById(Long id) {
        return veiculoRepository.findById(id).map(veiculoMapper::mapToOutput);
    }

    @Override
    public Optional<VeiculoOutput> findByPlaca(String placa) {
        return veiculoRepository.findByPlaca(placa).map(veiculoMapper::mapToOutput);
    }

    @Override
    public boolean existsByPlaca(String placa) {
        return veiculoRepository.existsByPlaca(placa);
    }

    @Override
    public PageOutput<VeiculoOutput> findAll(VeiculoFiltro filtro, PageInput page) {
        PageRequest pageable = PageRequest.of(page.page(), page.size(), Sort.by("id").descending());
        Page<VeiculoOutput> result = veiculoRepository
                .findAll(VeiculoSpecifications.comFiltros(filtro), pageable)
                .map(veiculoMapper::mapToOutput);

        return new PageOutput<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Override
    public boolean existsById(Long id) {
        return veiculoRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        veiculoRepository.deleteById(id);
    }
}
