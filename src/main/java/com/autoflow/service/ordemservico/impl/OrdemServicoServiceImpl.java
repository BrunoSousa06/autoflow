package com.autoflow.service.ordemservico.impl;

import com.autoflow.controller.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.repository.cliente.ClienteRepository;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.repository.ordemservico.historico.HistoricoStatusOsRepository;
import com.autoflow.service.cliente.ClienteService;
import com.autoflow.service.orcamento.OrcamentoFactory;
import com.autoflow.service.orcamento.OrcamentoNotificacaoService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static com.autoflow.controller.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse.mensagemParaCliente;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrdemServicoServiceImpl implements OrdemServicoService {
    @Autowired
    private final OrdemServicoRepository ordemServicoRepository;

    @Autowired
    private final VeiculoService veiculoService;
    @Autowired
    private final ServicoService servicoService;
    @Autowired
    private final UsuarioService usuarioService;
    @Autowired
    private final PecaInsumoService pecaInsumoService;
    @Autowired
    private final OrdemServicoAccessPolicy ordemServicoAccessPolicy;
    @Autowired
    private final OrcamentoFactory orcamentoFactoryImpl;
    @Autowired
    private final OrcamentoVersioningService orcamentoVersioningServiceImpl;
    @Autowired
    private final OrcamentoRepository orcamentoRepository;
    @Autowired
    private final OrcamentoPublicacaoService orcamentoPublicacaoServiceImpl;
    @Autowired
    private final ClienteService clienteService;
    @Autowired
    private final ClienteRepository clienteRepository;
    @Autowired
    private final HistoricoStatusOsRepository historicoStatusOsRepository;
    @Autowired
    private final OrcamentoNotificacaoService orcamentoNotificacaoService;

    public OrdemServicoEntity criar(String cpfCnpj, VeiculoOrdemServicoRequest veiculoRequest, List<ServicoSolicitadoEntity> servicosSolicitados) {
        ClienteEntity cliente = clienteService.buscarPorCpfCnpj(cpfCnpj);

        VeiculoEntity veiculo = veiculoService.buscarOuCadastrarPorPlacaParaCliente(
                cliente,
                veiculoRequest
        );
        validarServicosSolicitados(servicosSolicitados);

        OrdemServicoEntity ordemServico = OrdemServicoEntity.criar(cliente, veiculo);

        List<ServicoSolicitadoEntity> servicosComDados = servicosSolicitados.stream()
                .map(servico -> preencherDadosDoServico(ordemServico, servico))
                .toList();

        ordemServico.adicionarServicos(servicosComDados);
        return salvarOs(ordemServico);
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
        ordemServico.iniciarDiagnostico();
        return salvarOs(ordemServico);
    }

    private OrdemServicoEntity salvarOs(OrdemServicoEntity ordemServico) {
        OrdemServicoEntity ordemServicoSalva = ordemServicoRepository.save(ordemServico);
        registrarHistorico(ordemServicoSalva);
        return ordemServicoSalva;
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
        try {
            orcamentoNotificacaoService.enviarLinkOrcamentoParaCliente(
                    orcamentoSalvo,
                    ordemServico,
                    publicUrl
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        OrdemServicoEntity ordemServicoSalvo = salvarOs(ordemServico);

        return new FinalizarDiagnosticoResult(ordemServicoSalvo, orcamentoSalvo.getId(), publicUrl);
    }

    private List<ServicoSolicitadoEntity> preencherDadosDosServicos(OrdemServicoEntity ordemServico, List<ServicoSolicitadoEntity> servicos) {
        validarServicosSolicitados(servicos);

        return servicos.stream().map(servico -> preencherDadosDoServico(ordemServico, servico)).toList();
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
        StatusOrdemServico statusAnterior = ordemServico.getStatus();

        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoOsId);

        BaixaEstoqueResult baixaEstoqueResult =
                pecaInsumoService.verificarDisponibilidadeEBaixar(servico.getItensNecessarios());

        servico.iniciar(baixaEstoqueResult.itensAtualizados());

        ordemServico.iniciarExecucaoSeNecessario();
        if (!statusAnterior.equals(ordemServico.getStatus())) {
            return salvarOs(ordemServico);
        }

        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    @Override
    public OrdemServicoEntity finalizarServico(Long ordemServicoId, Long servicoOsId) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);

        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoOsId);

        servico.finalizar();
        ordemServico.atualizarUltimaAtualizacao();
        ordemServico.finalizarSeTodosServicosFinalizados();
        OrdemServicoEntity salva = ordemServicoRepository.save(ordemServico);

        if (StatusOrdemServico.FINALIZADA.equals(salva.getStatus())) {
            registrarHistorico(salva);
        }
        return salva;
    }

    @Override
    public OrdemServicoEntity entregar(Long ordemServicoId) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);
        ordemServico.entregar();
        OrdemServicoEntity salva = ordemServicoRepository.save(ordemServico);
        registrarHistorico(salva);
        return salva;
    }

    public List<AcompanhamentoOrdemServicoResponse> listarAcompanhamentoCliente(String emailCliente) {
        ClienteEntity cliente = clienteRepository.findByUsuarioEmail(emailCliente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente autenticado nao encontrado."
                ));

        return ordemServicoRepository.findByCliente_IdOrderByDataAberturaDesc(cliente.getId())
                .stream()
                .map(os -> {
                    OrcamentoEntity orcamentoAtual = buscarOrcamentoAtual(os.getId());
                    List<HistoricoStatusOsEntity> historico =
                            historicoStatusOsRepository.findByOrdemServicoIdOrderByRegistradoEmAsc(os.getId());

                    return AcompanhamentoOrdemServicoResponse.from(os, orcamentoAtual, historico);
                })
                .toList();
    }

    @Override
    public List<OrdemServicoEntity> listar() {
        return ordemServicoRepository.findAllByOrderByDataAberturaDesc();
    }

    @Override
    public OrcamentoEntity buscarOrcamentoAtual(Long ordemServicoId) {
        return orcamentoRepository.findByOrdemServicoIdAndStatus(ordemServicoId, StatusOrcamento.DISPONIVEL)
                .or(() -> orcamentoRepository.findTopByOrdemServicoIdOrderByVersaoDesc(ordemServicoId))
                .orElse(null);
    }

    private List<ItemNecessarioEntity> verificaItensNecessarios(List<ItemNecessarioEntity> itensNecessarios) {
        return itensNecessarios.stream()
                .map(itemNecessario -> {
                    PecaInsumoEntity itemEstoque = pecaInsumoService.buscarEntityPorId(itemNecessario.getPecaInsumoId());

                    boolean disponivel = itemEstoque.getQuantidade() >= itemNecessario.getQuantidade();

                    StatusItemNecessario status = disponivel
                            ? StatusItemNecessario.DISPONIVEL
                            : StatusItemNecessario.PENDENTE;

                    MotivoPendenciaItem motivoPendencia = disponivel
                            ? null
                            : MotivoPendenciaItem.ESTOQUE_INSUFICIENTE;

                    return ItemNecessarioEntity.criar(
                            itemEstoque.getId(),
                            itemEstoque.getNome(),
                            itemEstoque.getTipo(),
                            itemEstoque.getValor(),
                            itemNecessario.getQuantidade(),
                            status,
                            itemEstoque.getQuantidade(),
                            motivoPendencia
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

    private void registrarHistorico(OrdemServicoEntity os) {
        historicoStatusOsRepository.save(
                HistoricoStatusOsEntity.criar(
                        os.getId(),
                        os.getStatus(),
                        mensagemParaCliente(os.getStatus())
                )
        );
    }
}
