/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

buildscript {
  dependencies {
    classpath(libs.buildTools)
    classpath(libs.kotlinGradlePlugin)
    classpath(libs.paparazziGradlePlugin)
  }
}

plugins {
  alias(libs.plugins.composeCompiler) apply false
  alias(libs.plugins.dokka) apply false
}

apply("versions.gradle")

tasks.register<Delete>("clean") { delete(rootProject.layout.buildDirectory) }

subprojects.forEach { project ->
  project.tasks.withType<Test>().configureEach { useJUnitPlatform() }
}
