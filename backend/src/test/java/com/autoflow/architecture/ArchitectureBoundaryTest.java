package com.autoflow.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

/**
 * Testes de fronteira arquitetural. Violações congeladas só representam os
 * limites de JPA e Spring ainda isolados no modelo legado; o store não aceita
 * novas ocorrências.
 */
@AnalyzeClasses(
        packages = "com.autoflow",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureBoundaryTest {

    @ArchTest
    static final ArchRule presentationNaoAcessaRepositorioDiretamente =
        noClasses()
                .that().resideInAPackage("..presentation..")
            .should().dependOnClassesThat()
            .resideInAPackage("..repository..")
                .because("controllers devem acessar dados somente via casos de uso")
                .allowEmptyShould(true);

    @ArchTest
    static final ArchRule presentationDoServicoNaoAcessaDetalhesExternos =
        noClasses()
            .that().resideInAPackage("com.autoflow.presentation.servico..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.autoflow.infrastructure..", "..repository..")
            .because("a borda REST do piloto deve delegar aos casos de uso");

    @ArchTest
    static final ArchRule repositorioNaoAcessaController =
        noClasses()
            .that().resideInAPackage("..repository..")
            .should().dependOnClassesThat()
            .resideInAPackage("..controller..")
            .because("repositories nao devem depender de controllers");

    @ArchTest
    static final ArchRule dominioNaoUsaSpring =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..")
            .because("dominio nao deve depender do Spring");

    @ArchTest
    static final FreezingArchRule dominioNaoUsaJpa =
        freeze(noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("jakarta.persistence..")
            .because("dominio nao deve depender de JPA — violacoes conhecidas serao corrigidas "
                + "incrementalmente por componente conforme ADR-001"));

    @ArchTest
    static final ArchRule dominioDoPilotoNaoUsaBordas =
        noClasses()
            .that().resideInAPackage("com.autoflow.domain.servico..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.autoflow.infrastructure..",
                    "com.autoflow.presentation..",
                    "org.springframework..",
                    "jakarta.persistence..",
                    "org.hibernate..")
            .because("o modelo de dominio do piloto deve ser independente das bordas");

    @ArchTest
    static final ArchRule applicationNaoAcessaContratosExternos =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.autoflow.controller..",
                    "com.autoflow.service..",
                    "com.autoflow.repository..")
                .because("application deve depender de domain e portas internas; contratos externos ficam na presentation");

    @ArchTest
    static final ArchRule applicationDoPilotoNaoAcessaInfraestrutura =
        noClasses()
            .that().resideInAnyPackage(
                    "com.autoflow.application.usecases.servico..",
                    "com.autoflow.application.mapper..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.autoflow.infrastructure..", "com.autoflow.presentation..")
            .because("a aplicacao do piloto deve depender apenas de dominio e portas");

    @ArchTest
    static final ArchRule gatewaysNaoExponhamDetalhesExternos =
        noClasses()
            .that().resideInAPackage("com.autoflow.application.gateway..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.autoflow.infrastructure.persistence..",
                    "com.autoflow.presentation..",
                    "jakarta.persistence..",
                    "org.springframework.data..")
            .because("gateways sao portas internas e nao podem expor persistencia ou REST");

    @ArchTest
    static final ArchRule applicationDoPilotoNaoAcessaEntidadeJpa =
        noClasses()
            .that().resideInAPackage("com.autoflow.application.usecases.servico..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure.persistence.entity..", "..repository..");

    @ArchTest
    static final ArchRule applicationClienteNaoAcessaInfraestrutura =
        noClasses()
            .that().resideInAPackage("com.autoflow.application.usecases.cliente..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.autoflow.infrastructure..",
                    "com.autoflow.presentation..",
                    "com.autoflow.controller..",
                    "com.autoflow.repository..",
                    "org.springframework.data..",
                    "jakarta.persistence..")
            .because("os casos de uso de cliente devem depender somente de DTOs internos e gateways");

    @ArchTest
    static final ArchRule presentationClienteNaoAcessaPersistencia =
        noClasses()
            .that().resideInAPackage("com.autoflow.presentation.cliente..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.autoflow.infrastructure.persistence..", "..repository..")
            .because("o controller de cliente deve acessar persistencia somente por casos de uso");

    @ArchTest
    static final ArchRule applicationVeiculoNaoAcessaInfraestrutura =
        noClasses()
            .that().resideInAPackage("com.autoflow.application.usecases.veiculo..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.autoflow.infrastructure..",
                    "com.autoflow.presentation..",
                    "com.autoflow.controller..",
                    "com.autoflow.repository..",
                    "org.springframework.security.core.context..",
                    "jakarta.persistence..")
            .because("os casos de uso de veiculo devem usar gateways e policies internas");

    @ArchTest
    static final ArchRule presentationVeiculoNaoAcessaPersistencia =
        noClasses()
            .that().resideInAPackage("com.autoflow.presentation.veiculo..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.autoflow.infrastructure.persistence..", "..repository..")
            .because("o controller de veiculo deve delegar persistencia aos casos de uso");

    @ArchTest
    static final ArchRule contextoDeSegurancaFicaNaInfraestrutura =
        noClasses()
            .that().resideOutsideOfPackages("com.autoflow.infrastructure.security..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework.security.core.context..")
            .because("SecurityContextHolder deve ser acessado somente pelo adapter de infraestrutura");

    @ArchTest
    static final ArchRule policyDeEstoqueNaoUsaFrameworks =
        noClasses()
            .that().haveSimpleName("EstoquePolicy")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.autoflow.infrastructure..",
                    "com.autoflow.presentation..",
                    "org.springframework..",
                    "jakarta.persistence..",
                    "org.hibernate..")
            .because("a classificacao de estoque deve ser uma regra pura de dominio");

    @ArchTest
    static final ArchRule dominioDePecaInsumoNaoUsaJpa =
        noClasses()
            .that().resideInAPackage("com.autoflow.domain.pecainsumo..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("jakarta.persistence..", "org.hibernate..", "com.autoflow.infrastructure..");

    @ArchTest
    static final ArchRule dominioDeClienteNaoUsaBordas =
            noClasses()
                    .that().resideInAPackage("com.autoflow.domain.cliente..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "com.autoflow.infrastructure..",
                            "com.autoflow.presentation..",
                            "org.springframework..",
                            "jakarta.persistence..",
                            "org.hibernate..")
                    .because("o modelo de cliente deve ser independente das bordas");

    @ArchTest
    static final ArchRule casosDeEstoqueNaoAcessamInfraestrutura =
        noClasses()
            .that().resideInAPackage("com.autoflow.application.usecases.pecainsumo..")
            .and().haveSimpleName("ConsultarDisponibilidadeEstoqueUseCase")
            .or().haveSimpleName("BaixarEstoqueUseCase")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.autoflow.infrastructure..",
                    "com.autoflow.presentation..",
                    "com.autoflow.controller..",
                    "com.autoflow.repository..",
                    "org.springframework.data..",
                    "jakarta.persistence..")
            .because("a aplicacao de estoque deve depender do gateway e da policy");

    @ArchTest
    static final ArchRule applicationPecaInsumoNaoAcessaPersistencia =
        noClasses()
            .that().resideInAPackage("com.autoflow.application.usecases.pecainsumo..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.autoflow.infrastructure.persistence..",
                    "com.autoflow.presentation..",
                    "com.autoflow.repository..",
                    "jakarta.persistence..",
                    "org.springframework.data..")
            .because("casos de uso de pecas e insumos nao devem expor JPA ou Spring Data");

    @ArchTest
    static final ArchRule presentationPecaInsumoNaoAcessaPersistencia =
        noClasses()
            .that().resideInAPackage("com.autoflow.presentation.pecainsumo..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.autoflow.infrastructure.persistence..", "..repository..")
            .because("a Presentation de pecas e insumos deve delegar aos casos de uso");

    @ArchTest
    static final ArchRule infrastructureNaoAcessaPresentation =
        noClasses()
            .that().resideInAPackage("..infrastructure..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..presentation..", "..controller..")
            .because("infrastructure nao deve depender de contratos de entrada");
}
