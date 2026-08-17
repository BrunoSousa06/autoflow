package com.autoflow.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Regras globais das fronteiras arquiteturais. Os predicados usam somente
 * responsabilidades, camadas e convencoes que se aplicam a todo o modulo.
 */
@AnalyzeClasses(
        packages = "com.autoflow",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureBoundaryTest {

    private static final String DOMAIN = "com.autoflow.domain..";
    private static final String APPLICATION = "com.autoflow.application..";
    private static final String INFRASTRUCTURE = "com.autoflow.infrastructure..";
    private static final String PRESENTATION = "com.autoflow.presentation..";
    private static final String CONFIGURATION = "com.autoflow.config..";
    private static final String PERSISTENCE = "com.autoflow.infrastructure.persistence.entity..";
    private static final String PORTS = "com.autoflow.application.gateway..";

    @ArchTest
    static final ArchRule dependenciasSeguemParaDentro =
            noClasses()
                    .that().resideInAnyPackage(DOMAIN, APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(INFRASTRUCTURE, PRESENTATION, CONFIGURATION)
                    .because("dominio e aplicacao devem depender somente de responsabilidades internas");

    @ArchTest
    static final ArchRule dominioNaoDependeDeFrameworksTecnicos =
            noClasses()
                    .that().resideInAnyPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "org.springframework.data..",
                            "org.springframework.security..",
                            "jakarta.persistence..",
                            "jakarta.validation..",
                            "org.hibernate..")
                    .because("o dominio deve ser Java puro e independente de frameworks");

    @ArchTest
    static final ArchRule aplicacaoNaoDependeDeApresentacao =
            noClasses()
                    .that().resideInAnyPackage(APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(PRESENTATION)
                    .because("a aplicacao nao conhece requests, responses ou controllers");

    @ArchTest
    static final ArchRule aplicacaoNaoDependeDeImplementacoesExternas =
            noClasses()
                    .that().resideInAnyPackage(APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(INFRASTRUCTURE, CONFIGURATION)
                    .because("a aplicacao depende de portas, nunca de implementacoes tecnicas");

    @ArchTest
    static final ArchRule aplicacaoNaoDependeDeEntidadesDePersistencia =
            noClasses()
                    .that().resideInAnyPackage(APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(PERSISTENCE)
                    .because("contratos e casos de uso nao manipulam entidades JPA");

    @ArchTest
    static final ArchRule apresentacaoNaoAcessaDetalhesTecnicos =
            noClasses()
                    .that().resideInAnyPackage(PRESENTATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(INFRASTRUCTURE)
                    .because("controllers delegam persistencia e integracoes aos casos de uso");

    @ArchTest
    static final ArchRule apresentacaoNaoDependeDeEntidadesDePersistencia =
            noClasses()
                    .that().resideInAnyPackage(PRESENTATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(PERSISTENCE)
                    .because("contratos HTTP nao expoem entidades de persistencia");

    @ArchTest
    static final ArchRule infrastructureNaoDependeDeApresentacao =
            noClasses()
                    .that().resideInAnyPackage(INFRASTRUCTURE)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(PRESENTATION)
                    .because("adapters tecnicos nao conhecem contratos de entrada HTTP");

    @ArchTest
    static final ArchRule portasDeSaidaSaoDefinidasNaAplicacao =
            classes()
                    .that().resideInAnyPackage(PORTS)
                    .and().haveSimpleNameEndingWith("Gateway")
                    .should().beInterfaces()
                    .because("portas de saida sao contratos internos da aplicacao");

    @ArchTest
    static final ArchRule adaptersImplementamPortasDeSaida =
            classes()
                    .that().resideInAnyPackage(INFRASTRUCTURE)
                    .and().haveSimpleNameEndingWith("Adapter")
                    .should().implement(resideInAnyPackage(PORTS))
                    .because("adapters externos implementam portas definidas internamente");

    @ArchTest
    static final ArchRule entidadesJpaFicamNaPersistencia =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .should().resideInAnyPackage(PERSISTENCE)
                    .because("entidades e componentes JPA pertencem ao adapter de persistencia");

    @ArchTest
    static final ArchRule controllersFicamNaApresentacao =
            classes()
                    .that().areAnnotatedWith(RestController.class)
                    .should().resideInAnyPackage(PRESENTATION)
                    .because("controllers REST sao adapters de apresentacao");

    @ArchTest
    static final ArchRule camadasNaoFormamCiclos =
            slices()
                    .matching("com.autoflow.(*)..")
                    .should().beFreeOfCycles()
                    .because("camadas e pacotes relevantes devem permanecer aciclicos");
}
