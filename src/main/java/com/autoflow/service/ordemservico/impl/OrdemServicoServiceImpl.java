package com.autoflow.service.ordemservico.impl;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.service.orcamento.OrcamentoFactory;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import com.autoflow.service.ordemservico.OrdemServicoService;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import com.autoflow.service.pecainsumo.BaixaEstoqueResult;
import com.autoflow.service.pecainsumo.PecaInsumoService;
import com.autoflow.service.servico.ServicoService;
import com.autoflow.service.usuario.UsuarioService;
import com.autoflow.service.veiculo.VeiculoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrdemServicoServiceImpl implements OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;

    private final VeiculoService veiculoService;
    private final ServicoService servicoService;
    private final UsuarioService usuarioService;
    private final PecaInsumoService pecaInsumoService;
    private final OrdemServicoAccessPolicy ordemServicoAccessPolicy;
    private final OrcamentoFactory orcamentoFactoryImpl;
    private final OrcamentoVersioningService orcamentoVersioningServiceImpl;
    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoPublicacaoService orcamentoPublicacaoServiceImpl;

    public OrdemServicoEntity criar(Long veiculoId, List<ServicoSolicitadoEntity> servicosSolicitados) {
        VeiculoEntity veiculo = veiculoService.buscarPorId(veiculoId);
        validarServicosSolicitados(servicosSolicitados);

        OrdemServicoEntity ordemServico = OrdemServicoEntity.criar(veiculo);

        List<ServicoSolicitadoEntity> servicosComDados = servicosSolicitados.stream()
                .map(servico -> preencherDadosDoServico(ordemServico, servico))
                .toList();

        ordemServico.adicionarServicos(servicosComDados);

        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    @Override
    public OrdemServicoEntity incluirServicos(Long ordemServicoId, List<ServicoSolicitadoEntity> servicos) {

        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);

        List<ServicoSolicitadoEntity> servicosComDados =
                preencherDadosDosServicos(ordemServico, servicos);

        ordemServico.adicionarServicos(servicosComDados);

        return ordemServicoRepository.save(ordemServico);
    }
    @Override
    public OrdemServicoEntity atribuirMecanico(Long ordemServicoId, Long mecanicoId) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);

        UsuarioEntity mecanico = usuarioService.buscarMecanicoPorId(mecanicoId);

        if (ordemServico.getDiagnostico() == null) {
            ordemServico.setDiagnostico(new DiagnosticoEntity());
        }

        ordemServico.getDiagnostico().setMecanico(mecanico);

        return ordemServicoRepository.save(ordemServico);
    }
    @Override
    public OrdemServicoEntity iniciarDiagnostico(Long ordemServicoId, String emailUsuarioLogado) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);
        UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);
        if (!RoleEnum.ADMIN.equals(usuarioLogado.getRole())) {
            ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        }
        ordemServico.getDiagnostico().setIniciadoEm(LocalDateTime.now());
        ordemServico.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        return ordemServicoRepository.save(ordemServico);
    }

    public OrdemServicoEntity registrarItemNecessario(
            Long ordemServicoId,
            Long servicoOsId,
            String emailUsuarioLogado,
            List<ItemNecessarioEntity> itensNecessarios
    ) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);

        UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);

        if (!RoleEnum.ADMIN.equals(usuarioLogado.getRole())) {
            ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        }

        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoOsId);

        List<ItemNecessarioEntity> itensComDados = verificaItensNecessarios(itensNecessarios);

        servico.registrarItensNecessarios(itensComDados);

        return ordemServicoRepository.save(ordemServico);
    }


    @Override
    public OrdemServicoEntity registrarLaudo(Long ordemServicoId, String emailUsuarioLogado, String laudo){
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);
        UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);
        ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        ordemServico.registrarLaudo(laudo);
        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    @Override
    public FinalizarDiagnosticoResult finalizarDiagnostico(Long ordemServicoId, String emailUsuarioLogado){
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);
        UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);

        if(!RoleEnum.ADMIN.equals(usuarioLogado.getRole())){
            ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        }
        ordemServico.finalizarDiagnostico();
        int versao = orcamentoVersioningServiceImpl.proximaVersaoPrincipal(ordemServicoId);
        OrcamentoEntity orcamento = orcamentoFactoryImpl.criarPrincipalDisponivel(ordemServico, versao, LocalDateTime.now());

        ordemServico.aguardarAprovacao();

        OrcamentoEntity orcamentoSalvo = orcamentoRepository.save(orcamento);

        String publicUrl = orcamentoPublicacaoServiceImpl.publicar(orcamentoSalvo.getId()).url();

        OrdemServicoEntity ordemServicoSalvo = ordemServicoRepository.save(ordemServico);

        return new FinalizarDiagnosticoResult(ordemServicoSalvo, orcamentoSalvo.getId(), publicUrl);
    }

    private List<ServicoSolicitadoEntity> preencherDadosDosServicos(
            OrdemServicoEntity ordemServico,
            List<ServicoSolicitadoEntity> servicos
    ) {
        validarServicosSolicitados(servicos);

        return servicos.stream()
                .map(servico -> preencherDadosDoServico(ordemServico, servico))
                .toList();
    }

    @Override
    public OrdemServicoEntity buscaOrdemServicoPorId(Long ordemServicoId) {
        return ordemServicoRepository.findById(ordemServicoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
    }

    @Transactional
    @Override
    public OrdemServicoEntity iniciarServico(Long ordemServicoId, Long servicoOsId) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);

        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoOsId);

        BaixaEstoqueResult baixaEstoqueResult =
                pecaInsumoService.verificarDisponibilidadeEBaixar(servico.getItensNecessarios());

        servico.iniciar(baixaEstoqueResult.itensAtualizados());

        ordemServico.iniciarExecucaoSeNecessario();

        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    @Override
    public OrdemServicoEntity finalizarServico(Long ordemServicoId, Long servicoOsId) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);

        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoOsId);

        servico.finalizar();

        ordemServico.finalizarSeTodosServicosFinalizados();

        return ordemServicoRepository.save(ordemServico);
    }


    private List<ItemNecessarioEntity> verificaItensNecessarios(List<ItemNecessarioEntity> itensNecessarios) {
        return itensNecessarios.stream()
                .map(itemNecessario -> {
                    PecaInsumoEntity itemEstoque = pecaInsumoService.buscarEntityPorId(itemNecessario.getPecaInsumoId());
                    StatusItemNecessario status = itemEstoque.getQuantidade() >= itemNecessario.getQuantidade() ?
                            StatusItemNecessario.DISPONIVEL : StatusItemNecessario.PENDENTE;

                    return ItemNecessarioEntity.criar(
                            itemEstoque.getId(),
                            itemEstoque.getNome(),
                            itemEstoque.getTipo(),
                            itemEstoque.getValor(),
                            itemNecessario.getQuantidade(),
                            status
                    );
                }).toList();
    }

    private ServicoSolicitadoEntity preencherDadosDoServico(
            OrdemServicoEntity ordemServico,
            ServicoSolicitadoEntity servicoSolicitado
    ) {
        ServicoEntity servico = servicoService.buscarEntityPorId(servicoSolicitado.getServicoId());

        ServicoSolicitadoEntity servicoOs = new ServicoSolicitadoEntity();
        servicoOs.setServicoId(servico.getId());
        servicoOs.setNome(servico.getNome());
        servicoOs.setValor(servico.getValor());
        servicoOs.setStatus(StatusServicoOs.AGUARDANDO);
        servicoOs.setOrdemServico(ordemServico);

        return servicoOs;
    }

    private static void validarServicosSolicitados(List<ServicoSolicitadoEntity> servicos) {
        if (servicos == null || servicos.isEmpty()) {
            throw new IllegalArgumentException("A ordem de servico deve ter ao menos um servico solicitado.");
        }
    }
}
