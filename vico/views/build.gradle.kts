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
  `publishing-convention`
  id("com.android.library")
  id("kotlin-android")
  `dokka-convention`
}

android {
  configure()
  namespace = moduleNamespace
}

kotlin {
  explicitApi()
  compilerOptions { jvmTarget = JvmTarget.JVM_11 }
}

dependencies {
  api(project(":vico:core"))
  implementation(libs.androidXCore)
  implementation(libs.appcompat)
  implementation(libs.kotlinStdLib)
  testImplementation(libs.kotlinTest)
}
