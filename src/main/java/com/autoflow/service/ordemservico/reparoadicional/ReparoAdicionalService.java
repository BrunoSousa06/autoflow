package com.autoflow.service.ordemservico.reparoadicional;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.service.ordemservico.reparoadicional.impl.CriarReparoAdicionalResult;
import jakarta.transaction.Transactional;

import java.util.List;

public interface ReparoAdicionalService {
    @Transactional
    CriarReparoAdicionalResult criar(
            Long ordemServicoId,
            String emailMecanico,
            List<ServicoSolicitadoEntity> servicos
    );

    @Transactional
    OrdemServicoEntity aprovar(Long reparoAdicionalId);

    @Transactional
    ReparoAdicionalEntity recusar(Long reparoAdicionalId, String motivo);

    void aprovarPorOrcamentoId(Long id);
}
