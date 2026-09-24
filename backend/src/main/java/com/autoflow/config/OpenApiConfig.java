package com.autoflow.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AutoFlow API",
                version = "v1",
                description = "Documentação da API AutoFlow"
        ),
        tags = {
                @Tag(name = "autenticação", description = "Endpoints para gerenciamento da autenticação dos usuários"),
                @Tag(name = "usuários", description = "Endpoints para gerenciamento de usuários do sistema"),
                @Tag(name = "clientes", description = "Endpoints para gerenciamento de clientes"),
                @Tag(name = "ordens de serviço do cliente", description = "Endpoints para gerenciamento das ordens de serviço do cliente autenticado"),
                @Tag(name = "veículos", description = "Endpoints para gerenciamento de veículos"),
                @Tag(name = "serviços", description = "Endpoints para gerenciamento dos serviços"),
                @Tag(name = "peças e insumos", description = "Endpoints para gerenciamento das peças e insumos"),
                @Tag(name = "ordens de serviço", description = "Endpoints para consulta, criação e administração de ordens de serviço"),
                @Tag(name = "diagnóstico", description = "Endpoints para diagnóstico das ordens de serviço"),
                @Tag(name = "execução", description = "Endpoints para execução e entrega das ordens de serviço"),
                @Tag(name = "reparos adicionais", description = "Endpoints para gerenciamento dos reparos adicionais da ordem de serviço"),
                @Tag(name = "orçamentos", description = "Endpoints para gerenciamento de orçamentos autenticados"),
                @Tag(name = "orçamentos públicos", description = "Endpoints para consulta e decisão de orçamentos pelo link público"),
                @Tag(name = "acompanhamento público", description = "Consulta pública do acompanhamento de ordens de serviço")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
