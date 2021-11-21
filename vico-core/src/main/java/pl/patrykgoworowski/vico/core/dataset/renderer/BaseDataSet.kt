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

package pl.patrykgoworowski.vico.core.dataset.renderer

import android.graphics.RectF
import pl.patrykgoworowski.vico.core.dataset.draw.ChartDrawContext
import pl.patrykgoworowski.vico.core.dimensions.BoundsAware
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.threshold.ThresholdLine
import pl.patrykgoworowski.vico.core.extension.getClosestMarkerEntryModel
import pl.patrykgoworowski.vico.core.extension.getEntryModel
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.layout.MeasureContext
import pl.patrykgoworowski.vico.core.marker.Marker

public abstract class BaseDataSet<in Model : EntryModel> : DataSet<Model>, BoundsAware {

    private val thresholdLines = ArrayList<ThresholdLine>()
    private val persistentMarkers = HashMap<Float, Marker>()

    protected val Model.drawMinY: Float
        get() = this@BaseDataSet.minY ?: minY
    protected val Model.drawMaxY: Float
        get() = this@BaseDataSet.maxY ?: maxY
    protected val Model.drawMinX: Float
        get() = this@BaseDataSet.minX ?: minX
    protected val Model.drawMaxX: Float
        get() = this@BaseDataSet.maxX ?: maxX

    protected var drawScale: Float = 1f

    override val bounds: RectF = RectF()

    override var minY: Float? = null
    override var maxY: Float? = null
    override var minX: Float? = null
    override var maxX: Float? = null

    override var maxScrollAmount: Float = 0f

    override fun addThresholdLine(thresholdLine: ThresholdLine): Boolean =
        thresholdLines.add(thresholdLine)

    override fun removeThresholdLine(thresholdLine: ThresholdLine): Boolean =
        thresholdLines.remove(thresholdLine)

    override fun addPersistentMarker(x: Float, marker: Marker) {
        persistentMarkers[x] = marker
    }

    override fun removePersistentMarker(x: Float): Boolean =
        persistentMarkers.remove(x) != null

    override fun draw(
        context: ChartDrawContext,
        model: Model,
        touchMarker: Marker?
    ) {
        if (model.entryCollections.isNotEmpty()) {
            drawDataSet(context, model)
        }
        drawThresholdLines(context, model)
        persistentMarkers.forEach { (x, marker) ->
            markerLocationMap.getEntryModel(x)?.also { markerModel ->
                marker.draw(
                    context = context,
                    bounds = bounds,
                    markedEntries = markerModel,
                )
            }
        }
        drawTouchMarker(context, model, touchMarker)
    }

    public abstract fun drawDataSet(
        context: ChartDrawContext,
        model: Model,
    )

    public open fun drawTouchMarker(
        context: ChartDrawContext,
        model: Model,
        marker: Marker?
    ) {
        val touchPoint = context.markerTouchPoint
        if (touchPoint == null || marker == null) return
        markerLocationMap.getClosestMarkerEntryModel(touchPoint)?.also { markerModel ->
            marker.draw(
                context = context,
                bounds = bounds,
                markedEntries = markerModel,
            )
        }
    }

    private fun drawThresholdLines(
        context: ChartDrawContext,
        model: Model,
    ) {
        val valueRange = (maxY ?: model.maxY) - (minY ?: model.minY)
        thresholdLines.forEach { line ->
            val centerY = bounds.bottom - (line.thresholdValue / valueRange * bounds.height())
            val textY =
                centerY + line.lineComponent.thicknessDp.half * when (line.labelVerticalPosition) {
                    ThresholdLine.LabelVerticalPosition.Top -> -1
                    ThresholdLine.LabelVerticalPosition.Bottom -> 1
                }
            line.lineComponent.drawHorizontal(
                context = context,
                left = bounds.left,
                right = bounds.right,
                centerY = centerY
            )
            line.textComponent.drawText(
                context = context,
                text = line.thresholdValue.toString(),
                textX = when (line.labelHorizontalPosition) {
                    ThresholdLine.LabelHorizontalPosition.Start -> bounds.left
                    ThresholdLine.LabelHorizontalPosition.End -> bounds.right
                },
                textY = textY,
                horizontalPosition = line.labelHorizontalPosition.position,
                verticalPosition = line
                    .getSuggestedLabelVerticalPosition(context, textY).position,
            )
        }
    }

    private fun ThresholdLine.getSuggestedLabelVerticalPosition(
        context: MeasureContext,
        textY: Float,
    ): ThresholdLine.LabelVerticalPosition {
        val labelHeight = textComponent.getHeight(context = context)
        return when (labelVerticalPosition) {
            ThresholdLine.LabelVerticalPosition.Top -> {
                if (textY - labelHeight < bounds.top) ThresholdLine.LabelVerticalPosition.Bottom
                else labelVerticalPosition
            }
            ThresholdLine.LabelVerticalPosition.Bottom -> {
                if (textY + labelHeight > bounds.bottom) ThresholdLine.LabelVerticalPosition.Top
                else labelVerticalPosition
            }
        }
    }

    protected fun MeasureContext.calculateDrawSegmentSpecIfNeeded(model: Model) {
        val measuredWidth = getMeasuredWidth(this, model)
        if (isHorizontalScrollEnabled) {
            drawScale = zoom
            maxScrollAmount = maxOf(0f, measuredWidth * drawScale - bounds.width())
        } else {
            maxScrollAmount = 0f
            drawScale = minOf(bounds.width() / measuredWidth, 1f)
        }
    }
}
