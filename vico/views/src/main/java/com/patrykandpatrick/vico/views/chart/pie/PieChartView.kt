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
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import com.patrykandpatrick.vico.core.chart.pie.PieChart
import com.patrykandpatrick.vico.core.chart.pie.Size
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.context.MutableMeasureContext
import com.patrykandpatrick.vico.core.draw.drawContext
import com.patrykandpatrick.vico.core.entry.FloatPieEntry
import com.patrykandpatrick.vico.core.entry.PieEntryModel
import com.patrykandpatrick.vico.core.entry.pieEntryModelOf
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.views.extension.defaultColors
import com.patrykandpatrick.vico.views.extension.density
import com.patrykandpatrick.vico.views.extension.fontScale
import com.patrykandpatrick.vico.views.extension.getWidthAndHeight
import com.patrykandpatrick.vico.views.extension.isLtr
import com.patrykandpatrick.vico.views.theme.PieChartStyleHandler

/**
 * A [View] that displays a pie chart.
 */
public open class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val pieChartStyleHandler: PieChartStyleHandler = PieChartStyleHandler(
        context = context,
        attrs = attrs,
    )

    protected val contentBounds: RectF = RectF()

    protected val pieChart: PieChart = PieChart(
        slices = pieChartStyleHandler.slices,
        spacingDp = pieChartStyleHandler.sliceSpacing,
        outerSize = pieChartStyleHandler.outerSize,
        innerSize = pieChartStyleHandler.innerSize,
        startAngle = pieChartStyleHandler.startAngle,
    )

    protected var model: PieEntryModel? = null
        private set

    /**
     * The [List] of [Slice]s which define the appearance of each slice of the pie chart.
     */
    public var slices: List<Slice> by pieChart::slices

    /**
     * The spacing between each slice of the pie chart (in dp).
     */
    public var sliceSpacingDp: Float by pieChart::spacingDp

    /**
     * Defines the outer size of the pie chart.
     */
    public var outerSize: Size.OuterSize by pieChart::outerSize

    /**
     * Defines the inner size of the pie chart.
     */
    public var innerSize: Size.InnerSize by pieChart::innerSize

    /**
     * Defines the start angle of the pie chart (in degrees).
     */
    public var startAngle: Float by pieChart::startAngle

    /**
     * The color of elevation overlays, which are applied to [ShapeComponent]s that cast shadows.
     */
    public var elevationOverlayColor: Long = context.defaultColors.elevationOverlayColor

    protected val measureContext: MutableMeasureContext = MutableMeasureContext(
        canvasBounds = contentBounds,
        density = context.density,
        fontScale = context.fontScale,
        isLtr = context.isLtr,
    )

    init {
        if (isInEditMode) {
            setModel(model = sampleModel)
        }
    }

    /**
     * Sets the [PieEntryModel] to display.
     */
    public fun setModel(model: PieEntryModel) {
        this.model = model
        if (ViewCompat.isAttachedToWindow(this)) {
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (width, height) = getWidthAndHeight(widthMeasureSpec, heightMeasureSpec)
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
