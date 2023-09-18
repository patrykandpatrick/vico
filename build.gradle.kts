/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

import io.gitlab.arturbosch.detekt.Detekt

buildscript {
    dependencies {
        classpath(libs.buildTools)
        classpath(libs.kotlinGradlePlugin)
        classpath(libs.paparazziGradlePlugin)
    }
}

plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka) apply false
}

apply("versions.gradle")

dependencies { detektPlugins(libs.detektFormatting) }

tasks.register<Delete>("clean") { delete(rootProject.layout.buildDirectory) }

tasks.register<Detekt>("detektFix") { autoCorrect = true }

tasks.withType<Detekt>().configureEach {
    source = fileTree(projectDir)
    config = files(".idea/detekt-config.yml")
    buildUponDefaultConfig = true
    reports.html {
        required = true
        outputLocation = file("build/reports/detekt/detekt.html")
    }
}
