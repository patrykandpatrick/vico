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

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("com.android.library")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.multiplatform")
  id("org.jetbrains.kotlin.plugin.compose")
}

android {
  configure()
  namespace = "com.patrykandpatrick.vico.sample.multiplatform"
}

kotlin {
  androidTarget { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }
  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = project.name
      isStatic = true
    }
  }
  jvm("desktop")
  js {
    browser()
    binaries.executable()
  }
  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.executable()
  }
  sourceSets {
    commonMain.dependencies {
      implementation(compose.foundation)
      implementation(compose.ui)
      implementation(compose.material3)
      implementation(libs.kotlinDateTime)
      implementation(project(":vico:multiplatform"))
    }
  }
}
