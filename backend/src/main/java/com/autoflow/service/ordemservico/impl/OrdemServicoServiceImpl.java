package com.autoflow.service.ordemservico.impl;

import com.autoflow.application.usecases.cliente.BuscarClientePorCpfCnpjUseCase;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import com.autoflow.application.usecases.pecainsumo.BaixarEstoqueUseCase;
import com.autoflow.application.usecases.pecainsumo.ConsultarDisponibilidadeEstoqueUseCase;
import com.autoflow.application.usecases.ordemservico.acompanhamento.GerarTokenAcompanhamentoUseCase;
import com.autoflow.application.usecases.ordemservico.acompanhamento.EnviarLinkAcompanhamentoUseCase;
import com.autoflow.application.usecases.ordemservico.acompanhamento.AcompanharOrdemServicoUseCase;
import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.service.ordemservico.BuscarOuCadastrarVeiculoForOrdemServicoUseCase;
import com.autoflow.application.usecases.usuario.BuscarMecanicoPorIdUseCase;
import com.autoflow.application.usecases.usuario.BuscarUsuarioPorEmailUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.*;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.repository.ordemservico.historico.HistoricoStatusOsRepository;
import com.autoflow.application.dto.notificacao.OrcamentoNotificacao;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoNotificacaoGateway;
import com.autoflow.application.gateway.OrcamentoVersioningGateway;
import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.repository.ordemservico.OrdemServicoSpecifications;
import com.autoflow.service.ordemservico.OrdemServicoService;
import com.autoflow.service.ordemservico.dto.OrdemServicoCriada;
import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;
import com.autoflow.service.ordemservico.dto.OrdemServicoFiltro;
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

