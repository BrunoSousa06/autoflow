package com.autoflow.controller.ordemservico;

import com.autoflow.application.dto.ordemservico.TempoMedioOrdemServicoOutput;
import com.autoflow.application.dto.ordemservico.OrdemServicoCriadaOutput;
import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.usecases.ordemservico.DetalharOrdemServicoUseCase;
import com.autoflow.application.usecases.ordemservico.ListarOrdensServicoUseCase;
import com.autoflow.application.usecases.ordemservico.IniciarDiagnosticoUseCase;
import com.autoflow.application.usecases.ordemservico.RegistrarItensNecessariosUseCase;
import com.autoflow.application.usecases.ordemservico.RegistrarLaudoUseCase;
import com.autoflow.application.usecases.ordemservico.IniciarServicoUseCase;
import com.autoflow.application.usecases.ordemservico.FinalizarServicoUseCase;
import com.autoflow.application.usecases.ordemservico.EntregarOrdemServicoUseCase;
import com.autoflow.application.usecases.ordemservico.FinalizarDiagnosticoUseCase;
import com.autoflow.service.ordemservico.CriarOrdemServicoUseCase;
import com.autoflow.application.usecases.ordemservico.IncluirServicosUseCase;
import com.autoflow.application.usecases.ordemservico.AtribuirMecanicoUseCase;
import com.autoflow.application.usecases.ordemservico.CalcularTempoMedioOrdemServicoUseCase;
import com.autoflow.controller.ordemservico.request.*;
import com.autoflow.controller.ordemservico.response.FinalizarDiagnosticoResponse;
import com.autoflow.controller.ordemservico.response.OrdemServicoDetalheResponse;
import com.autoflow.controller.ordemservico.response.OrdemServicoResponse;
import com.autoflow.controller.ordemservico.response.TempoMedioOrdemServicoResponse;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.infrastructure.persistence.mapper.ItensNecessariosMapper;
import com.autoflow.infrastructure.persistence.mapper.ServicoSolicitadoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens-servico")
@Tag(name = "ordens de serviço", description = "Endpoints para gerenciamento das ordens de serviço")
@SecurityRequirement(name = "bearerAuth")
public class OrdemServicoController {

    private final ServicoSolicitadoMapper servicoSolicitadoMapper;
    private final ItensNecessariosMapper itensNecessariosMapper;
    private final CalcularTempoMedioOrdemServicoUseCase calcularTempoMedioOrdemServicoUseCase;
    private final ListarOrdensServicoUseCase listarOrdensServicoUseCase;
    private final DetalharOrdemServicoUseCase detalharOrdemServicoUseCase;
    private final IniciarDiagnosticoUseCase iniciarDiagnosticoUseCase;
    private final RegistrarItensNecessariosUseCase registrarItensNecessariosUseCase;
    private final RegistrarLaudoUseCase registrarLaudoUseCase;
    private final IniciarServicoUseCase iniciarServicoUseCase;
    private final FinalizarServicoUseCase finalizarServicoUseCase;
    private final EntregarOrdemServicoUseCase entregarOrdemServicoUseCase;
    private final FinalizarDiagnosticoUseCase finalizarDiagnosticoUseCase;
    private final CriarOrdemServicoUseCase criarOrdemServicoUseCase;
    private final IncluirServicosUseCase incluirServicosUseCase;
    private final AtribuirMecanicoUseCase atribuirMecanicoUseCase;

    @Value("${app.frontend-public-base-url}")
    private String frontendPublicBaseUrl;

    public OrdemServicoController(
                                  ServicoSolicitadoMapper servicoSolicitadoMapper,
                                  ItensNecessariosMapper itensNecessariosMapper,
                                  CalcularTempoMedioOrdemServicoUseCase calcularTempoMedioOrdemServicoUseCase,
                                  ListarOrdensServicoUseCase listarOrdensServicoUseCase,
                                  DetalharOrdemServicoUseCase detalharOrdemServicoUseCase,
                                  IniciarDiagnosticoUseCase iniciarDiagnosticoUseCase,
                                  RegistrarItensNecessariosUseCase registrarItensNecessariosUseCase,
                                  RegistrarLaudoUseCase registrarLaudoUseCase,
                                  IniciarServicoUseCase iniciarServicoUseCase,
                                  FinalizarServicoUseCase finalizarServicoUseCase,
                                  EntregarOrdemServicoUseCase entregarOrdemServicoUseCase,
                                  FinalizarDiagnosticoUseCase finalizarDiagnosticoUseCase,
                                  CriarOrdemServicoUseCase criarOrdemServicoUseCase,
                                  IncluirServicosUseCase incluirServicosUseCase,
                                  AtribuirMecanicoUseCase atribuirMecanicoUseCase) {
        this.servicoSolicitadoMapper = servicoSolicitadoMapper;
        this.itensNecessariosMapper = itensNecessariosMapper;
        this.calcularTempoMedioOrdemServicoUseCase = calcularTempoMedioOrdemServicoUseCase;
        this.listarOrdensServicoUseCase = listarOrdensServicoUseCase;
        this.detalharOrdemServicoUseCase = detalharOrdemServicoUseCase;
        this.iniciarDiagnosticoUseCase = iniciarDiagnosticoUseCase;
        this.registrarItensNecessariosUseCase = registrarItensNecessariosUseCase;
        this.registrarLaudoUseCase = registrarLaudoUseCase;
        this.iniciarServicoUseCase = iniciarServicoUseCase;
        this.finalizarServicoUseCase = finalizarServicoUseCase;
        this.entregarOrdemServicoUseCase = entregarOrdemServicoUseCase;
        this.finalizarDiagnosticoUseCase = finalizarDiagnosticoUseCase;
        this.criarOrdemServicoUseCase = criarOrdemServicoUseCase;
        this.incluirServicosUseCase = incluirServicosUseCase;
        this.atribuirMecanicoUseCase = atribuirMecanicoUseCase;
    }

