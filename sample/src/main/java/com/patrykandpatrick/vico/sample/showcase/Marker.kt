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

package com.patrykandpatrick.vico.sample.showcase

import android.graphics.Typeface
import android.text.Layout
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberLayeredComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shape.markerCornered
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.copyColor
import com.patrykandpatrick.vico.core.common.shape.Corner
import com.patrykandpatrick.vico.core.common.shape.Shape

@Composable
internal fun rememberMarker(
  labelPosition: DefaultCartesianMarker.LabelPosition = DefaultCartesianMarker.LabelPosition.Top,
  showIndicator: Boolean = true,
): CartesianMarker {
  val labelBackgroundShape = Shape.markerCornered(Corner.FullyRounded)
  val labelBackground =
    rememberShapeComponent(labelBackgroundShape, MaterialTheme.colorScheme.surface)
      .setShadow(
        radius = LABEL_BACKGROUND_SHADOW_RADIUS_DP,
        dy = LABEL_BACKGROUND_SHADOW_DY_DP,
        applyElevationOverlay = true,
      )
  val label =
    rememberTextComponent(
      color = MaterialTheme.colorScheme.onSurface,
      background = labelBackground,
      padding = Dimensions.of(8.dp, 4.dp),
      typeface = Typeface.MONOSPACE,
      textAlignment = Layout.Alignment.ALIGN_CENTER,
      minWidth = TextComponent.MinWidth.fixed(40.dp),
    )
  val indicatorFrontComponent =
    rememberShapeComponent(Shape.Pill, MaterialTheme.colorScheme.surface)
  val indicatorCenterComponent = rememberShapeComponent(Shape.Pill)
  val indicatorRearComponent = rememberShapeComponent(Shape.Pill)
  val indicator =
    rememberLayeredComponent(
      rear = indicatorRearComponent,
      front =
        rememberLayeredComponent(
          rear = indicatorCenterComponent,
          front = indicatorFrontComponent,
          padding = Dimensions.of(5.dp),
        ),
      padding = Dimensions.of(10.dp),
    )
  val guideline = rememberAxisGuidelineComponent()
  return remember(label, labelPosition, indicator, showIndicator, guideline) {
    object :
      DefaultCartesianMarker(
        label = label,
        labelPosition = labelPosition,
        indicator = if (showIndicator) indicator else null,
        indicatorSizeDp = 36f,
        setIndicatorColor =
          if (showIndicator) {
            { color ->
              indicatorRearComponent.color = color.copyColor(alpha = .15f)
              indicatorCenterComponent.color = color
              indicatorCenterComponent.setShadow(radius = 12f, color = color)
            }
          } else {
            null
          },
        guideline = guideline,
      ) {
      override fun updateInsets(
        context: CartesianMeasureContext,
        horizontalDimensions: HorizontalDimensions,
        insets: Insets,
      ) {
        with(context) {
          val baseShadowInsetDp =
            CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER * LABEL_BACKGROUND_SHADOW_RADIUS_DP
          var topInset = (baseShadowInsetDp - LABEL_BACKGROUND_SHADOW_DY_DP).pixels
          var bottomInset = (baseShadowInsetDp + LABEL_BACKGROUND_SHADOW_DY_DP).pixels
          when (labelPosition) {
            LabelPosition.Top,
            LabelPosition.AbovePoint ->
              topInset += label.getHeight(context) + label.tickSizeDp.pixels
            LabelPosition.Bottom ->
              bottomInset += label.getHeight(context) + label.tickSizeDp.pixels
            LabelPosition.AroundPoint -> {}
          }
          insets.ensureValuesAtLeast(top = topInset, bottom = bottomInset)
        }
      }
    }
  }
}

private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
private const val CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER = 1.4f