import static com.autoflow.application.usecases.ordemservico.StatusOrdemServicoMensagemPolicy.mensagem;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrdemServicoServiceImpl implements OrdemServicoService {

    private static final SecureRandom TOKEN_RANDOM = new SecureRandom();

    private final OrdemServicoRepository ordemServicoRepository;
    private final BuscarOuCadastrarVeiculoForOrdemServicoUseCase buscarOuCadastrarVeiculoUseCase;
    private final ServicoService servicoService;
    private final BuscarUsuarioPorEmailUseCase buscarUsuarioPorEmailUseCase;
    private final BuscarMecanicoPorIdUseCase buscarMecanicoPorIdUseCase;
    private final BaixarEstoqueUseCase baixarEstoqueUseCase;
    private final ConsultarDisponibilidadeEstoqueUseCase consultarDisponibilidadeEstoqueUseCase;
    private final OrdemServicoAccessPolicy ordemServicoAccessPolicy;
    private final OrcamentoFactory orcamentoFactoryImpl;
    private final OrcamentoVersioningGateway orcamentoVersioningGateway;
    private final OrcamentoGateway orcamentoGateway;
    private final OrcamentoPublicacaoGateway orcamentoPublicacaoGateway;
    private final HistoricoStatusOsRepository historicoStatusOsRepository;
    private final OrcamentoNotificacaoGateway orcamentoNotificacaoGateway;
    private final BuscarClientePorCpfCnpjUseCase buscarClientePorCpfCnpjUseCase;
    private final GerarTokenAcompanhamentoUseCase gerarTokenAcompanhamentoUseCase;
    private final EnviarLinkAcompanhamentoUseCase enviarLinkAcompanhamentoUseCase;
    private final AcompanharOrdemServicoUseCase acompanharOrdemServicoUseCase;

    @Autowired
    public OrdemServicoServiceImpl(OrdemServicoRepository ordemServicoRepository, BuscarOuCadastrarVeiculoForOrdemServicoUseCase buscarOuCadastrarVeiculoUseCase, ServicoService servicoService,
                                   BuscarUsuarioPorEmailUseCase buscarUsuarioPorEmailUseCase, BuscarMecanicoPorIdUseCase buscarMecanicoPorIdUseCase,
                                   HistoricoStatusOsRepository historicoStatusOsRepository, BaixarEstoqueUseCase baixarEstoqueUseCase,
                                   OrdemServicoAccessPolicy ordemServicoAccessPolicy, OrcamentoFactory orcamentoFactoryImpl,
                                   OrcamentoNotificacaoGateway orcamentoNotificacaoGateway, OrcamentoVersioningGateway orcamentoVersioningGateway,
                                    OrcamentoGateway orcamentoGateway, OrcamentoPublicacaoGateway orcamentoPublicacaoGateway,
                                    BuscarClientePorCpfCnpjUseCase buscarClientePorCpfCnpjUseCase,
                                    GerarTokenAcompanhamentoUseCase gerarTokenAcompanhamentoUseCase,
                                    EnviarLinkAcompanhamentoUseCase enviarLinkAcompanhamentoUseCase,
                                    ConsultarDisponibilidadeEstoqueUseCase consultarDisponibilidadeEstoqueUseCase,
                                    AcompanharOrdemServicoUseCase acompanharOrdemServicoUseCase) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.buscarOuCadastrarVeiculoUseCase = buscarOuCadastrarVeiculoUseCase;
        this.servicoService = servicoService;
        this.buscarUsuarioPorEmailUseCase = buscarUsuarioPorEmailUseCase;
        this.buscarMecanicoPorIdUseCase = buscarMecanicoPorIdUseCase;
        this.historicoStatusOsRepository = historicoStatusOsRepository;
        this.baixarEstoqueUseCase = baixarEstoqueUseCase;
        this.ordemServicoAccessPolicy = ordemServicoAccessPolicy;
        this.orcamentoFactoryImpl = orcamentoFactoryImpl;
        this.orcamentoNotificacaoGateway = orcamentoNotificacaoGateway;
        this.orcamentoVersioningGateway = orcamentoVersioningGateway;
        this.orcamentoGateway = orcamentoGateway;
        this.orcamentoPublicacaoGateway = orcamentoPublicacaoGateway;
        this.buscarClientePorCpfCnpjUseCase = buscarClientePorCpfCnpjUseCase;
        this.gerarTokenAcompanhamentoUseCase = gerarTokenAcompanhamentoUseCase;
        this.enviarLinkAcompanhamentoUseCase = enviarLinkAcompanhamentoUseCase;
        this.consultarDisponibilidadeEstoqueUseCase = consultarDisponibilidadeEstoqueUseCase;
        this.acompanharOrdemServicoUseCase = acompanharOrdemServicoUseCase;
    }

    @Transactional
    public OrdemServicoCriada criar(
            String cpfCnpj,
            VeiculoOrdemServicoInput veiculoRequest,
            List<ServicoSolicitadoEntity> servicosSolicitados
    ) {
        ClienteOutput cliente = buscarClientePorCpfCnpjUseCase.execute(cpfCnpj);

        VeiculoEntity veiculo =
                buscarOuCadastrarVeiculoUseCase.execute(cliente, veiculoRequest);

        validarServicosSolicitados(servicosSolicitados);

        OrdemServicoEntity ordemServico =
                OrdemServicoEntity.criar(
                        cliente.id(), cliente.nome(), cliente.cpfCnpj(), cliente.email(), cliente.telefone(), veiculo);

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
        int versao = orcamentoVersioningGateway.proximaVersaoPorNumeroOs(numeroOs, TipoOrcamento.PRINCIPAL);
        OrcamentoEntity orcamento = orcamentoFactoryImpl.criarPrincipalDisponivel(
                ordemServico, versao, LocalDateTime.now(ZoneId.systemDefault()));

        ordemServico.aguardarAprovacao();

        OrcamentoEntity orcamentoSalvo = orcamentoGateway.save(orcamento);
        String publicUrl = orcamentoPublicacaoGateway.publicar(orcamentoSalvo.getId());
        try {
            var cliente = orcamentoSalvo.getCliente();
            orcamentoNotificacaoGateway.notificar(new OrcamentoNotificacao(
                    orcamentoSalvo.getId(), orcamentoSalvo.getTipo(), orcamentoSalvo.getNumeroOs(),
                    cliente.getNome(), cliente.getEmail(), publicUrl));
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
        OrdemServicoEntity ordemServico = ordemServicoRepository.findByNumeroOsForUpdate(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
        if (ordemServico.getStatus() != StatusOrdemServico.EM_EXECUCAO) {
            throw new IllegalStateException("O serviço só pode ser iniciado após a aprovação do orçamento.");
        }

        ServicoSolicitadoEntity servico = ordemServico.buscarServicoSolicitado(servicoId);
        servico.validarPodeIniciar();

        servico.iniciar(baixarEstoqueUseCase.execute(servico.getItensNecessarios()));

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

    @Override
    public List<AcompanhamentoOrdemServicoOutput> listarAcompanhamentoCliente(String emailCliente) {
        return acompanharOrdemServicoUseCase.execute(emailCliente);
    }

    @Override
    public Page<OrdemServicoEntity> listar(OrdemServicoFiltro filtro, Pageable pageable, String emailUsuarioLogado) {
        UsuarioEntity usuario = buscarUsuarioPorEmailUseCase.execute(emailUsuarioLogado);
        String emailMecanico = RoleEnum.MECANICO.equals(usuario.getRole()) ? emailUsuarioLogado : null;
        return ordemServicoRepository.findAll(OrdemServicoSpecifications.comFiltros(filtro, emailMecanico), pageable);
    }

    @Override
    public OrcamentoEntity buscarOrcamentoAtual(String numeroOs) {
        return orcamentoGateway.findByNumeroOsAndStatus(numeroOs, StatusOrcamento.DISPONIVEL)
                .or(() -> orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc(numeroOs))
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
                        mensagem(os.getStatus()),
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
