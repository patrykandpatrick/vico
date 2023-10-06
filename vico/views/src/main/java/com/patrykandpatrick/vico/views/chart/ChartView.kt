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
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator.Companion.SERIES_COUNT
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator.Companion.X_RANGE_TOP
import com.patrykandpatrick.vico.core.util.RandomEntriesGenerator.Companion.Y_RANGE_TOP
import com.patrykandpatrick.vico.views.R
import com.patrykandpatrick.vico.views.theme.ThemeHandler

/**
 * A subclass of [BaseChartView] that displays charts that use [ChartEntryModel].
 */
public class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseChartView<ChartEntryModel>(
    context = context,
    attrs = attrs,
    defStyleAttr = defStyleAttr,
    chartType = ThemeHandler.ChartType.Single,
) {
    init {
        chart = themeHandler.chart
        if (isInEditMode && attrs != null) setPreviewModel(attrs, defStyleAttr)
    }

    private fun setPreviewModel(attrs: AttributeSet, defStyleAttr: Int) {
        context.obtainStyledAttributes(attrs, R.styleable.ChartView, defStyleAttr, 0).use { typedArray ->
            val seriesCount = typedArray.getInt(R.styleable.ChartView_previewSeriesCount, SERIES_COUNT)
            val minX = typedArray.getInt(R.styleable.ChartView_previewMinX, 0)
            val maxX = typedArray.getInt(R.styleable.ChartView_previewMaxX, X_RANGE_TOP)
            val minY = typedArray.getInt(R.styleable.ChartView_previewMinY, 0)
            val maxY = typedArray.getInt(R.styleable.ChartView_previewMaxY, Y_RANGE_TOP)
            setModel(
                model = RandomEntriesGenerator(
                    xRange = minX..maxX,
                    yRange = minY..maxY,
                    seriesCount = seriesCount,
                ).randomEntryModel(),
            )
        }
    }
}
