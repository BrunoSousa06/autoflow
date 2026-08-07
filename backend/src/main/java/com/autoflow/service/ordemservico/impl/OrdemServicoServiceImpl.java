package com.autoflow.service.ordemservico.impl;

import com.autoflow.application.usecases.cliente.BuscarClientePorCpfCnpjUseCase;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import com.autoflow.application.usecases.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase;
import com.autoflow.application.usecases.ordemservico.acompanhamento.GerarTokenAcompanhamentoUseCase;
import com.autoflow.application.usecases.ordemservico.acompanhamento.EnviarLinkAcompanhamentoUseCase;
import com.autoflow.application.usecases.veiculo.BuscarOuCadastrarVeiculoUseCase;
import com.autoflow.application.usecases.usuario.BuscarMecanicoPorIdUseCase;
import com.autoflow.application.usecases.usuario.BuscarUsuarioPorEmailUseCase;
import com.autoflow.presentation.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse;
import com.autoflow.controller.ordemservico.request.VeiculoOrdemServicoRequest;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.infrastructure.persistence.repository.ClienteRepository;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.repository.ordemservico.historico.HistoricoStatusOsRepository;
import com.autoflow.service.orcamento.OrcamentoNotificacaoService;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.repository.ordemservico.OrdemServicoSpecifications;
import com.autoflow.service.ordemservico.OrdemServicoService;
import com.autoflow.service.ordemservico.dto.OrdemServicoCriada;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import com.autoflow.service.ordemservico.dto.OrdemServicoFiltro;
import com.autoflow.service.pecainsumo.BaixaEstoqueResult;
import com.autoflow.service.pecainsumo.PecaInsumoService;
import com.autoflow.service.servico.ServicoService;
import com.autoflow.application.dto.ordemservico.acompanhamento.TokenAcompanhamentoOutput;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import static com.autoflow.presentation.ordemservico.acompanhamento.response.AcompanhamentoOrdemServicoResponse.mensagemParaCliente;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrdemServicoServiceImpl implements OrdemServicoService {

    private static final SecureRandom TOKEN_RANDOM = new SecureRandom();

    private final OrdemServicoRepository ordemServicoRepository;
    private final BuscarOuCadastrarVeiculoUseCase buscarOuCadastrarVeiculoUseCase;
    private final ServicoService servicoService;
    private final BuscarUsuarioPorEmailUseCase buscarUsuarioPorEmailUseCase;
    private final BuscarMecanicoPorIdUseCase buscarMecanicoPorIdUseCase;
    private final PecaInsumoService pecaInsumoService;
    private final ConsultarDisponibilidadeEstoqueUseCase consultarDisponibilidadeEstoqueUseCase;
    private final OrdemServicoAccessPolicy ordemServicoAccessPolicy;
    private final OrcamentoFactory orcamentoFactoryImpl;
    private final OrcamentoVersioningService orcamentoVersioningServiceImpl;
    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoPublicacaoGateway orcamentoPublicacaoGateway;
    private final ClienteRepository clienteRepository;
    private final HistoricoStatusOsRepository historicoStatusOsRepository;
    private final OrcamentoNotificacaoService orcamentoNotificacaoService;
    private final BuscarClientePorCpfCnpjUseCase buscarClientePorCpfCnpjUseCase;
    private final GerarTokenAcompanhamentoUseCase gerarTokenAcompanhamentoUseCase;
    private final EnviarLinkAcompanhamentoUseCase enviarLinkAcompanhamentoUseCase;

    @Autowired
    public OrdemServicoServiceImpl(OrdemServicoRepository ordemServicoRepository, BuscarOuCadastrarVeiculoUseCase buscarOuCadastrarVeiculoUseCase, ServicoService servicoService,
                                   BuscarUsuarioPorEmailUseCase buscarUsuarioPorEmailUseCase, BuscarMecanicoPorIdUseCase buscarMecanicoPorIdUseCase,
                                   HistoricoStatusOsRepository historicoStatusOsRepository, PecaInsumoService pecaInsumoService,
                                   OrdemServicoAccessPolicy ordemServicoAccessPolicy, OrcamentoFactory orcamentoFactoryImpl,
                                   OrcamentoNotificacaoService orcamentoNotificacaoService, OrcamentoVersioningService orcamentoVersioningServiceImpl,
                                   ClienteRepository clienteRepository, OrcamentoRepository orcamentoRepository, OrcamentoPublicacaoGateway orcamentoPublicacaoGateway,
                                   BuscarClientePorCpfCnpjUseCase buscarClientePorCpfCnpjUseCase,
                                   GerarTokenAcompanhamentoUseCase gerarTokenAcompanhamentoUseCase,
                                   EnviarLinkAcompanhamentoUseCase enviarLinkAcompanhamentoUseCase,
                                   ConsultarDisponibilidadeEstoqueUseCase consultarDisponibilidadeEstoqueUseCase) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.buscarOuCadastrarVeiculoUseCase = buscarOuCadastrarVeiculoUseCase;
        this.servicoService = servicoService;
        this.buscarUsuarioPorEmailUseCase = buscarUsuarioPorEmailUseCase;
        this.buscarMecanicoPorIdUseCase = buscarMecanicoPorIdUseCase;
        this.historicoStatusOsRepository = historicoStatusOsRepository;
        this.pecaInsumoService = pecaInsumoService;
        this.ordemServicoAccessPolicy = ordemServicoAccessPolicy;
        this.orcamentoFactoryImpl = orcamentoFactoryImpl;
        this.orcamentoNotificacaoService = orcamentoNotificacaoService;
        this.orcamentoVersioningServiceImpl = orcamentoVersioningServiceImpl;
        this.clienteRepository = clienteRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.orcamentoPublicacaoGateway = orcamentoPublicacaoGateway;
        this.buscarClientePorCpfCnpjUseCase = buscarClientePorCpfCnpjUseCase;
        this.gerarTokenAcompanhamentoUseCase = gerarTokenAcompanhamentoUseCase;
        this.enviarLinkAcompanhamentoUseCase = enviarLinkAcompanhamentoUseCase;
        this.consultarDisponibilidadeEstoqueUseCase = consultarDisponibilidadeEstoqueUseCase;
    }

    @Transactional
    public OrdemServicoCriada criar(
            String cpfCnpj,
            VeiculoOrdemServicoRequest veiculoRequest,
            List<ServicoSolicitadoEntity> servicosSolicitados
    ) {
        ClienteEntity cliente =
                buscarClientePorCpfCnpjUseCase.execute(cpfCnpj);

        VeiculoEntity veiculo =
                buscarOuCadastrarVeiculoUseCase.execute(
                        cliente,
                        veiculoRequest
                );

        validarServicosSolicitados(servicosSolicitados);

        OrdemServicoEntity ordemServico =
                OrdemServicoEntity.criar(cliente, veiculo);

        List<ServicoSolicitadoEntity> servicosComDados =
                servicosSolicitados.stream()
                        .map(servico ->
                                preencherDadosDoServico(
                                        ordemServico,
                                        servico
                                )
                        )
                        .toList();

        ordemServico.adicionarServicosSolicitados(
                servicosComDados
        );

        OrdemServicoEntity ordemSalva =
                salvarOs(ordemServico);

        TokenAcompanhamentoOutput tokenGerado =
                gerarTokenAcompanhamentoUseCase.execute(
                        ordemSalva.getId()
                );

        try {
            enviarLinkAcompanhamentoUseCase.execute(
                    ordemSalva,
                    tokenGerado.token()
            );
        } catch (RuntimeException exception) {
            log.error("Não foi possível enviar o link de acompanhamento da OS {}", ordemSalva.getNumeroOs(), exception);
        }

        return new OrdemServicoCriada(
                ordemSalva,
                tokenGerado.token()
        );
    }

    @Transactional
    @Override
    public OrdemServicoEntity incluirServicos(String numeroOs, List<ServicoSolicitadoEntity> servicos, String emailUsuarioLogado) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);

        if (StatusOrdemServico.EM_DIAGNOSTICO.equals(ordemServico.getStatus())) {
            UsuarioEntity usuarioLogado = buscarUsuarioPorEmailUseCase.execute(emailUsuarioLogado);
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
            return buscarMecanicoPorIdUseCase.execute(mecanicoId);
        }

        if (mecanicoEmail != null && !mecanicoEmail.isBlank()) {
            UsuarioEntity usuario = buscarUsuarioPorEmailUseCase.execute(mecanicoEmail);

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
        UsuarioEntity usuarioLogado = buscarUsuarioPorEmailUseCase.execute(emailUsuarioLogado);
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
        UsuarioEntity usuarioLogado = buscarUsuarioPorEmailUseCase.execute(emailUsuarioLogado);

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
        UsuarioEntity usuarioLogado = buscarUsuarioPorEmailUseCase.execute(emailUsuarioLogado);
        ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        ordemServico.registrarLaudo(laudo);
        return ordemServicoRepository.save(ordemServico);
    }

    @Transactional
    @Override
    public FinalizarDiagnosticoResult finalizarDiagnostico(String numeroOs, String emailUsuarioLogado){
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorNumeroOs(numeroOs);
        UsuarioEntity usuarioLogado = buscarUsuarioPorEmailUseCase.execute(emailUsuarioLogado);

        if(!RoleEnum.ADMIN.equals(usuarioLogado.getRole())){
            ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        }
        ordemServico.finalizarDiagnostico();
        int versao = orcamentoVersioningServiceImpl.proximaVersaoPrincipalNumeroOs(numeroOs);
        OrcamentoEntity orcamento = orcamentoFactoryImpl.criarPrincipalDisponivel(
                ordemServico, versao, LocalDateTime.now(ZoneId.systemDefault()));

        ordemServico.aguardarAprovacao();

        OrcamentoEntity orcamentoSalvo = orcamentoRepository.save(orcamento);
        String publicUrl = orcamentoPublicacaoGateway.publicar(orcamentoSalvo.getId());
        try {
            orcamentoNotificacaoService.enviarLinkOrcamentoParaCliente(
                    orcamentoSalvo,
                    ordemServico,
                    publicUrl
            );
        } catch (Exception e) {
            log.error(
                    "Falha ao notificar cliente sobre orçamento da OS {}. orcamentoId={}",
                    ordemServico.getNumeroOs(),
                    orcamentoSalvo.getId(),
                    e
            );
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
        if (ordemServico.getStatus() != StatusOrdemServico.EM_EXECUCAO) {
            throw new IllegalStateException("O serviço só pode ser iniciado após a aprovação do orçamento.");
        }

        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoId);

        BaixaEstoqueResult baixaEstoqueResult =
                pecaInsumoService.verificarDisponibilidadeEBaixar(servico.getItensNecessarios());

        servico.iniciar(baixaEstoqueResult.itensAtualizados());

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
        UsuarioEntity usuario = buscarUsuarioPorEmailUseCase.execute(emailUsuarioLogado);
        String emailMecanico = RoleEnum.MECANICO.equals(usuario.getRole()) ? emailUsuarioLogado : null;
        return ordemServicoRepository.findAll(OrdemServicoSpecifications.comFiltros(filtro, emailMecanico), pageable);
    }

    @Override
    public OrcamentoEntity buscarOrcamentoAtual(String numeroOs) {
        return orcamentoRepository.findByNumeroOsAndStatus(numeroOs, StatusOrcamento.DISPONIVEL)
                .or(() -> orcamentoRepository.findTopByNumeroOsOrderByVersaoDesc(numeroOs))
                .orElse(null);
    }

    private List<ItemNecessarioEntity> verificaItensNecessarios(List<ItemNecessarioEntity> itensNecessarios) {
        return consultarDisponibilidadeEstoqueUseCase.execute(itensNecessarios);
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
    public String gerarToken() {
        byte[] bytes = new byte[32];
        TOKEN_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public String calcularHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Algoritmo SHA-256 indisponível",
                    exception
            );
        }
    }
}
