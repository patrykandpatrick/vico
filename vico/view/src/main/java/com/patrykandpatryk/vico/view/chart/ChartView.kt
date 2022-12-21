/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatryk.vico.view.chart

import android.content.Context
import android.util.AttributeSet
import com.patrykandpatryk.vico.core.entry.ChartEntryModel
import com.patrykandpatryk.vico.core.util.RandomEntriesGenerator
import com.patrykandpatryk.vico.view.theme.ThemeHandler

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
        if (isInEditMode) setModel(model = RandomEntriesGenerator().randomEntryModel())
    }
}
