package com.autoflow.service.ordemServico;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.repository.ordemServico.OrdemServicoRepository;
import com.autoflow.service.cliente.ClienteService;
import com.autoflow.service.veiculo.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    @Autowired
    private final OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private final ClienteService clienteService;

    @Autowired
    private final VeiculoService veiculoService;


    public OrdemServicoEntity criar(
            Long clienteId,
            Long veiculoId,
            List<ServicoSolicitadoEntity> servicosSolicitados
    ) {
        ClienteEntity cliente = clienteService.buscarPorId(clienteId);
        VeiculoEntity veiculo = veiculoService.buscarPorId(veiculoId);
        validarVeiculoDoCliente(veiculo, cliente);

        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(cliente, veiculo, servicosSolicitados);
        return ordemServicoRepository.save(ordemServicoEntity);
    }

    public OrdemServicoEntity incluirServicos(
            Long ordemServicoId, List<ServicoSolicitadoEntity> servicos){
        OrdemServicoEntity ordemServicoEntity = ordemServicoRepository.findById(ordemServicoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        //TODO QUANDO CRIAR O CRUD DE SERVICOS UTILIZAR AQUI PARA VALIDAR SE O SERVICO JA TA REGISTRADO e salvar os dados
        ordemServicoEntity.adicionarServicos(servicos);
        return ordemServicoEntity;
    }

    private static void validarVeiculoDoCliente(VeiculoEntity veiculo, ClienteEntity cliente) {
        if (veiculo.getCliente() == null || !veiculo.getCliente().getId().equals(cliente.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Veiculo nao pertence ao cliente informado.");
        }
    }

}
