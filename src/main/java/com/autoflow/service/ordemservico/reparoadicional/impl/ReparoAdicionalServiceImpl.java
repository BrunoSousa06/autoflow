package com.autoflow.service.ordemservico.reparoadicional.impl;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.repository.ordemservico.reparoadicional.ReparoAdicionalRepository;
import com.autoflow.service.orcamento.OrcamentoFactory;
import com.autoflow.service.orcamento.OrcamentoNotificacaoService;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import com.autoflow.service.orcamento.dto.PublicacaoOrcamentoResult;
import com.autoflow.service.ordemservico.OrdemServicoService;
import com.autoflow.service.ordemservico.reparoadicional.ReparoAdicionalService;
import com.autoflow.service.usuario.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class ReparoAdicionalServiceImpl implements ReparoAdicionalService {

    private final ReparoAdicionalRepository reparoAdicionalRepository;
    private final OrdemServicoService ordemServicoService;
    private final UsuarioService usuarioService;
    private final OrcamentoFactory orcamentoFactory;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoVersioningService orcamentoVersioningService;
    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoPublicacaoService orcamentoPublicacaoService;
    private final OrcamentoNotificacaoService orcamentoNotificacaoService;

    @Transactional
    @Override
    public CriarReparoAdicionalResult criar(
            Long ordemServicoId,
            String emailMecanico,
            List<ServicoSolicitadoEntity> servicos
    ) {
        OrdemServicoEntity ordemServico =
                ordemServicoService.buscaOrdemServicoPorId(ordemServicoId);

        Long mecanicoId =
                usuarioService.buscarPorEmail(emailMecanico).getId();

        ReparoAdicionalEntity reparo =
                ReparoAdicionalEntity.criar(ordemServico.getId(), mecanicoId, servicos);

        ReparoAdicionalEntity reparoSalvo = reparoAdicionalRepository.save(reparo);

        int versao = orcamentoVersioningService.proximaVersaoPrincipal(ordemServicoId);
        OrcamentoEntity orcamento =
                orcamentoFactory.criarAdicionalDisponivel(ordemServico, reparoSalvo, versao, LocalDateTime.now());

        OrcamentoEntity orcamentoSalvo = orcamentoRepository.save(orcamento);
        reparoSalvo.setOrcamentoId(orcamentoSalvo.getId());

        PublicacaoOrcamentoResult publicacao =
                orcamentoPublicacaoService.publicar(orcamentoSalvo.getId());
        try {
            orcamentoNotificacaoService.enviarLinkOrcamentoParaCliente(
                    orcamentoSalvo,
                    ordemServico,
                    publicacao.url()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        reparoAdicionalRepository.save(reparoSalvo);

        return new CriarReparoAdicionalResult(
                reparoSalvo.getId(),
                orcamentoSalvo.getId(),
                publicacao.url()
        );
    }

    @Transactional
    @Override
    public OrdemServicoEntity aprovar(Long reparoAdicionalId) {
        ReparoAdicionalEntity reparo = buscarPorId(reparoAdicionalId);

        OrdemServicoEntity ordemServico =
                ordemServicoService.buscaOrdemServicoPorId(reparo.getOrdemServicoId());

        reparo.aprovar();

        List<ServicoSolicitadoEntity> servicosParaOs =
                reparo.getServicos()
                        .stream()
                        .map(servico -> copiarServicoParaOrdemServico(servico, ordemServico))
                        .toList();

        ordemServico.adicionarServicos(servicosParaOs);

        reparoAdicionalRepository.save(reparo);

        ordemServicoRepository.save(ordemServico);

        return ordemServico;
    }

    @Transactional
    @Override
    public ReparoAdicionalEntity recusar(Long reparoAdicionalId, String motivo) {
        ReparoAdicionalEntity reparo = buscarPorId(reparoAdicionalId);
        reparo.recusar(motivo);
        return reparoAdicionalRepository.save(reparo);
    }

    @Transactional
    public void aprovarPorOrcamentoId(Long orcamentoId) {
        ReparoAdicionalEntity reparo =
                reparoAdicionalRepository.findByOrcamentoId(orcamentoId)
                        .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));

        aprovar(reparo.getId());
    }

    private ReparoAdicionalEntity buscarPorId(Long id) {
        return reparoAdicionalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));
    }

    private ServicoSolicitadoEntity copiarServicoParaOrdemServico(
            ServicoSolicitadoEntity origem,
            OrdemServicoEntity ordemServico
    ) {
        ServicoSolicitadoEntity destino = new ServicoSolicitadoEntity();

        destino.setServicoId(origem.getServicoId());
        destino.setNome(origem.getNome());
        destino.setValor(origem.getValor());
        destino.setStatus(origem.getStatus());
        destino.setOrdemServico(ordemServico);
        destino.setItensNecessarios(origem.getItensNecessarios());

        return destino;
    }

}