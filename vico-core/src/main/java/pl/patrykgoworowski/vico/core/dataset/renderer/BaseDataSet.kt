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

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.vico.core.BoundsAware
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.segment.SegmentProperties
import pl.patrykgoworowski.vico.core.dataset.threshold.ThresholdLine
import pl.patrykgoworowski.vico.core.extension.getClosestMarkerEntryPositionModel
import pl.patrykgoworowski.vico.core.extension.half
import pl.patrykgoworowski.vico.core.marker.Marker

public abstract class BaseDataSet<in Model : EntryModel> : DataSet<Model>, BoundsAware {

    private val thresholdLines = ArrayList<ThresholdLine>()

    protected val Model.drawMinY: Float
        get() = this@BaseDataSet.minY ?: minY
    protected val Model.drawMaxY: Float
        get() = this@BaseDataSet.maxY ?: maxY
    protected val Model.drawMinX: Float
        get() = this@BaseDataSet.minX ?: minX
    protected val Model.drawMaxX: Float
        get() = this@BaseDataSet.maxX ?: maxX

    override val bounds: RectF = RectF()

    override var minY: Float? = null
    override var maxY: Float? = null
    override var minX: Float? = null
    override var maxX: Float? = null

    override var isHorizontalScrollEnabled: Boolean = false
    override var zoom: Float? = null

    override fun addThresholdLine(thresholdLine: ThresholdLine): Boolean =
        thresholdLines.add(thresholdLine)

    override fun removeThresholdLine(thresholdLine: ThresholdLine): Boolean =
        thresholdLines.remove(thresholdLine)

    override fun draw(
        canvas: Canvas,
        model: Model,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
        marker: Marker?
    ) {
        if (model.entryCollections.isNotEmpty()) {
            drawDataSet(canvas, model, segmentProperties, rendererViewState)
        }
        drawThresholdLines(canvas, model)
        drawMarker(canvas, model, segmentProperties, rendererViewState, marker)
    }

    abstract fun drawDataSet(
        canvas: Canvas,
        model: Model,
        segmentProperties: SegmentProperties,
        viewState: RendererViewState,
    )

    open fun drawMarker(
        canvas: Canvas,
        model: Model,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
        marker: Marker?
    ) {
        val touchPoint = rendererViewState.markerTouchPoint
        if (touchPoint == null || marker == null) return
        markerLocationMap.getClosestMarkerEntryPositionModel(touchPoint)?.let { markerModel ->
            marker.draw(
                canvas,
                bounds,
                markerModel,
            )
        }
    }

    private fun drawThresholdLines(
        canvas: Canvas,
        model: Model,
    ) {
        val valueRange = (maxY ?: model.maxY) - (minY ?: model.minY)
        thresholdLines.forEach { line ->
            val centerY = bounds.bottom - (line.thresholdValue / valueRange * bounds.height())
            val textY =
                centerY + line.lineComponent.thickness.half * when (line.labelVerticalPosition) {
                    ThresholdLine.LabelVerticalPosition.Top -> -1
                    ThresholdLine.LabelVerticalPosition.Bottom -> 1
                }
            line.lineComponent.drawHorizontal(
                canvas = canvas,
                left = bounds.left,
                right = bounds.right,
                centerY = centerY
            )
            line.textComponent.drawText(
                canvas = canvas,
                text = line.thresholdValue.toString(),
                textX = when (line.labelHorizontalPosition) {
                    ThresholdLine.LabelHorizontalPosition.Start -> bounds.left
                    ThresholdLine.LabelHorizontalPosition.End -> bounds.right
                },
                textY = textY,
                horizontalPosition = line.labelHorizontalPosition.position,
                verticalPosition = line.getSuggestedLabelVerticalPosition(textY).position,
            )
        }
    }

    private fun ThresholdLine.getSuggestedLabelVerticalPosition(
        textY: Float,
    ): ThresholdLine.LabelVerticalPosition {
        val labelHeight = textComponent.getHeight()
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
}
