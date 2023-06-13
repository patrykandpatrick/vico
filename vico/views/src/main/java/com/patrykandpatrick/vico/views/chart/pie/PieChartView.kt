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

package com.patrykandpatrick.vico.views.chart.pie

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.patrykandpatrick.vico.core.chart.pie.PieChart
import com.patrykandpatrick.vico.core.chart.pie.Size
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.draw.drawContext
import com.patrykandpatrick.vico.core.entry.pie.FloatPieEntry
import com.patrykandpatrick.vico.core.entry.pie.PieEntryModel
import com.patrykandpatrick.vico.core.entry.pie.pieEntryModelOf
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.views.chart.BaseChartView
import com.patrykandpatrick.vico.views.extension.defaultColors
import com.patrykandpatrick.vico.views.extension.isAttachedToWindowCompat
import com.patrykandpatrick.vico.views.extension.measureDimension
import com.patrykandpatrick.vico.views.extension.specSize
import com.patrykandpatrick.vico.views.theme.PieChartStyleHandler

/**
 * A [View] that displays a pie chart.
 */
public open class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseChartView<PieEntryModel>(context, attrs, defStyleAttr) {

    private val pieChartStyleHandler: PieChartStyleHandler = PieChartStyleHandler(
        context = context,
        attrs = attrs,
    )

    protected val pieChart: PieChart = PieChart(
        slices = pieChartStyleHandler.slices,
        spacingDp = pieChartStyleHandler.sliceSpacing,
        outerSize = pieChartStyleHandler.outerSize,
        innerSize = pieChartStyleHandler.innerSize,
        startAngle = pieChartStyleHandler.startAngle,
    )

    /**
     * The [List] of [Slice]s which define the appearance of each slice of the pie chart.
     */
    public var slices: List<Slice>
        get() = pieChart.slices
        set(value) {
            pieChart.slices = value
            invalidate()
        }

    /**
     * The spacing between each slice of the pie chart (in dp).
     */
    public var sliceSpacingDp: Float
        get() = pieChart.spacingDp
        set(value) {
            pieChart.spacingDp = value
            invalidate()
        }

    /**
     * Defines the outer size of the pie chart.
     */
    public var pieOuterSize: Size.OuterSize
        get() = pieChart.outerSize
        set(value) {
            pieChart.outerSize = value
            invalidate()
        }

    /**
     * Defines the inner size of the pie chart.
     */
    public var pieInnerSize: Size.InnerSize
        get() = pieChart.innerSize
        set(value) {
            pieChart.innerSize = value
            invalidate()
        }

    /**
     * Defines the start angle of the pie chart (in degrees).
     */
    public var startAngle: Float
        get() = pieChart.startAngle
        set(value) {
            pieChart.startAngle = value
            invalidate()
        }

    /**
     * The color of elevation overlays, which are applied to [ShapeComponent]s that cast shadows.
     */
    public var elevationOverlayColor: Long = context.defaultColors.elevationOverlayColor

    final override var model: PieEntryModel? = null
        private set

    init {
        if (isInEditMode) {
            setModel(model = sampleModel)
        }
    }

    /**
     * Sets the [PieEntryModel] to display.
     */
    public final override fun setModel(model: PieEntryModel) {
        this.model = model
        if (isAttachedToWindowCompat) {
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDimension(widthMeasureSpec.specSize, widthMeasureSpec)

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> width
            MeasureSpec.AT_MOST -> minOf(width, heightMeasureSpec.specSize)
            else -> measureDimension(heightMeasureSpec.specSize, heightMeasureSpec)
        }

        setMeasuredDimension(width, height)

        contentBounds.set(
            paddingLeft,
            paddingTop,
            width - paddingRight,
            height - paddingBottom,
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        val model = model ?: return

        measureContext.clearExtras()

        val drawContext = drawContext(
            canvas = canvas,
            density = measureContext.density,
            fontScale = measureContext.fontScale,
            isLtr = measureContext.isLtr,
            elevationOverlayColor = elevationOverlayColor,
        )

        pieChart.setBounds(contentBounds)
        pieChart.draw(context = drawContext, model = model)
    }

    public companion object {

        @Suppress("MagicNumber")
        internal val sampleModel = pieEntryModelOf(
            FloatPieEntry(value = 1f, label = "One"),
            FloatPieEntry(value = 2f, label = "Two"),
            FloatPieEntry(value = 3f, label = "Three"),
            FloatPieEntry(value = 1f, label = "Four"),
        )
    }
}
