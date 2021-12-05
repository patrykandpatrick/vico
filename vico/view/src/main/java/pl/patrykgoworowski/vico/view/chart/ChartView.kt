/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.view.chart

import android.content.Context
import android.util.AttributeSet
import pl.patrykgoworowski.vico.core.entry.ChartEntryModel
import pl.patrykgoworowski.vico.core.util.RandomEntriesGenerator
import pl.patrykgoworowski.vico.view.theme.ThemeHandler

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

        if (isInEditMode) {
            setModel(RandomEntriesGenerator().randomEntryModel())
        }
    }
}
