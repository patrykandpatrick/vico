/*
 * Copyright 2026 by Patryk Goworowski and Patrick Michalik.
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

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  id("com.android.kotlin.multiplatform.library")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.multiplatform")
  id("org.jetbrains.kotlin.plugin.compose")
  kotlin("plugin.serialization")
}

kotlin {
  androidLibrary {
    configure()
    namespace = "com.patrykandpatrick.vico.sample.app"
  }
  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "Sample"
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
    androidMain.dependencies { implementation(project(":sample:charts:views")) }
    commonMain.dependencies {
      implementation(compose.material3)
      implementation(libs.composeNavigation)
      implementation(libs.lifecycleRuntime)
      implementation(libs.materialIcons)
      implementation(project(":sample:charts:compose"))
    }
    val desktopMain by getting
    desktopMain.dependencies { implementation(compose.desktop.currentOs) }
  }
}

compose.desktop {
  application {
    mainClass = "com.patrykandpatrick.vico.sample.app.MainKt"
    nativeDistributions {
      packageName = "com.patrykandpatrick.vico.sample"
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
    }
  }
}
