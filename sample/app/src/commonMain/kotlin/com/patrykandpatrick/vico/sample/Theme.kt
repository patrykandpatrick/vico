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

package com.patrykandpatrick.vico.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Theme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme =
      if (isSystemInDarkTheme()) {
        darkColorScheme(
          secondaryContainer = Color(0xff18191b),
          onSecondaryContainer = Color.White,
          background = Color.Black,
          onBackground = Color.White,
          surface = Color.Black,
          onSurface = Color.White,
          outline = Color(0xff494c50),
          surfaceContainer = Color(0xff303336),
        )
      } else {
        lightColorScheme(
          secondaryContainer = Color(0xfff2f2f3),
          onSecondaryContainer = Color.Black,
          background = Color.White,
          onBackground = Color.Black,
          surface = Color.White,
          onSurface = Color.Black,
          outline = Color(0xffbcbfc2),
          surfaceContainer = Color.White,
        )
      },
    content = content,
  )
}
