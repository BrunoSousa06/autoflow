package com.autoflow.service.ordemservico;

import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import jakarta.transaction.Transactional;

import java.util.List;

public interface OrdemServicoService {
    OrdemServicoEntity criar(Long veiculoId, List<ServicoSolicitadoEntity> servicosSolicitados);

    @Transactional
    OrdemServicoEntity incluirServicos(Long ordemServicoId, List<ServicoSolicitadoEntity> servicos);

    OrdemServicoEntity atribuirMecanico(Long ordemServicoId, Long mecanicoId);

    OrdemServicoEntity iniciarDiagnostico(Long ordemServicoId, String emailUsuarioLogado);

    OrdemServicoEntity registrarItemNecessario(Long ordemServicoId, Long servicoOsId, String emailUsuarioLogado, List<ItemNecessarioEntity> itensNecessarios);

    OrdemServicoEntity registrarLaudo(Long ordemServicoId, String emailUsuarioLogado, String laudo);

    @Transactional
    FinalizarDiagnosticoResult finalizarDiagnostico(Long ordemServicoId, String emailUsuarioLogado);

    OrdemServicoEntity buscaOrdemServicoPorId(Long ordemServicoId);

    @Transactional
    OrdemServicoEntity iniciarServico(Long ordemServicoId, Long servicoOsId);

    OrdemServicoEntity finalizarServico(Long ordemServicoId, Long servicoOsId);

    OrdemServicoEntity entregar(Long ordemServicoId);
}
