package com.autoflow.service.ordemservico;

import com.autoflow.presentation.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.service.ordemservico.dto.OrdemServicoCriada;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import com.autoflow.service.ordemservico.dto.OrdemServicoFiltro;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrdemServicoService {
    OrdemServicoCriada criar(
            String cpfCnpj,
            VeiculoOrdemServicoRequest veiculo,
            List<ServicoSolicitadoEntity> servicosSolicitados
    );
    @Transactional
    OrdemServicoEntity incluirServicos(String numeroOs, List<ServicoSolicitadoEntity> servicos, String emailUsuarioLogado);

    OrdemServicoEntity atribuirMecanico(String numeroOs, Long mecanicoId, String email);

    OrdemServicoEntity iniciarDiagnostico(String numeroOs, String emailUsuarioLogado);

    OrdemServicoEntity registrarItemNecessario(String numeroOs, Long servicoOsId, String emailUsuarioLogado, List<ItemNecessarioEntity> itensNecessarios);

    OrdemServicoEntity registrarLaudo(String numeroOs, String emailUsuarioLogado, String laudo);

    @Transactional
    FinalizarDiagnosticoResult finalizarDiagnostico(String numeroOs, String emailUsuarioLogado);

    OrdemServicoEntity buscaOrdemServicoPorNumeroOs(String numeroOs);

    @Transactional
    OrdemServicoEntity iniciarServico(String numeroOs, Long servicoId);

    OrdemServicoEntity finalizarServico(String numeroOs, Long servicoId);

    OrdemServicoEntity entregar(String numeroOs);

    List<AcompanhamentoOrdemServicoResponse> listarAcompanhamentoCliente(String username);

    Page<OrdemServicoEntity> listar(OrdemServicoFiltro filtro, Pageable pageable, String emailUsuarioLogado);

    OrcamentoEntity buscarOrcamentoAtual(String numeroOs);
}
