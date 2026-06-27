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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.composeMultiplatformSample)
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.kotlin.plugin.compose")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  implementation(project(":sample:shared"))
  implementation(compose.desktop.currentOs)
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
