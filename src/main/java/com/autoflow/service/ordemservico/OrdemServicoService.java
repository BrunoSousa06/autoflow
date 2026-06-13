package com.autoflow.service.ordemservico;

import com.autoflow.controller.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import jakarta.transaction.Transactional;

import java.util.List;

public interface OrdemServicoService {
    OrdemServicoEntity criar(
            String cpfCnpj,
            VeiculoOrdemServicoRequest veiculo,
            List<ServicoSolicitadoEntity> servicosSolicitados
    );
    @Transactional
    OrdemServicoEntity incluirServicos(String numeroOs, List<ServicoSolicitadoEntity> servicos);

    OrdemServicoEntity atribuirMecanico(String numeroOs, Long mecanicoId);

    OrdemServicoEntity iniciarDiagnostico(String numeroOs, String emailUsuarioLogado);

    OrdemServicoEntity registrarItemNecessario(String numeroOs, Long servicoOsId, String emailUsuarioLogado, List<ItemNecessarioEntity> itensNecessarios);

    OrdemServicoEntity registrarLaudo(String numeroOs, String emailUsuarioLogado, String laudo);

    @Transactional
    FinalizarDiagnosticoResult finalizarDiagnostico(String numeroOs, String emailUsuarioLogado);

    OrdemServicoEntity buscaOrdemServicoPorId(Long ordemServicoId);

    OrdemServicoEntity buscaOrdemServicoPorNumeroOs(String numeroOs);

    @Transactional
    OrdemServicoEntity iniciarServico(String numeroOs, Long servicoId);

    OrdemServicoEntity finalizarServico(String numeroOs, Long servicoId);

    OrdemServicoEntity entregar(String numeroOs);

    List<AcompanhamentoOrdemServicoResponse> listarAcompanhamentoCliente(String username);

    List<OrdemServicoEntity> listar();

    OrcamentoEntity buscarOrcamentoAtual(String numeroOs);
}
