package com.autoflow.presentation.ordemservico;

import com.autoflow.application.gateway.AcompanhamentoUrlGateway;
import com.autoflow.application.input.ordemservico.CriarOrdemServicoCommand;
import com.autoflow.application.input.veiculo.VeiculoInput;
import com.autoflow.application.output.ordemservico.OrdemServicoCriadaOutput;
import com.autoflow.application.port.in.ordemservico.AtribuirMecanicoUseCase;
import com.autoflow.application.port.in.ordemservico.CriarOrdemServicoUseCase;
import com.autoflow.application.port.in.ordemservico.IncluirServicosUseCase;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.presentation.ordemservico.request.CriarOrdemServicoRequest;
import com.autoflow.presentation.ordemservico.request.IncluirMecanicoRequest;
import com.autoflow.presentation.ordemservico.request.ServicoSolicitadoRequest;
import com.autoflow.presentation.ordemservico.response.OrdemServicoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens-servico")
public class OrdemServicoAdministracaoController {

    private final CriarOrdemServicoUseCase criarOrdemServico;
    private final IncluirServicosUseCase incluirServicos;
    private final AtribuirMecanicoUseCase atribuirMecanico;
    private final ServicoSolicitadoControllerMapper servicoMapper;
    private final AcompanhamentoUrlGateway acompanhamentoUrl;

    public OrdemServicoAdministracaoController(
            CriarOrdemServicoUseCase criarOrdemServico,
            IncluirServicosUseCase incluirServicos,
            AtribuirMecanicoUseCase atribuirMecanico,
            ServicoSolicitadoControllerMapper servicoMapper,
            AcompanhamentoUrlGateway acompanhamentoUrl) {
        this.criarOrdemServico = criarOrdemServico;
        this.incluirServicos = incluirServicos;
        this.atribuirMecanico = atribuirMecanico;
        this.servicoMapper = servicoMapper;
        this.acompanhamentoUrl = acompanhamentoUrl;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public OrdemServicoResponse criar(@Valid @RequestBody CriarOrdemServicoRequest request) {
        OrdemServicoCriadaOutput criada = criarOrdemServico.execute(new CriarOrdemServicoCommand(
                request.cpfCnpj(),
                new VeiculoInput(request.veiculo().marca(), request.veiculo().ano(),
                        request.veiculo().placa(), request.veiculo().modelo()),
                request.servicosSolicitados().stream().map(ServicoSolicitadoRequest::servicoId).toList()));
        return OrdemServicoResponse.fromDomain(criada, acompanhamentoUrl.gerar(criada.tokenAcompanhamento()));
    }

    @PostMapping("/{numeroOs}/servicos")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public OrdemServicoResponse incluirServico(
            @PathVariable String numeroOs,
            @Valid @RequestBody List<ServicoSolicitadoRequest> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ServicoSolicitado> servicos = servicoMapper.mapToEntities(request);
        return OrdemServicoResponse.fromDomain(incluirServicos.execute(numeroOs, servicos, userDetails.getUsername()));
    }

    @PatchMapping("/{numeroOs}/mecanico")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public OrdemServicoResponse atribuirMecanico(
            @PathVariable String numeroOs,
            @Valid @RequestBody IncluirMecanicoRequest request) {
        return OrdemServicoResponse.fromDomain(atribuirMecanico.execute(numeroOs, request.mecanicoId(), request.mecanicoEmail()));
    }
}
