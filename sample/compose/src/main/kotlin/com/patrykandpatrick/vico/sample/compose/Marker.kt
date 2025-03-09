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

package com.patrykandpatrick.vico.sample.compose

import android.text.Layout
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shadow
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.shape.markerCorneredShape
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

@Composable
internal fun rememberMarker(
  valueFormatter: DefaultCartesianMarker.ValueFormatter =
    DefaultCartesianMarker.ValueFormatter.default(),
  showIndicator: Boolean = true,
): CartesianMarker {
  val labelBackgroundShape = markerCorneredShape(CorneredShape.Corner.Rounded)
  val labelBackground =
    rememberShapeComponent(
      fill = fill(MaterialTheme.colorScheme.background),
      shape = labelBackgroundShape,
      strokeThickness = 1.dp,
      strokeFill = fill(MaterialTheme.colorScheme.outline),
    )
  val label =
    rememberTextComponent(
      color = MaterialTheme.colorScheme.onSurface,
      textAlignment = Layout.Alignment.ALIGN_CENTER,
      padding = insets(8.dp, 4.dp),
      background = labelBackground,
      minWidth = TextComponent.MinWidth.fixed(40.dp),
    )
  val indicatorFrontComponent =
    rememberShapeComponent(fill(MaterialTheme.colorScheme.surface), CorneredShape.Pill)
  val guideline = rememberAxisGuidelineComponent()
  return rememberDefaultCartesianMarker(
    label = label,
    valueFormatter = valueFormatter,
    indicator =
      if (showIndicator) {
        { color ->
          LayeredComponent(
            back = ShapeComponent(fill(color.copy(.15f)), CorneredShape.Pill),
            front =
              LayeredComponent(
                back =
                  ShapeComponent(
                    fill = fill(color),
                    shape = CorneredShape.Pill,
                    shadow = shadow(radius = 12.dp, color = color),
                  ),
                front = indicatorFrontComponent,
                padding = insets(5.dp),
              ),
            padding = insets(10.dp),
          )
        }
      } else {
        null
      },
    indicatorSize = 36.dp,
    guideline = guideline,
  )
}
