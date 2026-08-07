val kotlinSerializationVersion: Provider<String> = providers.gradleProperty("kotlin_serialization_version")
val androidCompileSdk: Provider<String> = providers.gradleProperty("android_compile_sdk")
val androidMinSdk: Provider<String> = providers.gradleProperty("android_min_sdk")

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("com.android.library")
}

kotlin {
  jvm()

  androidTarget {
    compilerOptions {
      freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
  }

  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }

  compilerOptions {
    freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
  }

  sourceSets {
    commonMain.dependencies {
      implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinSerializationVersion.get()}")
    }
    commonTest.dependencies {
      implementation(kotlin("test"))
    }
  }
}

android {
  namespace = "io.github.flecomte.eventdemo.shared"
  compileSdk = androidCompileSdk.get().toInt()

  defaultConfig {
    minSdk = androidMinSdk.get().toInt()
  }
}
