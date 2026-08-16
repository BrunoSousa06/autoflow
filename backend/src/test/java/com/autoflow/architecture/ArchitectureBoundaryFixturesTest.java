package com.autoflow.architecture;

import com.autoflow.application.gateway.fixture.IllegalGatewayPersistenceDependency;
import com.autoflow.application.usecases.servico.fixture.IllegalApplicationInfrastructureDependency;
import com.autoflow.application.usecases.cliente.fixture.IllegalClienteApplicationInfrastructureDependency;
import com.autoflow.domain.servico.fixture.IllegalDomainInfrastructureDependency;
import com.autoflow.domain.servico.fixture.IllegalDomainSpringDependency;
import com.autoflow.infrastructure.persistence.adapters.fixture.IllegalInfrastructurePresentationDependency;
import com.autoflow.presentation.cliente.fixture.IllegalClientePresentationRepositoryDependency;
import com.autoflow.presentation.servico.fixture.IllegalPresentationRepositoryDependency;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArchitectureBoundaryFixturesTest {

    @Test
    void dominioNaoPodeDependerDeSpringOuJpa() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain.servico.fixture..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..");

        assertThrows(AssertionError.class, () -> rule.check(importFixtures(IllegalDomainSpringDependency.class)));
    }

    @Test
    void dominioNaoPodeDependerDeInfraestrutura() {
        ArchRule rule = noClasses().that().resideInAPackage("..domain.servico.fixture..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..");

        assertThrows(AssertionError.class, () -> rule.check(importFixtures(IllegalDomainInfrastructureDependency.class)));
    }

    @Test
    void applicationNaoPodeDependerDeInfraestrutura() {
        ArchRule rule = noClasses().that().resideInAPackage("..application.usecases.servico.fixture..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..");

        assertThrows(AssertionError.class, () -> rule.check(importFixtures(IllegalApplicationInfrastructureDependency.class)));
    }

    @Test
    void infrastructureNaoPodeDependerDePresentation() {
        ArchRule rule = noClasses().that().resideInAPackage("..infrastructure.persistence.adapters.fixture..")
                .should().dependOnClassesThat().resideInAnyPackage("..presentation..");

        assertThrows(AssertionError.class, () -> rule.check(importFixtures(IllegalInfrastructurePresentationDependency.class)));
    }

    @Test
    void presentationNaoPodeDependerDeRepository() {
        ArchRule rule = noClasses().that().resideInAPackage("..presentation.servico.fixture..")
                .should().dependOnClassesThat().resideInAnyPackage("..repository..", "..infrastructure..");

        assertThrows(AssertionError.class, () -> rule.check(importFixtures(IllegalPresentationRepositoryDependency.class)));
    }

    @Test
    void gatewayNaoPodeDependerDePersistencia() {
        ArchRule rule = noClasses().that().resideInAPackage("..application.gateway.fixture..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure.persistence..");

        assertThrows(AssertionError.class, () -> rule.check(importFixtures(IllegalGatewayPersistenceDependency.class)));
    }

    @Test
    void applicationDeClienteNaoPodeDependerDeInfraestrutura() {
        ArchRule rule = noClasses().that().resideInAPackage("..application.usecases.cliente.fixture..")
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..repository..");

        assertThrows(AssertionError.class,
                () -> rule.check(importFixtures(IllegalClienteApplicationInfrastructureDependency.class)));
    }

    @Test
    void presentationDeClienteNaoPodeDependerDeRepository() {
        ArchRule rule = noClasses().that().resideInAPackage("..presentation.cliente.fixture..")
                .should().dependOnClassesThat().resideInAnyPackage("..repository..", "..infrastructure..");

        assertThrows(AssertionError.class,
                () -> rule.check(importFixtures(IllegalClientePresentationRepositoryDependency.class)));
    }

    private static com.tngtech.archunit.core.domain.JavaClasses importFixtures(Class<?> fixture) {
        return new ClassFileImporter().importClasses(fixture);
    }
}