    @Operation(summary = "Criar a ordem de serviço", description = "Cria uma nova ordem de serviço identificando o cliente por CPF/CNPJ e buscando ou cadastrando o veiculo pela placa")
    @ApiResponse(responseCode = "201", description = "Ordem de serviço criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados obrigatórios não informados ou inválidos")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @ApiResponse(responseCode = "409", description = "Placa ja cadastrada para outro cliente")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public OrdemServicoResponse criar(
            @Valid @RequestBody CriarOrdemServicoRequest request
    ) {
        List<ServicoSolicitadoEntity> servicos =
                servicoSolicitadoMapper.mapToEntities(
                        request.servicosSolicitados()
                );

        OrdemServicoCriadaOutput osCriada = criarOrdemServicoUseCase.execute(
                request.cpfCnpj(),
                new VeiculoOrdemServicoInput(
                        request.veiculo().placa(),
                        request.veiculo().marca(),
                        request.veiculo().modelo(),
                        request.veiculo().ano()),
                servicos
        );

        String acompanhamentoUrl = frontendPublicBaseUrl
                + "/public/acompanhamento?token="
                + osCriada.tokenAcompanhamento();

        return OrdemServicoResponse.fromDomain(
                osCriada,
                acompanhamentoUrl
        );
    }

    @Operation(summary = "Incluir serviço na ordem de serviço", description = "Adiciona novos serviços a uma ordem de serviço existente")
    @ApiResponse(responseCode = "202", description = "Serviços incluídos com sucesso")
    @ApiResponse(responseCode = "404", description = "Veiculo pertencente a ordem de serviço não foi encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PostMapping("/{numeroOs}/servicos")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN', 'MECANICO')")
    public OrdemServicoResponse incluirServico(
            @PathVariable String numeroOs,
            @Valid @RequestBody List<ServicoSolicitadoRequest> request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ServicoSolicitadoEntity> servicos = servicoSolicitadoMapper.mapToEntities(request);
        return OrdemServicoResponse.fromDomain(incluirServicosUseCase.execute(numeroOs, servicos, userDetails.getUsername()));
    }

    @Operation(
            summary = "Atribuir mecânico à ordem de serviço",
            description = "Define o mecânico responsável pela execução da ordem de serviço."
    )
    @ApiResponse(responseCode = "202", description = "Mecânico atribuído com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço ou mecânico não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{numeroOs}/mecanico")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMIN')")
    public OrdemServicoResponse atribuirMecanico(
            @PathVariable String numeroOs,
            @Valid @RequestBody IncluirMecanicoRequest request){
        return OrdemServicoResponse.fromDomain(atribuirMecanicoUseCase.execute(
                numeroOs,
                request.mecanicoId(),
                request.mecanicoEmail()));
    }

    @Operation(
            summary = "Iniciar diagnóstico",
            description = "Inicia a etapa de diagnóstico da ordem de serviço."
    )
    @ApiResponse(responseCode = "202", description = "Diagnóstico iniciado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{numeroOs}/diagnostico/iniciar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse iniciarDiagnostico(@PathVariable String numeroOs, @AuthenticationPrincipal UserDetails userDetails){
        return OrdemServicoResponse.fromDomain(iniciarDiagnosticoUseCase.execute(
                numeroOs,
                userDetails.getUsername()));
    }

    @Operation(
            summary = "Registrar itens necessários",
            description = "Registra os itens, peças e materiais necessários para execução de um serviço da ordem de serviço."
    )
    @ApiResponse(responseCode = "202", description = "Itens registrados com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço ou serviço não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{numeroOs}/servicos/{servicoId}/itens-necessarios")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse registrarItensDoServico(
            @PathVariable String numeroOs,
            @PathVariable Long servicoId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody List<ItensNecessariosRequest> request
    ) {
        return OrdemServicoResponse.fromDomain(
                registrarItensNecessariosUseCase.execute(
                        numeroOs,
                        servicoId,
                        userDetails.getUsername(),
                        itensNecessariosMapper.mapToEntities(request)
                )
        );
    }

