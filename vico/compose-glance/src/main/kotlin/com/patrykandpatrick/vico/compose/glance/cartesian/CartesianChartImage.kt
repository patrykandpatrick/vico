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

package com.patrykandpatrick.vico.compose.glance.cartesian

import android.content.Context
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.layout.ContentScale
import com.patrykandpatrick.vico.compose.cartesian.CartesianChart
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.renderToImageBitmap
import kotlin.math.roundToInt

/**
 * Displays a [CartesianChart] as a static image in a Glance app widget.
 *
 * The chart is rendered off-screen to a bitmap and displayed via Glance's [Image]. Since Glance
 * doesn't support Compose Canvas, this is the recommended approach for showing charts in widgets.
 *
 * @param chart the [CartesianChart].
 * @param model the [CartesianChartModel].
 * @param contentDescription the content description for accessibility.
 * @param modifier the [GlanceModifier] to be applied.
 * @param size the desired chart size. Defaults to the widget size provided by Glance's [LocalSize].
 * @param contentScale how the chart image should be scaled within its bounds.
 */
@androidx.compose.runtime.Composable
public fun CartesianChartImage(
  chart: CartesianChart,
  model: CartesianChartModel,
  contentDescription: String?,
  modifier: GlanceModifier = GlanceModifier,
  size: DpSize = LocalSize.current,
  contentScale: ContentScale = ContentScale.Fit,
) {
  val context = LocalContext.current
  val bitmap = renderCartesianChartToBitmap(context, chart, model, size)
  Image(
    provider = ImageProvider(bitmap),
    contentDescription = contentDescription,
    modifier = modifier,
    contentScale = contentScale,
  )
}

private fun renderCartesianChartToBitmap(
  context: Context,
  chart: CartesianChart,
  model: CartesianChartModel,
  size: DpSize,
): android.graphics.Bitmap {
  val resources = context.resources
  val density = Density(resources.displayMetrics.density, resources.configuration.fontScale)
  val widthPx = with(density) { size.width.toPx() }.roundToInt().coerceAtLeast(1)
  val heightPx = with(density) { size.height.toPx() }.roundToInt().coerceAtLeast(1)
  val fontFamilyResolver = createFontFamilyResolver(context)
  val layoutDirection =
    if (context.resources.configuration.layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL) {
      LayoutDirection.Rtl
    } else {
      LayoutDirection.Ltr
    }
  return chart
    .renderToImageBitmap(model, widthPx, heightPx, fontFamilyResolver, density, layoutDirection)
    .asAndroidBitmap()
}
