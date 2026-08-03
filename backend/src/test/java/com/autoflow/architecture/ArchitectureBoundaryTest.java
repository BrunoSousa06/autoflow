package com.autoflow.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.freeze.FreezingArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

/**
 * Testes de fronteira arquitetural.
 *
 * Regras com FreezingArchRule registram violacoes existentes na primeira execucao
 * e falham apenas para violacoes novas. Isso permite detectar regressoes sem
 * bloquear o pipeline por debito tecnico ja conhecido.
 *
 * Apos a primeira execucao bem-sucedida, commitar o diretorio
 * src/test/resources/archunit_store/ junto com este teste.
 *
 * Violacoes existentes conhecidas:
 * - OrdemServicoEntity importa org.springframework.http (dominio->Spring Web)
 * - Entidades de dominio importam jakarta.persistence (dominio->JPA)
 * - OrdemServicoService interface importa com.autoflow.controller (service->controller)
 */
@AnalyzeClasses(
        packages = "com.autoflow",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureBoundaryTest {

    @ArchTest
    static final ArchRule controllerNaoAcessaRepositorioDiretamente =
        noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat()
            .resideInAPackage("..repository..")
            .because("controllers devem acessar dados somente via services");

    @ArchTest
    static final ArchRule repositorioNaoAcessaController =
        noClasses()
            .that().resideInAPackage("..repository..")
            .should().dependOnClassesThat()
            .resideInAPackage("..controller..")
            .because("repositories nao devem depender de controllers");

    @ArchTest
    static final FreezingArchRule serviceNaoAcessaController =
        freeze(noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat()
            .resideInAPackage("..controller..")
            .because("services nao devem depender de controllers — violacao conhecida: "
                + "OrdemServicoService usa tipos de request/response do pacote controller"));

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
}
