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

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("com.android.application")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.multiplatform")
  id("org.jetbrains.kotlin.plugin.compose")
}

android {
  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  compileSdk = Versions.COMPILE_SDK
  defaultConfig {
    minSdk = Versions.MIN_SDK
    targetSdk = Versions.COMPILE_SDK
    versionName = Versions.VICO
  }
  namespace = "com.patrykandpatrick.vico"
}

dependencies { debugImplementation(compose.uiTooling) }

kotlin {
  androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_1_8) } }
  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "Sample"
      isStatic = true
    }
  }
  jvm("desktop")
  sourceSets {
    androidMain.dependencies {
      implementation(libs.activityCompose)
      implementation(libs.material)
      implementation(project(":sample:compose"))
      implementation(project(":sample:multiplatform"))
      implementation(project(":sample:views"))
    }
    commonMain.dependencies {
      implementation(compose.material3)
      implementation(libs.composeNavigation)
      implementation(libs.materialIcons)
      implementation(project(":sample:multiplatform"))
    }
    val desktopMain by getting
    desktopMain.dependencies { implementation(compose.desktop.currentOs) }
  }
}

compose.desktop {
  application {
    mainClass = "com.patrykandpatrick.vico.sample.MainKt"
    nativeDistributions {
      packageName = "com.patrykandpatrick.vico.sample"
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
    }
  }
}
