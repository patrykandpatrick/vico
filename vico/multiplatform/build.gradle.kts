/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `dokka-convention`
  `publishing-convention`
  id("com.android.library")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.multiplatform")
  id("org.jetbrains.kotlin.plugin.compose")
}

android {
  configure()
  namespace = moduleNamespace
}

kotlin {
  androidTarget {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_1_8) }
    publishLibraryVariants("release")
  }
  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
    target.binaries.framework {
      baseName = project.name
      isStatic = true
    }
  }
  jvm("desktop")
  sourceSets {
    commonMain.dependencies {
      implementation(compose.foundation)
      implementation(compose.runtime)
      implementation(compose.ui)
      implementation(libs.androidXAnnotation)
      implementation(libs.coroutinesCore)
      implementation(libs.kotlinStdLib)
    }
  }
  explicitApi()
}
