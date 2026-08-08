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
    static final ArchRule repositorioNaoAcessaController =
        noClasses()
            .that().resideInAPackage("..repository..")
            .should().dependOnClassesThat()
            .resideInAPackage("..controller..")
            .because("repositories nao devem depender de controllers");

    @ArchTest
    static final FreezingArchRule dominioNaoUsaSpring =
        freeze(noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..")
            .because("dominio nao deve depender do Spring — violacao conhecida: "
                + "OrdemServicoEntity usa ResponseStatusException"));

    @ArchTest
    static final FreezingArchRule dominioNaoUsaJpa =
        freeze(noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("jakarta.persistence..")
            .because("dominio nao deve depender de JPA — violacoes conhecidas serao corrigidas "
                + "incrementalmente por componente conforme ADR-001"));

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
    static final FreezingArchRule infrastructureNaoAcessaPresentation =
        freeze(noClasses()
            .that().resideInAPackage("..infrastructure..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..presentation..", "..controller..")
            .because("infrastructure nao deve depender de contratos de entrada"));
}
