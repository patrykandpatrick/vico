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

import org.gradle.api.attributes.Attribute
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `dokka-convention`
  `publishing-convention`
  id("com.android.library")
}

val generatedCommonSources = layout.buildDirectory.dir("generated/commonMain/kotlin")

val generateCommonSources by
  tasks.registering(Sync::class) {
    from("../shared/src/commonMain/kotlin") {
      filter { line: String ->
        line.replace("com.patrykandpatrick.vico.shared", "com.patrykandpatrick.vico.views")
      }
    }
    into(generatedCommonSources)
  }

dokka {
  dokkaSourceSets.register("main") {
    sourceRoots.from("src/main/kotlin", generatedCommonSources)
    classpath.from(
      configurations.named("debugCompileClasspath").map { configuration ->
        configuration.incoming
          .artifactView {
            attributes.attribute(Attribute.of("artifactType", String::class.java), "jar")
          }
          .files
      }
    )
  }
}

android {
  configure()
  namespace = moduleNamespace
}

kotlin {
  explicitApi()
  compilerOptions { jvmTarget = JvmTarget.JVM_11 }
}

tasks.withType<KotlinCompile>().configureEach {
  if (!name.contains("Test", ignoreCase = true)) {
    dependsOn(generateCommonSources)
    source(generatedCommonSources)
  }
}

dependencies {
  implementation(libs.androidXAnnotation)
  implementation(libs.androidXCore)
  implementation(libs.appcompat)
  implementation(libs.coroutinesCore)
  implementation(libs.kotlinStdLib)
  testImplementation(libs.kotlinTest)
  testImplementation(libs.kotlinTestJunit5)
  testImplementation(libs.mockK)
}
