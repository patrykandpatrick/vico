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

package pl.patrykgoworowski.vico.view.theme

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.StyleableRes
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.axis.Axis
import pl.patrykgoworowski.vico.core.axis.AxisPosition
import pl.patrykgoworowski.vico.core.axis.axisBuilder
import pl.patrykgoworowski.vico.core.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.vico.core.axis.vertical.VerticalAxis
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.constants.Flags
import pl.patrykgoworowski.vico.core.dataset.column.ColumnDataSet
import pl.patrykgoworowski.vico.core.dataset.composed.ComposedDataSet
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryList
import pl.patrykgoworowski.vico.core.dataset.entry.collection.composed.ComposedEntryCollection
import pl.patrykgoworowski.vico.core.extension.hasFlag
import pl.patrykgoworowski.vico.core.util.RandomEntriesGenerator
import pl.patrykgoworowski.vico.view.R
import pl.patrykgoworowski.vico.view.dataset.common.DataSetWithModel
import pl.patrykgoworowski.vico.view.dataset.common.plus

internal class ThemeHandler(
    val context: Context,
    attrs: AttributeSet?,
    isInEditMode: Boolean,
) {

    public var startAxis: VerticalAxis<AxisPosition.Vertical.Start>? = null
        private set

    public var topAxis: HorizontalAxis<AxisPosition.Horizontal.Top>? = null
        private set

    public var endAxis: VerticalAxis<AxisPosition.Vertical.End>? = null
        private set

    public var bottomAxis: HorizontalAxis<AxisPosition.Horizontal.Bottom>? = null
        private set

    public var isHorizontalScrollEnabled: Boolean = false
        private set

    public var isChartZoomEnabled: Boolean = false
        private set

    public var dataSet: DataSetWithModel<*>? = null
        private set

    init {
        context.obtainStyledAttributes(attrs, R.styleable.DataSetView).use { typedArray ->
            if (typedArray.getBoolean(R.styleable.DataSetView_showStartAxis, false)) {
                startAxis = VerticalAxis.Builder(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.DataSetView_showTopAxis, false)) {
                topAxis = HorizontalAxis.Builder(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.DataSetView_showEndAxis, false)) {
                endAxis = VerticalAxis.Builder(typedArray.getAxis()).build()
            }
            if (typedArray.getBoolean(R.styleable.DataSetView_showBottomAxis, false)) {
                bottomAxis = HorizontalAxis.Builder(typedArray.getAxis()).build()
            }
            isHorizontalScrollEnabled = typedArray
                .getBoolean(R.styleable.DataSetView_chartHorizontalScrollingEnabled, false)
            isChartZoomEnabled = typedArray
                .getBoolean(R.styleable.DataSetView_chartZoomEnabled, false)
            dataSet = typedArray.getDataSetModel(isInEditMode)
        }
    }

    private fun TypedArray.getAxis(): Axis.Builder {

        fun TypedArray.getLineComponent(
            @StyleableRes resourceId: Int,
            @StyleableRes styleableResourceId: IntArray,
        ): LineComponent =
            getNestedTypedArray(
                context = context,
                resourceId = resourceId,
                styleableResourceId = styleableResourceId,
            ).getLineComponent(context = context)

        return getNestedTypedArray(
            context = context,
            resourceId = R.styleable.DataSetView_axisStyle,
            styleableResourceId = R.styleable.Axis,
        ).use { axisStyle ->
            axisBuilder {
                axis = axisStyle.getLineComponent(
                    resourceId = R.styleable.Axis_axisLineStyle,
                    styleableResourceId = R.styleable.LineComponentStyle
                )
                tick = axisStyle.getLineComponent(
                    resourceId = R.styleable.Axis_axisTickStyle,
                    styleableResourceId = R.styleable.LineComponentStyle
                )
                tickLengthDp = axisStyle.getRawDimension(
                    context = context,
                    R.styleable.Axis_axisTickLength,
                    defaultValue = Dimens.AXIS_TICK_LENGTH,
                )
                guideline = axisStyle.getLineComponent(
                    resourceId = R.styleable.Axis_axisGuidelineStyle,
                    styleableResourceId = R.styleable.LineComponentStyle
                )
                label = axisStyle.getNestedTypedArray(
                    context = context,
                    resourceId = R.styleable.Axis_axisLabelStyle,
                    styleableResourceId = R.styleable.LabelStyle
                ).getTextComponent(context = context)
            }
        }
    }

    private fun TypedArray.getDataSetModel(isInEditMode: Boolean): DataSetWithModel<*>? {
        val chartFlags = getInt(R.styleable.DataSetView_chartType, 0)

        val columnDataSet: ColumnDataSet? = if (chartFlags.hasFlag(Flags.COLUMN_CHART)) {
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.DataSetView_columnChartStyle,
                styleableResourceId = R.styleable.ColumnChartStyle,
            ).getColumnChart(context)
        } else {
            null
        }
        val lineDataSet = if (chartFlags.hasFlag(Flags.LINE_CHART)) {
            getNestedTypedArray(
                context = context,
                resourceId = R.styleable.DataSetView_lineChartStyle,
                styleableResourceId = R.styleable.LineChartStyle,
            ).getLineChart(context)
        } else {
            null
        }

        return when {
            columnDataSet != null && lineDataSet != null ->
                ComposedDataSet(columnDataSet, lineDataSet) + ComposedEntryCollection(
                    EntryList(
                        RandomEntriesGenerator().generateRandomEntries(),
                        animateChanges = false
                    ),
                    EntryList(
                        RandomEntriesGenerator().generateRandomEntries(),
                        animateChanges = false
                    ),
                ).model
            columnDataSet != null ->
                columnDataSet + RandomEntriesGenerator().randomEntryModel()
            lineDataSet != null ->
                lineDataSet + RandomEntriesGenerator().randomEntryModel()
            else -> null
        }
    }
}
