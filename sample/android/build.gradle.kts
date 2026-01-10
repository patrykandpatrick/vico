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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  id("com.android.application")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
  kotlin("android")
}

android {
  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  compileSdk = Versions.COMPILE_SDK
  defaultConfig {
    minSdk = Versions.MIN_SDK
    targetSdk = Versions.COMPILE_SDK
    versionName = Versions.VICO
  }
  namespace = "com.patrykandpatrick.vico.sample"
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  implementation(project(":sample:app"))
  implementation(libs.activityCompose)
  implementation(libs.material)
  debugImplementation(compose.uiTooling)
}
