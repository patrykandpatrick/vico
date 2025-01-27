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

plugins { `dokka-convention` }

subprojects {
  group = "com.patrykandpatrick.vico"
  version = Versions.VICO
}

dependencies {
  dokka(project(":vico:compose"))
  dokka(project(":vico:compose-m2"))
  dokka(project(":vico:compose-m3"))
  dokka(project(":vico:core"))
  dokka(project(":vico:multiplatform"))
  dokka(project(":vico:views"))
}

dokka { pluginsConfiguration.html { customStyleSheets.from("$rootDir/logo-styles.css") } }
