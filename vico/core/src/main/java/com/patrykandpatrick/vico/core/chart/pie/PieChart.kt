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

package com.patrykandpatrick.vico.core.chart.pie

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.pie.slice.Slice
import com.patrykandpatrick.vico.core.component.shape.extension.moveTo
import com.patrykandpatrick.vico.core.constants.FULL_DEGREES
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.dimensions.BoundsAware
import com.patrykandpatrick.vico.core.extension.PI_RAD
import com.patrykandpatrick.vico.core.extension.centerPoint
import com.patrykandpatrick.vico.core.extension.getRepeating
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.ifNotNull
import com.patrykandpatrick.vico.core.extension.maxOfOrNullIndexed
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.radius
import com.patrykandpatrick.vico.core.extension.round
import com.patrykandpatrick.vico.core.extension.set
import com.patrykandpatrick.vico.core.formatter.PieValueFormatter
import com.patrykandpatrick.vico.core.math.radians
import com.patrykandpatrick.vico.core.math.radiansDouble
import com.patrykandpatrick.vico.core.math.translatePointByAngle
import com.patrykandpatrick.vico.core.model.ExtraStore
import com.patrykandpatrick.vico.core.model.MutableExtraStore
import com.patrykandpatrick.vico.core.model.PieModel
import com.patrykandpatrick.vico.core.model.drawing.DefaultDrawingModelInterpolator
import com.patrykandpatrick.vico.core.model.drawing.DrawingModelInterpolator
import com.patrykandpatrick.vico.core.model.drawing.PieDrawingModel
import com.patrykandpatrick.vico.core.util.Point
import kotlin.math.max
import kotlin.math.sin

/**
 * A [PieChart] is a type of graph that represents the data in a circular graph.
 * The slices of pie show the relative size of the data, and it is a type of pictorial representation of data.
 *
 * @param slices defines appearances of the slices in the pie chart.
 * @param spacingDp the spacing between the slices in the pie chart.
 * @param outerSize the outer size of the pie chart.
 * @param innerSize the size of the hole in the pie chart.
 * @param startAngle the angle at which the first slice starts.
 */
