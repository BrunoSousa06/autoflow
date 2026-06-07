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
    OrdemServicoEntity incluirServicos(Long ordemServicoId, List<ServicoSolicitadoEntity> servicos);

    OrdemServicoEntity atribuirMecanico(Long ordemServicoId, Long mecanicoId);

    OrdemServicoEntity iniciarDiagnostico(Long ordemServicoId, String emailUsuarioLogado);

    OrdemServicoEntity registrarItemNecessario(Long ordemServicoId, Long servicoId, String emailUsuarioLogado, List<ItemNecessarioEntity> itensNecessarios);

    OrdemServicoEntity registrarLaudo(Long ordemServicoId, String emailUsuarioLogado, String laudo);

    @Transactional
    FinalizarDiagnosticoResult finalizarDiagnostico(Long ordemServicoId, String emailUsuarioLogado);

    OrdemServicoEntity buscaOrdemServicoPorId(Long ordemServicoId);

    @Transactional
    OrdemServicoEntity iniciarServico(Long ordemServicoId, Long servicoId);

    OrdemServicoEntity finalizarServico(Long ordemServicoId, Long servicoId);

    OrdemServicoEntity entregar(Long ordemServicoId);

    List<AcompanhamentoOrdemServicoResponse> listarAcompanhamentoCliente(String username);

    List<OrdemServicoEntity> listar();

    OrcamentoEntity buscarOrcamentoAtual(Long ordemServicoId);
}
