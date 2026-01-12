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

import com.android.build.api.dsl.androidLibrary
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  `dokka-convention`
  `publishing-convention`
  id("com.android.kotlin.multiplatform.library")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.multiplatform")
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  @Suppress("UnstableApiUsage")
  androidLibrary {
    configure()
    namespace = moduleNamespace
    // Host-side JVM tests (not device/instrumentation). We use this for MockK-based tests.
    withHostTest { isIncludeAndroidResources = true }
  }
  listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
    target.binaries.framework {
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
      implementation(compose.runtime)
      implementation(compose.ui)
      implementation(libs.androidXAnnotation)
      implementation(libs.coroutinesCore)
      implementation(libs.kotlinStdLib)
    }
    // Keep Kotlin test APIs in `commonTest` so target-specific test source sets inherit them.
    commonTest.dependencies { implementation(libs.kotlinTest) }
    // MockK isn’t multiplatform, so host-side JVM tests get it here.
    val androidHostTest by getting { dependencies { implementation(libs.mockK) } }
  }
  explicitApi()
  compilerOptions { freeCompilerArgs.add("-Xannotation-default-target=param-property") }
}

/*
 * Ensure `./gradlew test` includes this module’s test suite. In this module, the JVM-capable tests
 * live under Android host tests.
 */
val testTask = tasks.findByName("test")

if (testTask != null) {
  testTask.dependsOn("testAndroidHostTest")
} else {
  tasks.register("test") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Runs the vico-compose test suite on the JVM (Android host tests)."
    dependsOn("testAndroidHostTest")
  }
}
