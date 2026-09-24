package com.autoflow.presentation.ordemservico.acompanhamento;

import com.autoflow.application.output.ordemservico.acompanhamento.AcompanhamentoPublicoOutput;
import com.autoflow.application.port.in.ordemservico.acompanhamento.ConsultarAcompanhamentoPublicoUseCase;
import com.autoflow.presentation.ordemservico.acompanhamento.response.AcompanhamentoPublicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/ordens-servico")
@RequiredArgsConstructor
@Tag(
        name = "acompanhamento público",
        description = "Consulta pública do acompanhamento de ordens de serviço"
)
public class PublicAcompanhamentoController {

    private final ConsultarAcompanhamentoPublicoUseCase consultarAcompanhamentoPublicoUseCase;

    @Operation(
            summary = "Consultar acompanhamento público",
            description = """
                    Consulta o status público de uma ordem de serviço por meio
                    do token de acompanhamento
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Acompanhamento encontrado"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Token não informado"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Token inválido, expirado ou revogado"
    )
    @GetMapping("/acompanhamento")
    public AcompanhamentoPublicoResponse consultar(
            @RequestParam String token
    ) {
        AcompanhamentoPublicoOutput output =
                consultarAcompanhamentoPublicoUseCase.execute(token);

        return AcompanhamentoPublicoResponse.from(output);
    }
}
