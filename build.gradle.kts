buildscript {
  configurations.classpath {
    resolutionStrategy {
      force("org.jetbrains:annotations:23.0.0")
    }
  }
}

plugins {
  kotlin("multiplatform") version "2.1.21" apply false
  kotlin("plugin.serialization") version "2.4.10" apply false
  kotlin("plugin.compose") version "2.1.21" apply false
  id("com.android.application") version "9.2.0" apply false
  id("com.android.library") version "9.2.0" apply false
  id("org.jetbrains.compose") version "1.8.2" apply false
}
