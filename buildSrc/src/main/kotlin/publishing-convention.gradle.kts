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

import com.vanniktech.maven.publish.SonatypeHost

plugins { id("com.vanniktech.maven.publish") }

mavenPublishing {
  publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
  signAllPublications()
  pom {
    name = "Vico"
    description = "A powerful and extensible multiplatform chart library."
    url = "https://github.com/patrykandpatrick/vico"
    licenses {
      license {
        name = "The Apache License, Version 2.0"
        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    }
    scm {
      connection = "scm:git:git://github.com/patrykandpatrick/vico.git"
      developerConnection = "scm:git:ssh://github.com/patrykandpatrick/vico.git"
      url = "https://github.com/patrykandpatrick/vico"
    }
    developers {
      developer {
        id = "patrykgoworowski"
        name = "Patryk Goworowski"
        email = "contact@patrykgoworowski.pl"
      }
      developer {
        id = "patrickmichalik"
        name = "Patrick Michalik"
        email = "contact@patrickmichalik.com"
      }
    }
  }
}
