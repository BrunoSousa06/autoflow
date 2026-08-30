package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.input.ordemservico.reparoadicional.ItemReparoAdicionalCommand;
import com.autoflow.application.input.ordemservico.reparoadicional.ServicoReparoAdicionalCommand;
import com.autoflow.application.mapper.ServicoApplicationMapper;
import com.autoflow.application.output.servico.ServicoOutput;
import com.autoflow.application.port.in.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase;
import com.autoflow.domain.ordemservico.ItemNecessario;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnriquecerServicoReparoAdicionalService {

    private final ServicoGateway servicoGateway;
    private final ConsultarDisponibilidadeEstoqueUseCase estoque;

    public EnriquecerServicoReparoAdicionalService(ServicoGateway servicoGateway,
                                                   ConsultarDisponibilidadeEstoqueUseCase estoque) {
        this.servicoGateway = servicoGateway;
        this.estoque = estoque;
    }

    public ServicoSolicitado enriquecer(ServicoReparoAdicionalCommand command) {
        if (command.itensNecessarios() == null || command.itensNecessarios().isEmpty()) {
            throw new IllegalArgumentException("Servico do reparo adicional deve ter ao menos um item necessario.");
        }
        ServicoOutput catalogo = servicoGateway.findById(command.servicoId()).map(ServicoApplicationMapper::toOutput)
                .orElseThrow(() -> ApplicationException.notFound("Serviço não encontrado com o ID: " + command.servicoId()));
        List<ItemNecessario> itens = command.itensNecessarios().stream().map(this::mapearItem).toList();
        ServicoSolicitado servico = ServicoSolicitado.criar(catalogo.getId(), catalogo.getNome(), catalogo.getValor());
        servico.registrarItensNecessarios(estoque.execute(itens));
        return servico;
    }

    private ItemNecessario mapearItem(ItemReparoAdicionalCommand command) {
        if (command == null || command.pecaInsumoId() == null || command.quantidade() == null) {
            throw new IllegalArgumentException("Item necessario e obrigatorio.");
        }
        if (command.quantidade() <= 0) throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
        ItemNecessario item = new ItemNecessario();
        item.setPecaInsumoId(command.pecaInsumoId());
        item.setQuantidade(command.quantidade());
        return item;
    }
}
