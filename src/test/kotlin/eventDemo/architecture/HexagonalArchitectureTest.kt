package eventDemo.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.junit.jupiter.api.Test

/**
 * Vérifie le respect des frontières de l'architecture hexagonale (ports & adapters).
 *
 * Convention attendue :
 *   eventDemo.contexts.uno.domain
 *   eventDemo.contexts.uno.application
 *   eventDemo.contexts.uno.infrastructure
 *
 * Règles imposées :
 *   – domain         → ne dépend d'aucune autre couche (ni application, ni infrastructure)
 *   – application    → ne dépend que de domain (jamais d'infrastructure)
 *   – infrastructure → ne dépend que de domain et application
 */
class HexagonalArchitectureTest {
  private val basePackage = "eventDemo.contexts.uno"

  private val classes =
    ClassFileImporter()
      .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
      .importPackages(basePackage)

  @Test
  fun `respecte les couches de l'architecture hexagonale`() {
    @Suppress("ktlint:standard:chain-method-continuation")
    layeredArchitecture()
      .consideringAllDependencies()
      .layer("Domain").definedBy("$basePackage.domain..")
      .layer("Application").definedBy("$basePackage.application..")
      .layer("Infrastructure").definedBy("$basePackage.infrastructure..")
      .whereLayer("Domain").mayNotAccessAnyLayer()
      .whereLayer("Application").mayOnlyAccessLayers("Domain")
      .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Application")
      .check(classes)
  }
}
