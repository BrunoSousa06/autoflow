package com.autoflow.service.ordemservico.reparoadicional.impl;

import com.autoflow.application.usecases.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
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
import com.autoflow.service.pecainsumo.PecaInsumoService;
import com.autoflow.service.servico.ServicoService;
import com.autoflow.service.usuario.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


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
    private final ServicoService servicoService;
    private final ConsultarDisponibilidadeEstoqueUseCase consultarDisponibilidadeEstoqueUseCase;

    @Transactional
    @Override
    public CriarReparoAdicionalResult criar(
            String numeroOs,
            String emailMecanico,
            List<ServicoSolicitadoEntity> servicos
    ) {
        OrdemServicoEntity ordemServico =
                ordemServicoService.buscaOrdemServicoPorNumeroOs(numeroOs);

        if (ordemServico.getStatus() == StatusOrdemServico.FINALIZADA
                || ordemServico.getStatus() == StatusOrdemServico.ENTREGUE) {
            throw new IllegalStateException("Não é possível registrar reparo adicional em uma OS finalizada.");
        }

        Set<Long> servicosJaNaOs = ordemServico.getServicosSolicitados().stream()
                .map(ServicoSolicitadoEntity::getServicoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        servicos.forEach(s -> {
            if (s.getServicoId() != null && servicosJaNaOs.contains(s.getServicoId())) {
                throw new IllegalArgumentException(
                        "Serviço já incluído na ordem de serviço e não pode ser adicionado novamente: ID " + s.getServicoId());
            }
        });

        Long mecanicoId =
                usuarioService.buscarPorEmail(emailMecanico).getId();

        List<ServicoSolicitadoEntity> servicosComDados = preencherDadosDosServicos(servicos);

        ReparoAdicionalEntity reparo =
                ReparoAdicionalEntity.criar(ordemServico.getNumeroOs(), mecanicoId, servicosComDados);
        reparo.setOrdemServicoId(ordemServico.getId());

        ReparoAdicionalEntity reparoSalvo = reparoAdicionalRepository.save(reparo);

        int versao = orcamentoVersioningService.proximaVersaoPrincipalNumeroOs(numeroOs);
        OrcamentoEntity orcamento =
                orcamentoFactory.criarAdicionalDisponivel(
                        ordemServico,
                        reparoSalvo,
                        versao,
                        LocalDateTime.now()
                );

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
                ordemServicoService.buscaOrdemServicoPorNumeroOs(reparo.getNumeroOs());

        reparo.aprovar();

        List<ServicoSolicitadoEntity> servicosParaOs =
                reparo.getServicos()
                        .stream()
                        .map(servico -> copiarServicoParaOrdemServico(servico, ordemServico))
                        .toList();

        ordemServico.adicionarServicosSolicitados(servicosParaOs);

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

    @Override
    @Transactional
    public void aprovarSeExistirPorOrcamentoId(Long orcamentoId) {
        reparoAdicionalRepository.findByOrcamentoId(orcamentoId)
                .ifPresent( reparo -> aprovar(reparo.getId()));
    }

    @Override
    @Transactional
    public void recusarSeExistirPorOrcamentoId(Long orcamentoId, String motivo) {
        reparoAdicionalRepository.findByOrcamentoId(orcamentoId)
                .ifPresent( reparo -> recusar(reparo.getId(), motivo));
    }

    @Override
    public boolean existePorOrcamentoId(Long orcamentoId) {
        return reparoAdicionalRepository.findByOrcamentoId(orcamentoId).isPresent();
    }

    private ReparoAdicionalEntity buscarPorId(Long id) {
        return reparoAdicionalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));
    }

    private List<ServicoSolicitadoEntity> preencherDadosDosServicos(List<ServicoSolicitadoEntity> servicos) {
        if (servicos == null || servicos.isEmpty()) {
            throw new IllegalArgumentException("Reparo adicional deve ter ao menos um servico.");
        }

        return servicos.stream()
                .map(this::preencherDadosDoServico)
                .toList();
    }

    private ServicoSolicitadoEntity preencherDadosDoServico(ServicoSolicitadoEntity servicoSolicitado) {
        ServicoEntity servico = servicoService.buscarEntityPorId(servicoSolicitado.getServicoId());

        ServicoSolicitadoEntity servicoReparo = new ServicoSolicitadoEntity();
        servicoReparo.setServicoId(servico.getId());
        servicoReparo.setNome(servico.getNome());
        servicoReparo.setValor(servico.getValor());
        servicoReparo.setStatus(StatusServicoOs.AGUARDANDO);
        servicoReparo.registrarItensNecessarios(
                verificarItensNecessarios(servicoSolicitado.getItensNecessarios())
        );

        return servicoReparo;
    }

    private List<ItemNecessarioEntity> verificarItensNecessarios(List<ItemNecessarioEntity> itensNecessarios) {
        if (itensNecessarios == null || itensNecessarios.isEmpty()) {
            throw new IllegalArgumentException("Servico do reparo adicional deve ter ao menos um item necessario.");
        }

        return consultarDisponibilidadeEstoqueUseCase.execute(itensNecessarios);
    }

    @Override
    public List<ItemNecessarioEntity> buscaItensNecessarios(List<ItemNecessarioEntity> itensNecessarios, PecaInsumoService pecaInsumoService) {
        return consultarDisponibilidadeEstoqueUseCase.execute(itensNecessarios);
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
        destino.registrarItensNecessarios(copiarItens(origem.getItensNecessarios()));
        return destino;
    }

    private List<ItemNecessarioEntity> copiarItens(List<ItemNecessarioEntity> itens) {
        return itens.stream()
                .map(item -> ItemNecessarioEntity.criar(
                        item.getPecaInsumoId(),
                        item.getNome(),
                        item.getTipo(),
                        item.getValorUnitario(),
                        item.getQuantidade(),
                        item.getStatus(),
                        new SituacaoEstoque(
                                item.getQuantidadeDisponivel(),
                                item.getMotivoPendencia()
                        )
                ))
                .toList();
    }

}
