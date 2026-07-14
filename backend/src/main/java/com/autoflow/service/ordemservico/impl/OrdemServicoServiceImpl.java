package com.autoflow.service.ordemservico.impl;

import com.autoflow.application.usecases.cliente.BuscarClientePorCpfCnpjUseCase;
import com.autoflow.application.usecases.veiculo.BuscarOuCadastrarVeiculoUseCase;
import com.autoflow.controller.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.controller.ordemservico.response.TempoMedioOrdemServicoResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.repository.ClienteRepository;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.repository.ordemservico.TempoMedioOrdemServicoProjection;
import com.autoflow.repository.ordemservico.historico.HistoricoStatusOsRepository;
import com.autoflow.service.orcamento.OrcamentoFactory;
import com.autoflow.service.orcamento.OrcamentoNotificacaoService;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import com.autoflow.repository.ordemservico.OrdemServicoSpecifications;
import com.autoflow.service.ordemservico.OrdemServicoService;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import com.autoflow.service.ordemservico.dto.OrdemServicoFiltro;
import com.autoflow.service.pecainsumo.BaixaEstoqueResult;
import com.autoflow.service.pecainsumo.PecaInsumoService;
import com.autoflow.service.servico.ServicoService;
import com.autoflow.service.usuario.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final OrdemServicoRepository ordemServicoRepository;
    private final BuscarOuCadastrarVeiculoUseCase buscarOuCadastrarVeiculoUseCase;
    private final ServicoService servicoService;
    private final UsuarioService usuarioService;
    private final PecaInsumoService pecaInsumoService;
    private final OrdemServicoAccessPolicy ordemServicoAccessPolicy;
    private final OrcamentoFactory orcamentoFactoryImpl;
    private final OrcamentoVersioningService orcamentoVersioningServiceImpl;
    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoPublicacaoService orcamentoPublicacaoServiceImpl;
    private final ClienteRepository clienteRepository;
    private final HistoricoStatusOsRepository historicoStatusOsRepository;
    private final OrcamentoNotificacaoService orcamentoNotificacaoService;
    private final BuscarClientePorCpfCnpjUseCase buscarClientePorCpfCnpjUseCase;

    @Autowired
    public OrdemServicoServiceImpl(OrdemServicoRepository ordemServicoRepository, BuscarOuCadastrarVeiculoUseCase buscarOuCadastrarVeiculoUseCase, ServicoService servicoService, UsuarioService usuarioService, HistoricoStatusOsRepository historicoStatusOsRepository, PecaInsumoService pecaInsumoService, OrdemServicoAccessPolicy ordemServicoAccessPolicy, OrcamentoFactory orcamentoFactoryImpl, OrcamentoNotificacaoService orcamentoNotificacaoService, OrcamentoVersioningService orcamentoVersioningServiceImpl, ClienteRepository clienteRepository, OrcamentoRepository orcamentoRepository, OrcamentoPublicacaoService orcamentoPublicacaoServiceImpl, BuscarClientePorCpfCnpjUseCase buscarClientePorCpfCnpjUseCase) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.buscarOuCadastrarVeiculoUseCase = buscarOuCadastrarVeiculoUseCase;
        this.servicoService = servicoService;
        this.usuarioService = usuarioService;
        this.historicoStatusOsRepository = historicoStatusOsRepository;
        this.pecaInsumoService = pecaInsumoService;
        this.ordemServicoAccessPolicy = ordemServicoAccessPolicy;
        this.orcamentoFactoryImpl = orcamentoFactoryImpl;
        this.orcamentoNotificacaoService = orcamentoNotificacaoService;
        this.orcamentoVersioningServiceImpl = orcamentoVersioningServiceImpl;
        this.clienteRepository = clienteRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.orcamentoPublicacaoServiceImpl = orcamentoPublicacaoServiceImpl;
        this.buscarClientePorCpfCnpjUseCase = buscarClientePorCpfCnpjUseCase;
    }

    public OrdemServicoEntity criar(String cpfCnpj, VeiculoOrdemServicoRequest veiculoRequest, List<ServicoSolicitadoEntity> servicosSolicitados) {
        ClienteEntity cliente = buscarClientePorCpfCnpjUseCase.execute(cpfCnpj);

            VeiculoEntity veiculo = buscarOuCadastrarVeiculoUseCase.execute(
                cliente,
                veiculoRequest
        );
        validarServicosSolicitados(servicosSolicitados);

        OrdemServicoEntity ordemServico = OrdemServicoEntity.criar(cliente, veiculo);

        List<ServicoSolicitadoEntity> servicosComDados = servicosSolicitados.stream()
                .map(servico -> preencherDadosDoServico(ordemServico, servico))
                .toList();

        ordemServico.adicionarServicosSolicitados(servicosComDados);
        return salvarOs(ordemServico);
    }

    @Transactional
    @Override
    public OrdemServicoEntity incluirServicos(String numeroOs, List<ServicoSolicitadoEntity> servicos, String emailUsuarioLogado) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);

        if (StatusOrdemServico.EM_DIAGNOSTICO.equals(ordemServico.getStatus())) {
            UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);
            if (!RoleEnum.ADMIN.equals(usuarioLogado.getRole())) {
                ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
            }
        }

        List<ServicoSolicitadoEntity> servicosComDados = preencherDadosDosServicos(ordemServico, servicos);
        ordemServico.adicionarServicosSolicitados(servicosComDados);
        return ordemServicoRepository.save(ordemServico);
    }

    @Override
    public OrdemServicoEntity atribuirMecanico(String numeroOs, Long mecanicoId, String email) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);

        UsuarioEntity mecanico = buscarMecanicoParaAtribuicao(mecanicoId, email);

        if (ordemServico.getDiagnostico() == null) {
            ordemServico.setDiagnostico(new DiagnosticoEntity());
        }

        ordemServico.getDiagnostico().setMecanico(mecanico);

        return ordemServicoRepository.save(ordemServico);
    }

    private UsuarioEntity buscarMecanicoParaAtribuicao(
            Long mecanicoId,
            String mecanicoEmail
    ) {
        if (mecanicoId != null) {
            return usuarioService.buscarMecanicoPorId(mecanicoId);
        }

        if (mecanicoEmail != null && !mecanicoEmail.isBlank()) {
            UsuarioEntity usuario = usuarioService.buscarPorEmail(mecanicoEmail);

            if (!RoleEnum.MECANICO.equals(usuario.getRole())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Usuário informado não é mecânico."
                );
            }

            return usuario;
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Informe mecanicoId ou mecanicoEmail."
        );
    }

    @Override
    public OrdemServicoEntity iniciarDiagnostico(String numeroOs, String emailUsuarioLogado) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);
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

    @Override
    public OrdemServicoEntity registrarItemNecessario(
            String numeroOs,
            Long servicoId,
            String emailUsuarioLogado,
            List<ItemNecessarioEntity> itensNecessarios
    ) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);
        UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);

        if (!RoleEnum.ADMIN.equals(usuarioLogado.getRole())) {
            ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        }

        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoId);
        if (!StatusOrdemServico.EM_DIAGNOSTICO.equals(ordemServico.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Só é possível incluir peças e insumos enquanto o serviço está em diagnóstico."
            );
        }

        List<ItemNecessarioEntity> itensComDados = verificaItensNecessarios(itensNecessarios);

        servico.registrarItensNecessarios(itensComDados);

        return ordemServicoRepository.save(ordemServico);
    }

    @Override
    public OrdemServicoEntity registrarLaudo(String numeroOs, String emailUsuarioLogado, String laudo){
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);
        UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);
        ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        ordemServico.registrarLaudo(laudo);
        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    @Override
    public FinalizarDiagnosticoResult finalizarDiagnostico(String numeroOs, String emailUsuarioLogado){
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);
        UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);

        if(!RoleEnum.ADMIN.equals(usuarioLogado.getRole())){
            ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        }
        ordemServico.finalizarDiagnostico();
        int versao = orcamentoVersioningServiceImpl.proximaVersaoPrincipalNumeroOs(numeroOs);
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
    public OrdemServicoEntity buscaOrdemServicoPorNumeroOs(String numeroOs) {
        return ordemServicoRepository.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
    }

    @Transactional
    @Override
    public OrdemServicoEntity iniciarServico(String numeroOs, Long servicoId) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);
        StatusOrdemServico statusAnterior = ordemServico.getStatus();

        if (statusAnterior == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            ordemServico.iniciarExecucao();
        } else if (statusAnterior != StatusOrdemServico.EM_EXECUCAO) {
            throw new IllegalStateException("OS deve estar aprovada ou em execucao para iniciar servico.");
        }

        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoId);

        BaixaEstoqueResult baixaEstoqueResult =
                pecaInsumoService.verificarDisponibilidadeEBaixar(servico.getItensNecessarios());

        servico.iniciar(baixaEstoqueResult.itensAtualizados());

        if (!statusAnterior.equals(ordemServico.getStatus())) {
            return salvarOs(ordemServico);
        }

        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    public OrdemServicoEntity finalizarServico(String numeroOs, Long servicoId) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);

        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoId);

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
    public OrdemServicoEntity entregar(String numeroOs) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);
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
                    OrcamentoEntity orcamentoAtual = buscarOrcamentoAtual(os.getNumeroOs());
                    List<HistoricoStatusOsEntity> historico =
                            historicoStatusOsRepository.findByNumeroOsOrderByRegistradoEmAsc(os.getNumeroOs());

                    return AcompanhamentoOrdemServicoResponse.from(os, orcamentoAtual, historico);
                })
                .toList();
    }

    @Override
    public Page<OrdemServicoEntity> listar(OrdemServicoFiltro filtro, Pageable pageable, String emailUsuarioLogado) {
        UsuarioEntity usuario = usuarioService.buscarPorEmail(emailUsuarioLogado);
        String emailMecanico = RoleEnum.MECANICO.equals(usuario.getRole()) ? emailUsuarioLogado : null;
        return ordemServicoRepository.findAll(OrdemServicoSpecifications.comFiltros(filtro, emailMecanico), pageable);
    }

    @Override
    public OrcamentoEntity buscarOrcamentoAtual(String numeroOs) {
        return orcamentoRepository.findByNumeroOsAndStatus(numeroOs, StatusOrcamento.DISPONIVEL)
                .or(() -> orcamentoRepository.findTopByNumeroOsOrderByVersaoDesc(numeroOs))
                .orElse(null);
    }

    @Override
    public TempoMedioOrdemServicoResponse calcularTempoMedioFinalizacao() {
        TempoMedioOrdemServicoProjection projection =
                ordemServicoRepository.calcularTempoMedioFinalizacao();

        Double tempoMedioSegundos = projection.getTempoMedioSegundos();
        Double tempoMedioMinutos = tempoMedioSegundos == null ? null : tempoMedioSegundos / 60;
        Double tempoMedioHoras = tempoMedioSegundos == null ? null : tempoMedioSegundos / 3600;

        return new TempoMedioOrdemServicoResponse(
                projection.getQuantidadeOrdensFinalizadas(),
                tempoMedioSegundos,
                tempoMedioMinutos,
                tempoMedioHoras
        );
    }

    private List<ItemNecessarioEntity> verificaItensNecessarios(List<ItemNecessarioEntity> itensNecessarios) {
        return itensNecessarios.stream()
                .map(itemNecessario -> {
                    PecaInsumoEntity itemEstoque =
                            pecaInsumoService.buscarEntityPorId(itemNecessario.getPecaInsumoId());

                    boolean disponivel =
                            itemEstoque.getQuantidade() >= itemNecessario.getQuantidade();

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
                            new SituacaoEstoque(
                                    itemEstoque.getQuantidade(),
                                    motivoPendencia
                            )
                    );
                })
                .toList();
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
                        mensagemParaCliente(os.getStatus()),
                        os.getNumeroOs()
                )
        );
    }
}
