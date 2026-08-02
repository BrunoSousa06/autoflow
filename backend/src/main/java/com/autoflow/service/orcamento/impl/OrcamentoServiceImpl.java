package com.autoflow.service.orcamento.impl;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.application.usecases.ordemservico.reparoadicional.AprovarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.application.usecases.ordemservico.reparoadicional.RecusarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.orcamento.OrcamentoSpecifications;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import com.autoflow.service.orcamento.OrcamentoService;
import com.autoflow.service.orcamento.dto.OrcamentoFiltro;
import com.autoflow.service.usuario.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrcamentoServiceImpl implements OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoPublicacaoService publicacaoService;
    private final AprovarReparoAdicionalPorOrcamentoUseCase aprovarReparoAdicionalPorOrcamentoUseCase;
    private final RecusarReparoAdicionalPorOrcamentoUseCase recusarReparoAdicionalPorOrcamentoUseCase;
    private final UsuarioService usuarioService;

    @Override
    public OrcamentoEntity consultarAutenticado(Long orcamentoId, String emailUsuario) {
        OrcamentoEntity orcamento = getOrcamento(orcamentoId);
        validarUsuarioPodeAcessar(orcamento, emailUsuario);
        return orcamento;
    }

    @Override
    public OrcamentoEntity consultarPorToken(Long orcamentoId, String token){
        OrcamentoEntity orcamento = getOrcamento(orcamentoId);
        validarToken(orcamento, token);
        return orcamento;
    }

    @Override
    @Transactional
    public OrcamentoEntity aprovar(Long orcamentoId, String emailUsuario) {
        OrcamentoEntity orcamento = getOrcamento(orcamentoId);
        UsuarioEntity usuarioEntity = validarUsuarioPodeAcessar(orcamento, emailUsuario);
        return efetivarAprovacao(orcamento, usuarioEntity.getNome());
    }

    @Override
    public OrcamentoEntity consultarDaOrdem(Long orcamentoId, String numeroOs) {
        OrcamentoEntity orcamento = getOrcamento(orcamentoId);
        validarPertenceAOrdem(orcamento, numeroOs);
        return orcamento;
    }

    @Override
    @Transactional
    public OrcamentoEntity aprovarDaOrdem(Long orcamentoId, String numeroOs) {
        OrcamentoEntity orcamento = getOrcamento(orcamentoId);
        validarPertenceAOrdem(orcamento, numeroOs);
        return efetivarAprovacao(orcamento, orcamento.getCliente().getNome());
    }

    private OrcamentoEntity efetivarAprovacao(OrcamentoEntity orcamento, String assinaturaNome) {
        if (orcamento.getStatus() == StatusOrcamento.APROVADO || orcamento.getStatus() == StatusOrcamento.REPROVADO) {
            return orcamento;
        }
        if (orcamento.getStatus() != StatusOrcamento.DISPONIVEL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento nao esta disponível");
        }

        orcamento.setStatus(StatusOrcamento.APROVADO);
        orcamento.setAssinaturaNome(assinaturaNome);
        orcamento.setAprovadoEm(LocalDateTime.now(ZoneId.systemDefault()));

        OrcamentoEntity orcamentoSalvo = orcamentoRepository.save(orcamento);

        if (aprovarReparoAdicionalPorOrcamentoUseCase.executeSeExistir(orcamento.getId())) {
            return orcamentoSalvo;
        }

        OrdemServicoEntity ordemServico = ordemServicoRepository.findById(orcamento.getOrdemServicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OS nao encontrada"));
        ordemServico.iniciarExecucao();
        ordemServicoRepository.save(ordemServico);

        return orcamentoRepository.save(orcamento);
    }

    private void validarPertenceAOrdem(OrcamentoEntity orcamento, String numeroOs) {
        if (!orcamento.getNumeroOs().equals(numeroOs)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado para esta ordem de serviço");
        }
    }

    private UsuarioEntity validarUsuarioPodeAcessar(OrcamentoEntity orcamento, String emailUsuario) {
        UsuarioEntity usuarioEntity = usuarioService.buscarPorEmail(emailUsuario);

        if (usuarioEntity.getRole().equals(RoleEnum.CLIENTE) && !orcamento.getCliente().getEmail().equals(emailUsuario)) {
                throw new  ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return usuarioEntity;
    }

    @Override
    public OrcamentoEntity recusar(Long orcamentoId, String motivo, String emailUsuario) {
        OrcamentoEntity orcamento = getOrcamento(orcamentoId);
        UsuarioEntity usuarioEntity = validarUsuarioPodeAcessar(orcamento, emailUsuario);
        if (orcamento.getStatus() == StatusOrcamento.APROVADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento já aprovado, não é possivel recusar");
        }
        if (orcamento.getStatus() == StatusOrcamento.REPROVADO) {
            return orcamento;
        }

        if (orcamento.getStatus() != StatusOrcamento.DISPONIVEL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento não esta disponivel");
        }

        orcamento.setStatus(StatusOrcamento.REPROVADO);
        orcamento.setReprovadoEm(LocalDateTime.now(ZoneId.systemDefault()));
        orcamento.setAssinaturaNome(usuarioEntity.getNome());
        if(motivo != null) orcamento.setRecusaMotivo(motivo);

        OrcamentoEntity orcamentoSalvo = orcamentoRepository.save(orcamento);

        if (recusarReparoAdicionalPorOrcamentoUseCase.executeSeExistir(orcamento.getId(), motivo)) {
            return orcamentoSalvo;
        }

        OrdemServicoEntity ordemServico = ordemServicoRepository.findByNumeroOs(orcamento.getNumeroOs())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "OS nao encontrada"));
        ordemServico.finalizarPorOrcamentoRecusado();
        ordemServicoRepository.save(ordemServico);
        return orcamento;
    }

    @Override
    public List<OrcamentoEntity> consultarOrcamentos(String emailUsuario, OrcamentoFiltro filtro) {
        UsuarioEntity usuario = usuarioService.buscarPorEmail(emailUsuario);

        OrcamentoFiltro filtroEfetivo = aplicarRestricaoDeAcesso(usuario, normalizarFiltro(filtro));

        return orcamentoRepository.findAll(
                OrcamentoSpecifications.comFiltros(filtroEfetivo)
        );
    }

    private OrcamentoFiltro normalizarFiltro(OrcamentoFiltro filtro) {
        if (filtro != null) {
            return filtro;
        }

        return new OrcamentoFiltro(null, null, null, null, null, null);
    }

    private OrcamentoFiltro aplicarRestricaoDeAcesso(UsuarioEntity usuario, OrcamentoFiltro filtro) {
        if (!RoleEnum.CLIENTE.equals(usuario.getRole())) {
            return filtro;
        }

        if (filtro.clienteEmail() != null
                && !filtro.clienteEmail().isBlank()
                && !filtro.clienteEmail().equalsIgnoreCase(usuario.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return new OrcamentoFiltro(
                filtro.status(),
                filtro.numeroOs(),
                filtro.placa(),
                usuario.getEmail(),
                filtro.clienteDocumento(),
                filtro.tipo()
        );
    }

    private OrcamentoEntity getOrcamento(Long orcamentoId) {
        return orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado"));
    }

    @Override
    public void validarToken(OrcamentoEntity orcamento, String token) {
        if(!publicacaoService.validarToken(orcamento, token)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido");
        }
    }
}
