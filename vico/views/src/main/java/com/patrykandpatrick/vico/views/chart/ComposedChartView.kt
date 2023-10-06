/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.views.chart

import android.content.Context
import android.util.AttributeSet
import com.patrykandpatrick.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator.Companion.MODEL_COUNT
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator.Companion.SERIES_COUNT
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator.Companion.X_RANGE_TOP
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator.Companion.Y_RANGE_TOP
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.theme.ThemeHandler

/**
 * A subclass of [BaseChartView] that displays charts that use [ComposedChartEntryModel].
 */
public class ComposedChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseChartView<ComposedChartEntryModel<ChartEntryModel>>(
    context = context,
    attrs = attrs,
    defStyleAttr = defStyleAttr,
    chartType = ThemeHandler.ChartType.Composed,
) {

    init {
        chart = themeHandler.composedChart
        if (isInEditMode && attrs != null) setPreviewModel(attrs, defStyleAttr)
    }

    private fun setPreviewModel(attrs: AttributeSet, defStyleAttr: Int) {
        context.obtainStyledAttributes(attrs, R.styleable.ComposedChartView, defStyleAttr, 0).use { typedArray ->
            val modelCount = typedArray.getInt(R.styleable.ComposedChartView_previewModelCount, MODEL_COUNT)
            val seriesCount = typedArray.getInt(R.styleable.ComposedChartView_previewSeriesCount, SERIES_COUNT)
            val minX = typedArray.getInt(R.styleable.ComposedChartView_previewMinX, 0)
            val maxX = typedArray.getInt(R.styleable.ComposedChartView_previewMaxX, X_RANGE_TOP)
            val minY = typedArray.getInt(R.styleable.ComposedChartView_previewMinY, 0)
            val maxY = typedArray.getInt(R.styleable.ComposedChartView_previewMaxY, Y_RANGE_TOP)
            setModel(
                model = RandomEntriesGenerator(
                    xRange = minX..maxX,
                    yRange = minY..maxY,
                    seriesCount = seriesCount,
                    modelCount = modelCount,
                ).randomComposedEntryModel(),
            )
        }
    }
}
