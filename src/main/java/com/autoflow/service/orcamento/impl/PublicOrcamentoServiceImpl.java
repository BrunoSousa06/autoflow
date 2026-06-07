package com.autoflow.service.orcamento.impl;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import com.autoflow.service.orcamento.PublicOrcamentoService;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import jakarta.transaction.Transactional;
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
    private final ReparoAdicionalService reparoAdicionalService;

    @Override
    public OrcamentoEntity consultar(Long orcamentoId, String token) {
        OrcamentoEntity orcamento = getOrcamento(orcamentoId);
        validarToken(orcamento, token);
        return orcamento;
    }

    @Override
    @Transactional
    public OrcamentoEntity aprovar(Long orcamentoId, String token, String nome) {
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

        OrcamentoEntity orcamentoSalvo = orcamentoRepository.save(orcamento);

        if (reparoAdicionalService.existePorOrcamentoId(orcamento.getId())) {
            reparoAdicionalService.aprovarSeExistirPorOrcamentoId(orcamento.getId());
            return orcamentoSalvo;
        }

        OrdemServicoEntity ordemServico = ordemServicoRepository.findById(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OS nao encontrada"));
        ordemServico.iniciarExecucao();
        ordemServicoRepository.save(ordemServico);

        return orcamentoRepository.save(orcamento);
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

        OrcamentoEntity orcamentoSalvo = orcamentoRepository.save(orcamento);

        if(reparoAdicionalService.existePorOrcamentoId(orcamento.getId())) {
            reparoAdicionalService.recusarSeExistirPorOrcamentoId(orcamento.getId(), motivo);
            return orcamentoSalvo;
        }

        OrdemServicoEntity ordemServico = ordemServicoRepository.findById(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OS nao encontrada"));
        ordemServico.finalizarPorOrcamentoRecusado();
        ordemServicoRepository.save(ordemServico);
        return orcamento;
    }

    @Override
    public OrcamentoEntity consultarPdf(Long orcamentoId, String token) {
        return null;
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
