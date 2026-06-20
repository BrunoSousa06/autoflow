package com.autoflow.service.ordemservico.reparoadicional;

import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.service.ordemservico.reparoadicional.impl.CriarReparoAdicionalResult;
import com.autoflow.service.pecainsumo.PecaInsumoService;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ReparoAdicionalService {
    @Transactional
    CriarReparoAdicionalResult criar(
            String numeroOs,
            String emailMecanico,
            List<ServicoSolicitadoEntity> servicos
    );

    @Transactional
    OrdemServicoEntity aprovar(Long reparoAdicionalId);

    @Transactional
    ReparoAdicionalEntity recusar(Long reparoAdicionalId, String motivo);

    void aprovarPorOrcamentoId(Long id);

    void aprovarSeExistirPorOrcamentoId(Long orcamentoId);

    void recusarSeExistirPorOrcamentoId(Long orcamentoId, String motivo);

    boolean existePorOrcamentoId(Long orcamentoId);

    List<ItemNecessarioEntity> buscaItensNecessarios(List<ItemNecessarioEntity> itensNecessarios, PecaInsumoService pecaInsumoService);
}
