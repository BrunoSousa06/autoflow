package com.autoflow.service.ordemServico;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.repository.ordemServico.OrdemServicoRepository;
import com.autoflow.service.cliente.ClienteService;
import com.autoflow.service.servico.ServicoService;
import com.autoflow.service.veiculo.VeiculoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;

    private final ClienteService clienteService;

    private final VeiculoService veiculoService;

    private final ServicoService servicoService;

    public OrdemServicoEntity criar(
            Long clienteId,
            Long veiculoId,
            List<ServicoSolicitadoEntity> servicosSolicitados
    ) {
        ClienteEntity cliente = clienteService.buscarPorId(clienteId);
        VeiculoEntity veiculo = veiculoService.buscarPorId(veiculoId);
        validarVeiculoDoCliente(veiculo, cliente);

        List<ServicoSolicitadoEntity> servicoComDados = preencherDadosDosServicos(servicosSolicitados);

        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(cliente, veiculo, servicoComDados);
        return ordemServicoRepository.save(ordemServicoEntity);
    }

    @Transactional
    public OrdemServicoEntity incluirServicos(Long ordemServicoId, List<ServicoSolicitadoEntity> servicos){

        OrdemServicoEntity ordemServico = ordemServicoRepository.findById(ordemServicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ordem de servico nao encontrada."
                ));
        List<ServicoSolicitadoEntity> servicosComDados = preencherDadosDosServicos(servicos);

        ordemServico.adicionarServicos(servicosComDados);

        return ordemServicoRepository.save(ordemServico);
    }

    private List<ServicoSolicitadoEntity> preencherDadosDosServicos(List<ServicoSolicitadoEntity> servicos) {
        validarServicosSolicitados(servicos);

        return servicos.stream()
                .map(this::preencherDadosDoServico)
                .toList();
    }


    private ServicoSolicitadoEntity preencherDadosDoServico(ServicoSolicitadoEntity servicoSolicitado) {
        ServicoEntity servico = servicoService.buscarEntityPorId(
                servicoSolicitado.getServicoId()
        );

        return new ServicoSolicitadoEntity(
                servico.getId(),
                servico.getNome(),
                servico.getValor()
        );
    }

    private static void validarServicosSolicitados(List<ServicoSolicitadoEntity> servicos) {
        if (servicos == null || servicos.isEmpty()) {
            throw new IllegalArgumentException("A ordem de servico deve ter ao menos um servico solicitado.");
        }
    }

    private static void validarVeiculoDoCliente(VeiculoEntity veiculo, ClienteEntity cliente) {
        if (veiculo.getCliente() == null || !veiculo.getCliente().getId().equals(cliente.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Veiculo nao pertence ao cliente informado.");
        }
    }

}
