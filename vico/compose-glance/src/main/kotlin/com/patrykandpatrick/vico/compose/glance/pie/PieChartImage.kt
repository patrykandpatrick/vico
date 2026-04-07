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

package com.patrykandpatrick.vico.compose.glance.pie

import android.content.Context
import android.view.View
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
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.data.PieChartModel
import com.patrykandpatrick.vico.compose.pie.renderToImageBitmap
import kotlin.math.roundToInt

/**
 * Displays a [PieChart] as a static image in a Glance app widget.
 *
 * The chart is rendered off-screen to a bitmap and displayed via Glance's [Image]. Since Glance
 * doesn't support Compose Canvas, this is the recommended approach for showing charts in widgets.
 *
 * @param chart the [PieChart].
 * @param model the [PieChartModel].
 * @param contentDescription the content description for accessibility.
 * @param modifier the [GlanceModifier] to be applied.
 * @param size the desired chart size. Defaults to the widget size provided by Glance's [LocalSize].
 * @param contentScale how the chart image should be scaled within its bounds.
 */
@androidx.compose.runtime.Composable
public fun PieChartImage(
  chart: PieChart,
  model: PieChartModel,
  contentDescription: String?,
  modifier: GlanceModifier = GlanceModifier,
  size: DpSize = LocalSize.current,
  contentScale: ContentScale = ContentScale.Fit,
) {
  val context = LocalContext.current
  val bitmap = renderPieChartToBitmap(context, chart, model, size)
  Image(
    provider = ImageProvider(bitmap),
    contentDescription = contentDescription,
    modifier = modifier,
    contentScale = contentScale,
  )
}

private fun renderPieChartToBitmap(
  context: Context,
  chart: PieChart,
  model: PieChartModel,
  size: DpSize,
): android.graphics.Bitmap {
  val resources = context.resources
  val density = Density(resources.displayMetrics.density, resources.configuration.fontScale)
  val widthPx = with(density) { size.width.toPx() }.roundToInt().coerceAtLeast(1)
  val heightPx = with(density) { size.height.toPx() }.roundToInt().coerceAtLeast(1)
  val fontFamilyResolver = createFontFamilyResolver(context)
  val layoutDirection =
    if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
      LayoutDirection.Rtl
    } else {
      LayoutDirection.Ltr
    }
  return chart
    .renderToImageBitmap(model, widthPx, heightPx, fontFamilyResolver, density, layoutDirection)
    .asAndroidBitmap()
}
