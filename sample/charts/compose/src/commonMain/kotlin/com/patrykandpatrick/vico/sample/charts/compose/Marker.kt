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

package com.patrykandpatrick.vico.sample.charts.compose

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LayeredComponent
import com.patrykandpatrick.vico.compose.common.MarkerCornerBasedShape
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent

@Composable
internal fun rememberMarker(
  valueFormatter: DefaultCartesianMarker.ValueFormatter =
    DefaultCartesianMarker.ValueFormatter.default(),
  showIndicator: Boolean = true,
): CartesianMarker {
  val labelBackgroundShape = MarkerCornerBasedShape(CircleShape)
  val labelBackground =
    rememberShapeComponent(
      fill = Fill(MaterialTheme.colorScheme.background),
      shape = labelBackgroundShape,
      strokeFill = Fill(MaterialTheme.colorScheme.outline),
      strokeThickness = 1.dp,
    )
  val label =
    rememberTextComponent(
      style =
        TextStyle(
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          fontSize = 12.sp,
        ),
      padding = Insets(8.dp, 4.dp),
      background = labelBackground,
      minWidth = TextComponent.MinWidth.fixed(40.dp),
    )
  val indicatorFrontComponent =
    rememberShapeComponent(Fill(MaterialTheme.colorScheme.surface), CircleShape)
  val guideline = rememberAxisGuidelineComponent()
  return rememberDefaultCartesianMarker(
    label = label,
    valueFormatter = valueFormatter,
    indicator =
      if (showIndicator) {
        { color ->
          LayeredComponent(
            back = ShapeComponent(Fill(color.copy(alpha = 0.15f)), CircleShape),
            front =
              LayeredComponent(
                back = ShapeComponent(fill = Fill(color), shape = CircleShape),
                front = indicatorFrontComponent,
                padding = Insets(5.dp),
              ),
            padding = Insets(10.dp),
          )
        }
      } else {
        null
      },
    indicatorSize = 36.dp,
    guideline = guideline,
  )
}
