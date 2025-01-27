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

plugins {
  id("com.android.application")
  id("kotlin-android")
  id("org.jetbrains.kotlin.plugin.compose")
}

android {
  buildFeatures {
    viewBinding = true
    compose = true
  }
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
  kotlinOptions { jvmTarget = JavaVersion.VERSION_1_8.toString() }
  namespace = "com.patrykandpatrick.vico"
}

composeCompiler { reportsDestination = layout.buildDirectory.dir("reports") }

dependencies {
  debugImplementation(libs.composeUITooling)
  implementation(libs.activityCompose)
  implementation(libs.androidXCore)
  implementation(libs.appcompat)
  implementation(libs.composeMaterial)
  implementation(libs.composeMaterial3)
  implementation(libs.composeNavigation)
  implementation(libs.composePreview)
  implementation(libs.composeUI)
  implementation(libs.composeViewBinding)
  implementation(libs.coroutinesCore)
  implementation(libs.kotlinStdLib)
  implementation(libs.lifecycleRuntime)
  implementation(libs.lifecycleRuntimeCompose)
  implementation(libs.material)
  implementation(libs.viewModelCompose)
  implementation(platform(libs.composeBom))
  implementation(project(":vico:compose-m3"))
  implementation(project(":vico:views"))
  testImplementation(libs.kotlinTest)
}
