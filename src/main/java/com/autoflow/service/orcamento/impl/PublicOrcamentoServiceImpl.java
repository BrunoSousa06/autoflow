package com.autoflow.service.orcamento.impl;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import com.autoflow.service.orcamento.PublicOrcamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PublicOrcamentoServiceImpl implements PublicOrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoPublicacaoService publicacaoService;

    @Override
    public OrcamentoEntity consultar(Long orcamentoId, String token) {
        OrcamentoEntity orcamento = getOrcamento(orcamentoId);
        validarToken(orcamento, token);
        return orcamento;
    }

    @Override
    public OrcamentoEntity aceitar(Long orcamentoId, String token, String nome) {
        OrcamentoEntity orcamento = getOrcamento(orcamentoId);
        validarToken(orcamento, token);
        if (orcamento.getStatus() == StatusOrcamento.APROVADO || orcamento.getStatus() == StatusOrcamento.REPROVADO) {
            return orcamento;
        }
        if (orcamento.getStatus() != StatusOrcamento.DISPONIVEL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento nao esta disponível");
        }

        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome obrigatório");
        }
        orcamento.setStatus(StatusOrcamento.APROVADO);
        orcamento.setAssinaturaNome(nome.trim());
        orcamento.setAprovadoEm(LocalDateTime.now());
        orcamentoRepository.save(orcamento);

        OrdemServicoEntity ordemServico = ordemServicoRepository.findById(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OS não encontrada"));

        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ordemServicoRepository.save(ordemServico);
        return orcamento;
    }

    @Override
    public OrcamentoEntity recusar(Long orcamentoId, String token, String motivo) {
        OrcamentoEntity orcamento = getOrcamento(orcamentoId);

        validarToken(orcamento, token);

        if (orcamento.getStatus() == StatusOrcamento.APROVADO ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento já aprovado, não é possivel recusar");
        }
        if(orcamento.getStatus() == StatusOrcamento.REPROVADO){
            return orcamento;
        }

        if (orcamento.getStatus() != StatusOrcamento.DISPONIVEL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento não esta disponivel");
        }

        orcamento.setStatus(StatusOrcamento.REPROVADO);
        orcamento.setReprovadoEm(LocalDateTime.now());
        if(motivo != null) orcamento.setRecusaMotivo(motivo);
        orcamentoRepository.save(orcamento);

        OrdemServicoEntity ordemServico = ordemServicoRepository.findById(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OS nao encontrada"));
        ordemServico.setStatus(StatusOrdemServico.FINALIZADA);
        ordemServicoRepository.save(ordemServico);
        return orcamento;
    }

    private OrcamentoEntity getOrcamento(Long orcamentoId) {
        return orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado"));
    }

    private void validarToken(OrcamentoEntity orcamento, String token) {
        if(!publicacaoService.validarToken(orcamento, token)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido");
        }
    }
}
