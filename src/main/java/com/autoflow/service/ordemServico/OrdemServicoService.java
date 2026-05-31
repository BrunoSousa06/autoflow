package com.autoflow.service.ordemServico;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.ordemServico.DiagnosticoEntity;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemServico.StatusOrdemServico;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.repository.ordemServico.OrdemServicoRepository;
import com.autoflow.service.cliente.ClienteService;
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

    private final ClienteService clienteService;
    private final VeiculoService veiculoService;
    private final ServicoService servicoService;
    private final UsuarioService usuarioService;

    public OrdemServicoEntity criar(Long clienteId, Long veiculoId, List<ServicoSolicitadoEntity> servicosSolicitados) {
        ClienteEntity cliente = clienteService.buscarPorId(clienteId);
        VeiculoEntity veiculo = veiculoService.buscarPorId(veiculoId);
        validarVeiculoDoCliente(veiculo, cliente);

        List<ServicoSolicitadoEntity> servicoComDados = preencherDadosDosServicos(servicosSolicitados);

        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(cliente, veiculo, servicoComDados);
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
        if (!RoleEnum.ROLE_ADMIN.equals(usuarioLogado.getRole())) {
            validarMecanicoAtribuido(ordemServico, usuarioLogado);
        }
        ordemServico.getDiagnostico().setIniciadoEm(LocalDateTime.now());
        ordemServico.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        return ordemServicoRepository.save(ordemServico);
    }

    private void validarMecanicoAtribuido(OrdemServicoEntity ordemServico, UsuarioEntity usuarioLogado) {
        DiagnosticoEntity diagnostico = ordemServico.getDiagnostico();
        if (diagnostico == null || diagnostico.getMecanico() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A ordem de serviço ainda não possui mecânico atribuído."
            );
        }
        Long mecanicoAtribuidoId = diagnostico.getMecanico().getId();
        if(!mecanicoAtribuidoId.equals(usuarioLogado.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Somente o mecânico atribuído pode iniciar o diagnóstico."
            );
        }
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

    private static void validarVeiculoDoCliente(VeiculoEntity veiculo, ClienteEntity cliente) {
        if (veiculo.getCliente() == null || !veiculo.getCliente().getId().equals(cliente.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Veiculo nao pertence ao cliente informado.");
        }
    }


}
