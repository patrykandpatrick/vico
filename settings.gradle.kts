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

pluginManagement.repositories {
  google()
  gradlePluginPortal()
  mavenCentral()
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    google()
    mavenCentral()
    mavenLocal()
  }
}

rootProject.name = "Vico"

include(
  "sample",
  "vico",
  "vico:compose",
  "vico:compose-m2",
  "vico:compose-m3",
  "vico:core",
  "vico:multiplatform",
  "vico:views",
)
