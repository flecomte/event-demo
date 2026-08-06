package eventDemo.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.junit.jupiter.api.Test

/**
 * Vérifie le respect des frontières de l'architecture hexagonale (ports & adapters),
 * pour chaque bounded context sous `eventDemo.contexts`.
 *
 * Convention attendue, pour un contexte donné :
 *   eventDemo.contexts.<context>.domain
 *   eventDemo.contexts.<context>.application
 *   eventDemo.contexts.<context>.infrastructure
 *
 * Règles imposées :
 *   – domain         → ne dépend d'aucune autre couche (ni application, ni infrastructure)
 *   – application    → ne dépend que de domain (jamais d'infrastructure)
 *   – infrastructure → ne dépend que de domain et application
 */
class HexagonalArchitectureTest {
  private val rootPackage = "eventDemo.contexts"
  private val contexts = listOf("auth", "game")

  private val classes =
    ClassFileImporter()
      .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
      .importPackages(rootPackage)

  @Test
  fun `respecte les couches de l'architecture hexagonale`() {
    contexts.forEach { context ->
      val basePackage = "$rootPackage.$context"

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
}
