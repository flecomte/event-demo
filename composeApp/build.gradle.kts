import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val kotlinSerializationVersion: Provider<String> = providers.gradleProperty("kotlin_serialization_version")
val kotlinxCoroutinesVersion: Provider<String> = providers.gradleProperty("kotlinx_coroutines_version")
val ktorVersion: Provider<String> = providers.gradleProperty("ktor_version")
val navigationComposeVersion: Provider<String> = providers.gradleProperty("navigation_compose_version")
val androidCompileSdk: Provider<String> = providers.gradleProperty("android_compile_sdk")
val androidTargetSdk: Provider<String> = providers.gradleProperty("android_target_sdk")
val androidMinSdk: Provider<String> = providers.gradleProperty("android_min_sdk")

plugins {
  kotlin("multiplatform")
  kotlin("plugin.compose")
  kotlin("plugin.serialization")
  id("org.jetbrains.compose")
  id("com.android.application")
}

kotlin {
  androidTarget {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_21)
      freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
  }

  jvm("desktop") {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_21)
      freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    outputModuleName = "eventDemoApp"
    browser {
      commonWebpackConfig {
        outputFileName = "eventDemoApp.js"
      }
    }
    binaries.executable()
  }

  compilerOptions {
    freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
  }

  sourceSets {
    commonMain.dependencies {
      implementation(project(":shared"))

      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)

      implementation("org.jetbrains.androidx.navigation:navigation-compose:${navigationComposeVersion.get()}")
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinxCoroutinesVersion.get()}")
      implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinSerializationVersion.get()}")

      implementation("io.ktor:ktor-client-core:${ktorVersion.get()}")
      implementation("io.ktor:ktor-client-content-negotiation:${ktorVersion.get()}")
      implementation("io.ktor:ktor-client-websockets:${ktorVersion.get()}")
      implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion.get()}")
    }

    androidMain.dependencies {
      implementation("androidx.activity:activity-compose:1.11.0")
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${kotlinxCoroutinesVersion.get()}")
      implementation("io.ktor:ktor-client-okhttp:${ktorVersion.get()}")
    }

    getByName("desktopMain").dependencies {
      implementation(compose.desktop.currentOs)
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${kotlinxCoroutinesVersion.get()}")
      implementation("io.ktor:ktor-client-cio:${ktorVersion.get()}")
    }

    wasmJsMain.dependencies {
      implementation("io.ktor:ktor-client-js:${ktorVersion.get()}")
    }
  }
}

android {
  namespace = "io.github.flecomte.eventdemo.app"
  compileSdk = androidCompileSdk.get().toInt()

  defaultConfig {
    applicationId = "io.github.flecomte.eventdemo.app"
    minSdk = androidMinSdk.get().toInt()
    targetSdk = androidTargetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
}

compose.desktop {
  application {
    mainClass = "eventDemo.app.MainKt"

    nativeDistributions {
      targetFormats(
        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
      )
      packageName = "EventDemo"
      packageVersion = "1.0.0"
    }
  }
}