    @Operation(
            summary = "Registrar laudo do diagnóstico",
            description = "Registra ou atualiza o laudo técnico produzido durante o diagnóstico da ordem de serviço."
    )
    @ApiResponse(responseCode = "202", description = "Laudo registrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{numeroOs}/diagnostico/laudo")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse atualizarDiagnostico(
            @PathVariable String numeroOs,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegistrarLaudoRequest request
    ){
        return OrdemServicoResponse.fromDomain(registrarLaudoUseCase.execute(
                numeroOs,
                userDetails.getUsername(),
                request.laudo()
        ));
    }

    @Operation(
            summary = "Finalizar diagnóstico",
            description = "Finaliza a etapa de diagnóstico e retorna o resultado consolidado do diagnóstico."
    )
    @ApiResponse(responseCode = "202", description = "Diagnóstico finalizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{numeroOs}/diagnostico/finalizar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public FinalizarDiagnosticoResponse finalizarDiagnostico(
            @PathVariable String numeroOs,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        return FinalizarDiagnosticoResponse.from(finalizarDiagnosticoUseCase.execute(
                numeroOs,
                userDetails.getUsername()
        ));
    }

    @Operation(
            summary = "Iniciar serviço",
            description = "Altera o status de um serviço da ordem de serviço para EM_EXECUCAO."
    )
    @ApiResponse(responseCode = "202", description = "Serviço iniciado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço ou serviço não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{numeroOs}/servicos/{servicoId}/iniciar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse iniciarServico(
            @PathVariable String numeroOs,
            @PathVariable Long servicoId
    ) {
        return OrdemServicoResponse.fromDomain(
                iniciarServicoUseCase.execute(numeroOs, servicoId)
        );
    }

    @Operation(
            summary = "Finalizar serviço",
            description = "Conclui a execução de um serviço da ordem de serviço e altera seu status para FINALIZADO."
    )
    @ApiResponse(responseCode = "202", description = "Serviço finalizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço ou serviço não encontrado")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{numeroOs}/servicos/{servicoId}/finalizar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MECANICO')")
    public OrdemServicoResponse finalizarServico(
            @PathVariable String numeroOs,
            @PathVariable Long servicoId
    ) {
        return OrdemServicoResponse.fromDomain(
                finalizarServicoUseCase.execute(numeroOs, servicoId)
        );
    }

    @Operation(
            summary = "Entregar Ordem de Servico",
            description = "Entrega a ordem de Servico e altera seu status para ENTREGUE."
    )
    @ApiResponse(responseCode = "202", description = "Ordem de serviço entregue com sucesso")
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @PatchMapping("/{numeroOs}/entregar")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE')")
    public OrdemServicoResponse entregar(
            @PathVariable String numeroOs
    ) {
        return OrdemServicoResponse.fromDomain(
                entregarOrdemServicoUseCase.execute(numeroOs)
        );
    }

    @Operation(
            summary = "Listar ordens de serviço",
            description = "Lista as ordens de serviço com suporte a paginação e filtragem por cliente (nome/CPF/CNPJ), número da OS e status. Ordenação padrão por data de abertura decrescente."
    )
    @ApiResponse(responseCode = "200", description = "Ordens de serviço listadas com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    public Page<OrdemServicoResponse> listar(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String numeroOs,
            @RequestParam(required = false) StatusOrdemServico status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        OrdemServicoFiltroInput filtro = new OrdemServicoFiltroInput(cliente, numeroOs, status);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataAbertura"));
        return listarOrdensServicoUseCase.execute(filtro, pageable, userDetails.getUsername()).map(OrdemServicoResponse::fromDomain);
    }

    @Operation(
            summary = "Detalhar ordem de serviço",
            description = "Retorna os dados completos da ordem de serviço, incluindo cliente, veículo, serviços, peças, orçamento atual e status."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ordem de serviço encontrada",
            content = @Content(schema = @Schema(implementation = OrdemServicoDetalheResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping("/{numeroOs}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENDENTE', 'MECANICO')")
    public OrdemServicoDetalheResponse detalhar(@PathVariable String numeroOs) {
        var detalhe = detalharOrdemServicoUseCase.execute(numeroOs);
        return OrdemServicoDetalheResponse.fromDomain(detalhe.ordemServico(), detalhe.orcamentoAtual());
    }

    @Operation(
            summary = "Consultar tempo médio de finalização das ordens de serviço",
            description = "Retorna o tempo médio de execução das ordens de serviço finalizadas ou entregues."
    )
    @ApiResponse(responseCode = "200", description = "Tempo médio de finalização consultado com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para executar a operação")
    @GetMapping("/metricas/tempo-medio")
    @PreAuthorize("hasRole('ADMIN')")
    public TempoMedioOrdemServicoResponse calcularTempoMedioFinalizacao() {
        TempoMedioOrdemServicoOutput output = calcularTempoMedioOrdemServicoUseCase.execute();
        return new TempoMedioOrdemServicoResponse(
                output.quantidadeOrdensFinalizadas(),
                output.tempoMedioSegundos(),
                output.tempoMedioMinutos(),
                output.tempoMedioHoras()
        );
    }
}
