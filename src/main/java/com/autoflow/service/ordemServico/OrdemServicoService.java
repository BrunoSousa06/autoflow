package com.autoflow.service.ordemServico;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemServico.*;
import com.autoflow.domain.pecaInsumo.PecaInsumoEntity;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemServico.OrdemServicoRepository;
import com.autoflow.service.orcamento.OrcamentoFactory;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import com.autoflow.service.pecaInsumo.PecaInsumoService;
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
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;

    private final VeiculoService veiculoService;
    private final ServicoService servicoService;
    private final UsuarioService usuarioService;
    private final PecaInsumoService pecaInsumoService;
    private final OrdemServicoAccessPolicy ordemServicoAccessPolicy;
    private final OrcamentoFactory orcamentoFactory;
    private final OrcamentoVersioningService orcamentoVersioningService;
    private final OrcamentoRepository orcamentoRepository;

    public OrdemServicoEntity criar(Long veiculoId, List<ServicoSolicitadoEntity> servicosSolicitados) {
        VeiculoEntity veiculo = veiculoService.buscarPorId(veiculoId);
        if(veiculo.getCliente() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Veiculo sem cliente vinculado.");
        }

        List<ServicoSolicitadoEntity> servicoComDados = preencherDadosDosServicos(servicosSolicitados);

        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(veiculo, servicoComDados);
        return ordemServicoRepository.save(ordemServicoEntity);
    }

    @Transactional
    public OrdemServicoEntity incluirServicos(Long ordemServicoId, List<ServicoSolicitadoEntity> servicos) {

        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);

        List<ServicoSolicitadoEntity> servicosComDados = preencherDadosDosServicos(servicos);

        ordemServico.adicionarServicos(servicosComDados);

        return ordemServicoRepository.save(ordemServico);
    }

    public OrdemServicoEntity atribuirMecanico(Long ordemServicoId, Long mecanicoId) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);

        UsuarioEntity mecanico = usuarioService.buscarMecanicoPorId(mecanicoId);

        if (ordemServico.getDiagnostico() == null) {
            ordemServico.setDiagnostico(new DiagnosticoEntity());
        }

        ordemServico.getDiagnostico().setMecanico(mecanico);

        return ordemServicoRepository.save(ordemServico);
    }

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

    public OrdemServicoEntity registrarItemNecessario(Long ordemServicoId, String emailUsuarioLogado, List<ItemNecessarioEntity> itensNecessarios) {
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);

        UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);

        if (!RoleEnum.ADMIN.equals(usuarioLogado.getRole())) {
            ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        }

        List<ItemNecessarioEntity> itemNecessarios = itensNecessarios.stream()
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

        ordemServico.adicionarItensNecessarios(itemNecessarios);
        return ordemServicoRepository.save(ordemServico);
    }


    public OrdemServicoEntity registrarLaudo(Long ordemServicoId, String emailUsuarioLogado, String laudo){
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);
        UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);
        ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        ordemServico.registrarLaudo(laudo);
        return ordemServicoRepository.save(ordemServico);
    }

    public OrdemServicoEntity finalizarDiagnostico(Long ordemServicoId, String emailUsuarioLogado){
        OrdemServicoEntity ordemServico = buscaOrdemServicoPorId(ordemServicoId);
        UsuarioEntity usuarioLogado = usuarioService.buscarPorEmail(emailUsuarioLogado);

        if(!RoleEnum.ADMIN.equals(usuarioLogado.getRole())){
            ordemServicoAccessPolicy.validarPodeAlterarDiagnostico(ordemServico, usuarioLogado);
        }
        ordemServico.finalizarDiagnostico();
        int versao = orcamentoVersioningService.proximaVersaoPrincipal(ordemServicoId);
        OrcamentoEntity orcamento = orcamentoFactory.criarPrincipalDisponivel(ordemServico, versao, LocalDateTime.now());
        ordemServico.aguardarAprovacao();
        orcamentoRepository.save(orcamento);
        return ordemServicoRepository.save(ordemServico);
    }

    private List<ServicoSolicitadoEntity> preencherDadosDosServicos(List<ServicoSolicitadoEntity> servicos) {
        validarServicosSolicitados(servicos);

        return servicos.stream().map(this::preencherDadosDoServico).toList();
    }

    public OrdemServicoEntity buscaOrdemServicoPorId(Long ordemServicoId) {
        return ordemServicoRepository.findById(ordemServicoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada."));
    }

    private ServicoSolicitadoEntity preencherDadosDoServico(ServicoSolicitadoEntity servicoSolicitado) {
        ServicoEntity servico = servicoService.buscarEntityPorId(servicoSolicitado.getServicoId());

        return new ServicoSolicitadoEntity(servico.getId(), servico.getNome(), servico.getValor());
    }

    private static void validarServicosSolicitados(List<ServicoSolicitadoEntity> servicos) {
        if (servicos == null || servicos.isEmpty()) {
            throw new IllegalArgumentException("A ordem de servico deve ter ao menos um servico solicitado.");
        }
    }
}