public open class PieChart(
    public var slices: List<Slice>,
    public var spacingDp: Float,
    public var outerSize: Size.OuterSize,
    public var innerSize: Size.InnerSize,
    public var startAngle: Float,
    public var drawingModelInterpolator: DrawingModelInterpolator<
        PieDrawingModel.SliceInfo,
        PieDrawingModel,
        > = DefaultDrawingModelInterpolator(),
) : BoundsAware {
    init {
        checkParameters()
    }

    /**
     * The [PieValueFormatter] used to format the values of the pie chart.
     */
    public var valueFormatter: PieValueFormatter = PieValueFormatter.Default

    override val bounds: RectF = RectF()

    /**
     * The bounds of the oval that the pie chart is drawn in.
     */
    protected val oval: RectF = RectF()

    /**
     * The [Path] used to construct a path for the spacing between the slices.
     */
    protected val spacingPathBuilder: Path = Path()

    /**
     * The [Matrix] used to transform the [spacingPathBuilder] to the correct position.
     */
    protected val spacingMatrix: Matrix = Matrix()

    protected val insets: Insets = Insets()

    protected val drawingModelKey: ExtraStore.Key<PieDrawingModel> = ExtraStore.Key()

    protected fun checkParameters() {
        require(slices.isNotEmpty()) { "Slices cannot be empty." }
        require(spacingDp >= 0f) { "The spacing cannot be negative." }
    }

    /**
     * Updates the [oval] bounds which are later used to draw the pie chart.
     */
    public open fun updateOvalBounds(
        context: DrawContext,
        model: PieModel,
    ): Unit =
        with(context) {
            checkParameters()
            insets.clear()

            var ovalRadius = outerSize.getRadius(context, bounds.width(), bounds.height())

            var startAngle = startAngle

            val maxOffsetFromCenter =
                model.entries.maxOfOrNullIndexed { index, entry ->
                    val slice = slices.getRepeating(index)

                    val sweepAngle = entry.value / model.sumOfValues * FULL_DEGREES

                    ifNotNull(
                        slice.label,
                        valueFormatter.formatValue(index, entry.value, model),
                    ) { labelComponent, label ->
                        oval.set(
                            left = bounds.centerX() - ovalRadius,
                            top = bounds.centerY() - ovalRadius,
                            right = bounds.centerX() + ovalRadius,
                            bottom = bounds.centerY() + ovalRadius,
                        )

                        labelComponent.getInsets(
                            context = context,
                            contentBounds = bounds,
                            oval = oval,
                            label = label,
                            outInsets = insets,
                            angle = startAngle + sweepAngle.half,
                        )
                    }

                    startAngle += sweepAngle

                    slice.offsetFromCenterDp.pixels
                }.orZero

            ovalRadius -= maxOffsetFromCenter + insets.largestEdge
            ovalRadius = ovalRadius.round

            oval.set(
                left = bounds.centerX() - ovalRadius,
                top = bounds.centerY() - ovalRadius,
                right = bounds.centerX() + ovalRadius,
                bottom = bounds.centerY() + ovalRadius,
            )
        }

    /**
     * Draws the pie chart.
     *
     * @param context the [DrawContext] used to draw the pie chart.
     * @param model holds the data for the pie chart.
     */
    public fun draw(
        context: DrawContext,
        model: PieModel,
    ): Unit =
        with(context) {
            val innerRadius = innerSize.getRadius(context, bounds.width(), bounds.height())
            val drawingModel = model.extraStore.getOrNull(drawingModelKey)
            val sliceIno = drawingModel?.getOrNull(0)
            updateOvalBounds(context, model)

            require(oval.radius > innerRadius) { "The outer size must be greater than the inner size." }

            val restoreCount = if (spacingDp > 0f) saveLayer() else -1

            val sliceCount = sliceIno?.size ?: model.entries.size
            var drawAngle = startAngle

            for (index in 0 until sliceCount) {
                val slice = slices.getRepeating(index)

                val sliceInfo = sliceIno?.get(index.toFloat())
                val sliceInfoDegrees = sliceInfo?.degrees
                val entry = model.entries.getOrNull(index)
                val sweepAngle = sliceInfoDegrees ?: (checkNotNull(entry).value / model.sumOfValues * FULL_DEGREES)

                spacingPathBuilder.rewind()

                if (spacingDp > 0f) {
                    addSpacingSegment(spacingPathBuilder, sweepAngle, sweepAngle)
                    addSpacingSegment(spacingPathBuilder, drawAngle, sweepAngle)
                }

                if (innerRadius > 0f) {
                    addHole(spacingPathBuilder, innerRadius)
                }

                slice.draw(
                    context = context,
                    contentBounds = bounds,
                    oval = oval,
                    startAngle = drawAngle,
                    sweepAngle = sweepAngle,
                    holeRadius = innerRadius,
                    label = sliceInfo?.label ?: entry?.formattedValue(index, model),
                    spacingPath = spacingPathBuilder,
                    sliceOpacity = sliceInfo?.sliceOpacity ?: 1f,
                    labelOpacity = sliceInfo?.labelOpacity ?: 1f,
                )

                drawAngle += sweepAngle
            }

            if (restoreCount >= 0) restoreCanvasToCount(restoreCount)
        }

    protected open fun DrawContext.addSpacingSegment(
        pathBuilder: Path,
        startAngle: Float,
        sweepAngle: Float,
    ) {
        val spacing = spacingDp.pixels
        with(pathBuilder) {
            spacingMatrix.postRotate(startAngle, oval.centerX(), oval.centerY())

            if (sweepAngle > PI_RAD.half) {
                val correctedSpacing = spacing / sin(sweepAngle.half.radians)
                val correctedAngle =
                    if (sweepAngle == startAngle) {
                        PI_RAD - sweepAngle.half
                    } else {
                        sweepAngle.half
                    }
                val correctedSpacingFactor =
                    if (startAngle == sweepAngle && sweepAngle > PI_RAD) {
                        -1f
                    } else {
                        1f
                    }

                moveTo(
                    translatePointByAngle(
                        center = oval.centerPoint,
                        point =
                            Point(
                                x = oval.centerX() + correctedSpacing.half * correctedSpacingFactor,
                                y = oval.centerY(),
                            ),
                        angle = correctedAngle.radiansDouble,
                    ),
                )
                lineTo(oval.centerX(), oval.centerY() + spacing.half)
            } else {
                moveTo(oval.centerX(), oval.centerY() + spacing.half)
            }
            lineTo(bounds.right, oval.centerY() + spacing.half)
            lineTo(bounds.right + spacing, oval.centerY() - spacing.half)
            lineTo(oval.centerX(), oval.centerY() - spacing.half)
            close()
            transform(spacingMatrix)
            spacingMatrix.reset()
        }
    }

    protected open fun DrawContext.addHole(
        pathBuilder: Path,
        innerRadius: Float,
    ): Unit =
        with(pathBuilder) {
            addCircle(oval.centerX(), oval.centerY(), innerRadius, Path.Direction.CCW)
        }

    /**
     * Prepares the [PieChart] for a difference animation.
     */
    public fun prepareForTransformation(
        model: PieModel?,
        extraStore: MutableExtraStore,
    ) {
        val oldModel = extraStore.getOrNull(drawingModelKey)
        val customSize =
            if (oldModel != null) {
                max(oldModel.first().values.filter { it.degrees > 0f }.size, model?.entries?.size ?: 0)
            } else {
                model?.entries?.size
            }
        drawingModelInterpolator.setModels(
            old = oldModel,
            new = model?.toDrawingModel(customSize),
        )
    }

    /**
     * Carries out the pending difference animation.
     */
    public suspend fun transform(
        extraStore: MutableExtraStore,
        fraction: Float,
    ) {
        drawingModelInterpolator
            .transform(fraction)
            ?.let { extraStore[drawingModelKey] = it }
            ?: extraStore.remove(drawingModelKey)
    }

    private fun PieModel.Entry.formattedValue(
        index: Int,
        model: PieModel,
    ): CharSequence = valueFormatter.formatValue(index, value, model)

    private fun PieModel.toDrawingModel(customSize: Int?): PieDrawingModel =
        PieDrawingModel(
            slices =
                List(customSize ?: entries.size) { index ->
                    val entry = entries.getOrNull(index)
                    PieDrawingModel.SliceInfo(
                        degrees = entry?.value?.let { it / sumOfValues * FULL_DEGREES } ?: 0f,
                        label = entry?.formattedValue(index, this),
                    )
                },
        )
}
