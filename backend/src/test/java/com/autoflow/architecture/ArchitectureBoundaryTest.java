package com.autoflow.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat;
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
    private static final String PERSISTENCE = "com.autoflow.infrastructure.persistence..";
    private static final String PORTS = "com.autoflow.application.gateway..";
    private static final String INPUT_PORTS = "com.autoflow.application.port.in..";
    private static final String USE_CASES = "com.autoflow.application.usecases..";
    private static final String[] TECHNICAL = {INFRASTRUCTURE, CONFIGURATION};
    private static final DescribedPredicate<? super JavaClass> JPA_TYPE =
            annotatedWith(Entity.class)
                    .or(annotatedWith(Embeddable.class))
                    .or(annotatedWith(MappedSuperclass.class));
    private static final DescribedPredicate<? super JavaClass> WEB_ADAPTER =
            annotatedWith(RestController.class)
                    .or(annotatedWith(Controller.class))
                    .or(annotatedWith(RestControllerAdvice.class))
                    .or(annotatedWith(ControllerAdvice.class));

    @ArchTest
    static final ArchRule dependenciasSeguemParaDentro =
            noClasses()
                    .that().resideInAnyPackage(APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(INFRASTRUCTURE, PRESENTATION, CONFIGURATION)
                    .because("a aplicacao deve depender somente de responsabilidades internas");

    @ArchTest
    static final ArchRule dominioNaoDependeDeCamadasExternas =
            noClasses()
                    .that().resideInAnyPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(APPLICATION, INFRASTRUCTURE, PRESENTATION, CONFIGURATION)
                    .because("o dominio e o nucleo interno e nao conhece camadas externas");

    @ArchTest
    static final ArchRule dominioNaoDependeDeFrameworksTecnicos =
            noClasses()
                    .that().resideInAnyPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta..",
                            "org.hibernate..",
                            "lombok..")
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
                    .resideInAnyPackage(TECHNICAL)
                    .because("a aplicacao depende de portas, nunca de implementacoes tecnicas");

    @ArchTest
    static final ArchRule aplicacaoNaoDependeDeEntidadesDePersistencia =
            noClasses()
                    .that().resideInAnyPackage(APPLICATION)
                    .should(dependOnClassesThat(JPA_TYPE))
                    .because("contratos e casos de uso nao manipulam entidades JPA");

    @ArchTest
    static final ArchRule aplicacaoNaoDependeDeDetalhesHttpOuSeguranca =
            noClasses()
                    .that().resideInAnyPackage(APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web..",
                            "org.springframework.http..",
                            "jakarta.servlet..",
                            "org.springframework.security.core.userdetails..")
                    .because("a aplicacao recebe comandos e identidade por portas, sem conhecer HTTP ou UserDetails");

    @ArchTest
    static final ArchRule apresentacaoNaoAcessaDetalhesTecnicos =
            noClasses()
                    .that().resideInAnyPackage(PRESENTATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(TECHNICAL)
                    .because("controllers delegam persistencia e integracoes aos casos de uso");

    @ArchTest
    static final ArchRule apresentacaoNaoDependeDeEntidadesJpa =
            noClasses()
                    .that().resideInAnyPackage(PRESENTATION)
                    .should(dependOnClassesThat(JPA_TYPE))
                    .because("contratos HTTP nao expoem entidades de persistencia");

    @ArchTest
    static final ArchRule apresentacaoNaoDependeDeRepositoriosSpringData =
            noClasses()
                    .that().resideInAnyPackage(PRESENTATION)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("com.autoflow.infrastructure.persistence.repository..")
                    .because("controllers nao acessam repositorios Spring Data");

    @ArchTest
    static final ArchRule controllersNaoAcessamRepositorios =
            noClasses()
                    .that(WEB_ADAPTER)
                    .should().dependOnClassesThat()
                    .areAnnotatedWith(Repository.class)
                    .because("controllers delegam acesso a dados aos casos de uso");

    @ArchTest
    static final ArchRule infrastructureNaoDependeDeApresentacao =
            noClasses()
                    .that().resideInAnyPackage(TECHNICAL)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(PRESENTATION)
                    .because("adapters tecnicos nao conhecem contratos de entrada HTTP");

    @ArchTest
    static final ArchRule portasDeEntradaSaoInterfaces =
            classes()
                    .that().resideInAnyPackage(INPUT_PORTS)
                    .and().haveSimpleNameEndingWith("UseCase")
                    .should().beInterfaces()
                    .because("portas de entrada sao contratos internos da aplicacao");

    @ArchTest
    static final ArchRule implementacoesDeCasosDeUsoImplementamPortas =
            classes()
                    .that().resideInAnyPackage(USE_CASES)
                    .and().haveSimpleNameEndingWith("UseCaseImpl")
                    .should().implement(resideInAnyPackage(INPUT_PORTS))
                    .because("implementacoes de casos de uso devem realizar portas de entrada");

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
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .should().resideInAnyPackage(PERSISTENCE)
                    .because("entidades e componentes JPA pertencem ao adapter de persistencia");

    @ArchTest
    static final ArchRule repositoriosFicamNaInfraestrutura =
            classes()
                    .that().areAnnotatedWith(Repository.class)
                    .should().resideInAnyPackage(PERSISTENCE)
                    .because("repositorios tecnicos pertencem a infraestrutura");

    @ArchTest
    static final ArchRule controllersFicamNaApresentacao =
            classes()
                    .that(WEB_ADAPTER)
                    .should().resideInAnyPackage(PRESENTATION)
                    .because("controllers REST sao adapters de apresentacao");

    @ArchTest
    static final ArchRule camadasNaoFormamCiclos =
            slices()
                    .matching("com.autoflow.(*)..")
                    .should().beFreeOfCycles()
                    .because("camadas e pacotes relevantes devem permanecer aciclicos");
}
