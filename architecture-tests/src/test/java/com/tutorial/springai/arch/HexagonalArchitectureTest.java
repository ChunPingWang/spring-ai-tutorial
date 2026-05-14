package com.tutorial.springai.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.tutorial.springai",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
public class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring_or_persistence = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "org.hibernate..",
                    "com.fasterxml.jackson.."
            )
            .as("Domain layer must stay free of framework, persistence, and serialization concerns");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_adapter_or_application = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..adapter..",
                    "..application.."
            )
            .as("Domain must not reach outwards — dependencies point inward");

    @ArchTest
    static final ArchRule application_should_not_depend_on_adapter = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..")
            .as("Application layer talks to adapters only through outbound ports");

    @ArchTest
    static final ArchRule hexagonal_layering = Architectures.layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .optionalLayer("Domain").definedBy("..domain..")
            .optionalLayer("Application").definedBy("..application..")
            .optionalLayer("Adapter").definedBy("..adapter..")
            .whereLayer("Adapter").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapter")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapter")
            .as("Hexagonal layering: Adapter -> Application -> Domain (inward only)");
}
