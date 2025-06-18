package dev.heinisch.menumaestro.arch_unit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class LayeringTest {
    private static JavaClasses importedClasses;
    private static final String PACKAGE_NAME = "dev.heinisch.menumaestro";

    @BeforeAll
    public static void loadClasses() {
        importedClasses = new ClassFileImporter().importPackages(PACKAGE_NAME);
    }

    @Test
    public void testLayeredArchitecture() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Persistence").definedBy(PACKAGE_NAME + ".persistence..")
                .layer("Service").definedBy(PACKAGE_NAME + ".service..")
                .layer("Domain").definedBy(PACKAGE_NAME + ".domain..")
                .layer("Endpoint").definedBy(PACKAGE_NAME + ".endpoint")

                .whereLayer("Endpoint").mayOnlyAccessLayers("Service")
                .whereLayer("Service").mayOnlyAccessLayers("Persistence", "Domain")
                .whereLayer("Domain").mayNotAccessAnyLayer()
                .whereLayer("Persistence").mayOnlyAccessLayers("Domain")
                .check(importedClasses);
    }

    /**
     * <ul>
     *     <li>Transactional</li>
     *     <li>Parameters and return value is not from domain model</li>
     *     <li>It either is purely an endpoint+service (inherits from an Api) or internal service</li>
     *     <li>If endpoint service, it has a PreAuthorize</li>
     *     <li>If endpoint, all public methods are inherited</li>
     * </ul>
     */
    @Test
    public void checkServiceInterfaceAndAnnotations() {

    }
}
